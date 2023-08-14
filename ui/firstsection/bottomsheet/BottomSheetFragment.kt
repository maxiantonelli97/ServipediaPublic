package com.antonelli.servipedia.ui.firstsection.bottomsheet

import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.antonelli.servipedia.MainActivity
import com.antonelli.servipedia.R
import com.antonelli.servipedia.databinding.FragmentBottomSheetBinding
import com.antonelli.servipedia.entity.ReseniaModel
import com.antonelli.servipedia.entity.ServiModel
import com.antonelli.servipedia.entity.UserModel
import com.antonelli.servipedia.utils.BottomSheetListener
import com.antonelli.servipedia.utils.IClickReseniaListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var resAdapter: ReseniasAdapter
    private lateinit var binding: FragmentBottomSheetBinding
    private val vm: BottomSheetViewModel by viewModels()
    private lateinit var database: FirebaseFirestore
    private var user: UserModel? = null
    private lateinit var bottomSheetListener: BottomSheetListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        this.isCancelable = false
        binding = FragmentBottomSheetBinding.inflate(inflater)
        database = FirebaseFirestore.getInstance()
        user = (activity as MainActivity).getuser()
        if (user != null) {
            binding.tvNoRating.visibility = View.GONE
            binding.llResenia.visibility = View.VISIBLE
        } else {
            binding.tvNoRating.text = getString(R.string.log_to_rating)
            binding.tvNoRating.visibility = View.VISIBLE
            binding.llResenia.visibility = View.GONE
        }
        binding.bSendRes.isClickable = true
        binding.bSendRes.isFocusable = true
        return binding.root
    }

    companion object {
        const val TAG = "ModalBottomSheet"

        fun newInstance(servi: ServiModel, listener: BottomSheetListener): BottomSheetFragment {
            val bundle = Bundle()
            bundle.putParcelable("servi", servi)
            val fragment = BottomSheetFragment()
            fragment.bottomSheetListener = listener
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when {
            SDK_INT >= 33 -> vm.servi = arguments?.getParcelable("servi", ServiModel::class.java)
            else -> @Suppress("DEPRECATION")
            vm.servi = arguments?.getParcelable("servi") as? ServiModel
        }
        startListeners()
        startCollects()
        if (vm.servi != null) {
            setRating()
        }
    }

    private fun setRating() {
        showProgressBar()
        vm.resList.value = null
        vm.cantVotos = 0
        vm.rating = 0.0
        vm.servi!!.votes.let { votes ->
            val lista: ArrayList<ReseniaModel> = arrayListOf()
            val gson = Gson()
            if (votes.isNotEmpty()) {
                votes.forEach { vote ->
                    val aux = gson.fromJson(vote, ReseniaModel::class.java)
                    if (user?.id == aux.idUser) {
                        aux.isOwn = true
                        binding.llResenia.visibility = View.GONE
                        binding.tvNoRating.visibility = View.VISIBLE
                        binding.tvNoRating.text = getString(R.string.already_rating)
                    }
                    vm.rating += aux.valor ?: 0
                    lista.add(aux)
                }
                vm.rating = vm.rating / votes.size
            }
            vm.cantVotos = lista.size
            vm.resListAux = lista
            vm.resList.value = lista
        }
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
        binding.standardBottomSheet.visibility = View.GONE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
        binding.standardBottomSheet.visibility = View.VISIBLE
    }

    private fun sendResenia() {
        showProgressBar()
        if (vm.servi?.id != null) {
            val actualizar = mutableMapOf<String, Any>()
            val reseniaText = getString(
                R.string.reseniaModel,
                vm.givenRating.toString(),
                vm.givenComment,
                user?.id
            )
            actualizar["votes"] = FieldValue.arrayUnion(reseniaText)
            vm.rating = ((vm.rating * vm.cantVotos) + vm.givenRating) / (vm.cantVotos + 1)
            actualizar["rating"] = vm.rating
            vm.cantVotos += 1
            database.collection("services")
                .document(vm.servi?.id!!)
                .update(actualizar)
                .addOnSuccessListener {
                    hideProgressBar()
                    binding.llResenia.visibility = View.GONE
                    binding.tvNoRating.visibility = View.VISIBLE
                    binding.tvNoRating.text = getString(R.string.already_rating)
                    vm.resListAux.add(
                        ReseniaModel(
                            vm.givenRating,
                            vm.givenComment,
                            user?.id,
                            true
                        )
                    )
                    bottomSheetListener.envioRating(reseniaText, vm.rating, false, -1)
                    vm.resList.value = vm.resListAux
                }.addOnFailureListener {
                    (activity as MainActivity).toastError()
                }
        } else {
            (activity as MainActivity).toastError()
        }
    }

    fun deleteResenia(position: Int) {
        showProgressBar()
        vm.servi?.id?.let {
            val actualizar = mutableMapOf<String, Any>()
            val reseniaText = getString(
                R.string.reseniaModel,
                vm.resListAux[position].valor.toString(),
                vm.resListAux[position].comentario.toString(),
                vm.resListAux[position].idUser.toString()
            )
            actualizar["votes"] = FieldValue.arrayRemove(reseniaText)
            vm.rating = ((vm.rating * vm.cantVotos) - vm.resListAux[position].valor!!) / (vm.cantVotos - 1)
            actualizar["rating"] = vm.rating
            vm.cantVotos -= 1
            vm.resListAux.removeAt(position)
            database.collection("services")
                .document(it)
                .update(actualizar)
                .addOnSuccessListener {
                    binding.llResenia.visibility = View.VISIBLE
                    binding.tvNoRating.visibility = View.GONE
                    binding.bSendRes.isClickable = true
                    binding.bSendRes.isFocusable = true
                    bottomSheetListener.envioRating(null, vm.rating, true, position)
                    vm.resList.value = vm.resListAux
                }.addOnFailureListener {
                    (activity as MainActivity).toastError()
                }
        }
    }

    private fun startCollects() {
        lifecycleScope.launch {
            vm.resList.collect {
                if (it != null) {
                    vm.resListAux = it
                    binding.progressBar.visibility = View.GONE
                    binding.standardBottomSheet.visibility = View.VISIBLE
                    resAdapter = ReseniasAdapter(
                        it,
                        object : IClickReseniaListener {
                            override fun deleteReseniaI(position: Int) {
                                if ((activity as MainActivity).isOnline()) {
                                    deleteResenia(position)
                                }
                            }
                        }
                    )
                    binding.rvResenias.adapter = resAdapter
                    vm.resList.value = null
                    hideProgressBar()
                }
            }
        }
    }

    private fun startListeners() {
        binding.bSendRes.setOnClickListener {
            view?.let { (activity as MainActivity).hideKeyboard(it) }
            if (validaciones()) {
                if ((activity as MainActivity).isOnline()) {
                    binding.bSendRes.isClickable = false
                    sendResenia()
                }
            }
        }
        binding.ivClose.setOnClickListener {
            bottomSheetListener.closeBottomSheet()
            dismiss()
        }
    }

    private fun validaciones(): Boolean {
        var valid = true
        if (binding.etComment.text.toString().length >= 10) {
            vm.givenComment = binding.etComment.text.toString()
            binding.tlComment.error = null
        } else {
            valid = false
            binding.tlComment.error = getString(R.string.min_leght, 10)
        }
        if (binding.ratingBar.rating > 0) {
            binding.noStars.visibility = View.GONE
            vm.givenRating = binding.ratingBar.rating.toInt()
        } else {
            valid = false
            binding.noStars.visibility = View.VISIBLE
            vm.givenRating = 0
        }
        return valid
    }
}
