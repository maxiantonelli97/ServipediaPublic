package com.antonelli.servipedia.ui.firstsection.search

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.antonelli.servipedia.MainActivity
import com.antonelli.servipedia.databinding.FragmentSearchBinding
import com.antonelli.servipedia.entity.ServiModel
import com.antonelli.servipedia.entity.UserModel
import com.antonelli.servipedia.ui.firstsection.bottomsheet.BottomSheetFragment
import com.antonelli.servipedia.ui.firstsection.search.utils.IClickListener
import com.antonelli.servipedia.utils.BottomSheetListener
import com.antonelli.servipedia.utils.MainInterface
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private val viewModel: SearchViewModel by viewModels()
    private lateinit var contexto: Context
    private lateinit var servisAdapter: SearchAdapter
    private lateinit var database: FirebaseFirestore
    private var user: UserModel? = null
    private var rubro: String? = null
    private var modalBottomSheet: BottomSheetFragment? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        contexto = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        database = FirebaseFirestore.getInstance()
        binding = FragmentSearchBinding.inflate(inflater)
        startCollects()
        startListeners()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rubro = arguments?.getString("rubro")
    }

    override fun onResume() {
        super.onResume()
        user = (activity as MainActivity).getuser()
        if (rubro != null) {
            search()
        } else {
            (activity as MainActivity).toastError()
            findNavController().popBackStack()
        }
    }

    private fun startListeners() {
        binding.ivReload.setOnClickListener {
            binding.ivReload.isClickable = false
            binding.ivReload.visibility = View.GONE
            user = (activity as MainActivity).getuser()
            if (rubro != null) {
                search()
            } else {
                (activity as MainActivity).toastError()
                findNavController().popBackStack()
            }
        }
    }

    private fun getFavorites() {
        viewModel.idsFavs.value = null
        (activity as MainInterface).mostrarProgressBar()
        val user = (activity as MainActivity).getuser()
        if (user != null) {
            viewModel.getFavorites()
        } else {
            viewModel.servis?.let { it1 -> setAdapter(it1, arrayListOf()) }
        }
    }

    private fun search() {
        viewModel.servisResponse.value = null
        (activity as MainInterface).mostrarProgressBar()
        if ((activity as MainActivity).isOnline()) {
            database.collection("services").whereArrayContains("rubros", rubro!!).get().addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val lista: ArrayList<ServiModel> = arrayListOf()
                    querySnapshot.forEach { queryDocumentSnapshot ->
                        lista.add(queryDocumentSnapshot.toObject(ServiModel::class.java))
                    }
                    viewModel.servisResponse.value = lista
                } else {
                    viewModel.servisResponse.value = arrayListOf()
                }
            }.addOnFailureListener {
                (activity as MainInterface).ocultarProgressBar()
                (activity as MainActivity).toastError()
                binding.ivReload.visibility = View.VISIBLE
                binding.ivReload.isClickable = true
            }
        } else {
            (activity as MainInterface).ocultarProgressBar()
            binding.ivReload.visibility = View.VISIBLE
            binding.ivReload.isClickable = true
        }
    }

    private fun startCollects() {
        lifecycleScope.launch {
            viewModel.servisResponse.collect {
                if (it != null) {
                    if (it.isNotEmpty()) {
                        viewModel.servis = it
                        getFavorites()
                    } else {
                        binding.rvServis.visibility = View.GONE
                        binding.noItems.visibility = View.VISIBLE
                        (activity as MainInterface).ocultarProgressBar()
                    }
                    viewModel.servisResponse.value = null
                }
            }
        }
        lifecycleScope.launch {
            viewModel.idsFavs.collect {
                if (it != null) {
                    binding.rvServis.visibility = View.VISIBLE
                    binding.noItems.visibility = View.GONE
                    binding.ivReload.visibility = View.GONE
                    viewModel.servis?.let { it1 -> setAdapter(it1, it) }
                    viewModel.idsFavs.value = null
                }
            }
        }
    }

    private fun setAdapter(servisList: ArrayList<ServiModel>, idsFavs: ArrayList<String>?) {
        idsFavs?.forEach { idFav ->
            servisList.forEach { servi ->
                if (servi.id == idFav) {
                    servi.favorite = true
                }
            }
        }
        binding.rvServis.visibility = View.VISIBLE
        servisAdapter = SearchAdapter(
            servisList,
            object : IClickListener {

                override fun phoneClick(phone: String) {
                    try {
                        val intent = Intent(Intent.ACTION_DIAL)
                        intent.data = Uri.parse("tel:$phone")
                        contexto.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun instagramClick(link: String) {
                    val uri = Uri.parse(link)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.setPackage("com.instagram.android")

                    try {
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        // No encontró la aplicación, abre la versión web.
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(link)
                            )
                        )
                    }
                }

                override fun favClick(setFavorite: Boolean, servi: ServiModel) {
                    if (setFavorite) {
                        addFavorito(servi)
                    } else {
                        deleteFavorito(servi)
                    }
                }

                override fun ratingClick(servi: ServiModel, position: Int) {
                    modalBottomSheet = BottomSheetFragment.newInstance(
                        servi,
                        object : BottomSheetListener {
                            override fun closeBottomSheet() {
                                modalBottomSheet!!.dismiss()
                            }

                            override fun envioRating(newVote: String?, newRating: Double, delete: Boolean, positionVotes: Int) {
                                viewModel.servis?.let {
                                    if (newVote != null) {
                                        it[position].votes.add(newVote)
                                    }
                                    if (delete) {
                                        it[position].votes.removeAt(positionVotes)
                                    }
                                    it[position].rating = newRating
                                    servisAdapter.notifyItemChanged(position)
                                }
                            }
                        }
                    )
                    modalBottomSheet!!.show((activity as MainActivity).supportFragmentManager, BottomSheetFragment.TAG)
                }

                override fun isOnline(): Boolean {
                    return (contexto as MainActivity).isOnline()
                }

                override fun showToast(texto: String) {
                    Toast.makeText(contexto, texto, Toast.LENGTH_LONG).show()
                }

                override fun openMap(latitud: String, longitud: String) {
                    val gmmIntentUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitud,$longitud")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    startActivity(mapIntent)
                }

                override fun openMapLink(link: String) {
                    val gmmIntentUri = Uri.parse(link)
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    startActivity(mapIntent)
                }

                override fun openWsp(wsp: String) {
                    val wspIntentUri = Uri.parse("https://api.whatsapp.com/send?phone=$wsp")
                    val intent = Intent(Intent.ACTION_VIEW, wspIntentUri)
                    intent.setPackage("com.whatsapp")
                    try {
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        (activity as MainActivity).toastError()
                    }
                }
            },
            user != null
        )
        binding.rvServis.adapter = servisAdapter
        (activity as MainInterface).ocultarProgressBar()
    }

    private fun addFavorito(servi: ServiModel) {
        if (user?.id != null) {
            servi.userId = user?.id
            viewModel.addItem(servi)
        } else {
            (activity as MainActivity).toastError()
            binding.ivReload.visibility = View.VISIBLE
            binding.ivReload.isClickable = true
        }
    }

    private fun deleteFavorito(servi: ServiModel) {
        if (user?.id != null) {
            servi.userId = user?.id
            viewModel.deleteItem(servi)
        } else {
            (activity as MainActivity).toastError()
            binding.ivReload.visibility = View.VISIBLE
            binding.ivReload.isClickable = true
        }
    }

    override fun onPause() {
        super.onPause()
        modalBottomSheet?.dismiss()
        binding.rvServis.visibility = View.GONE
        binding.noItems.visibility = View.GONE
        binding.ivReload.visibility = View.GONE
    }
}
