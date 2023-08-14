package com.antonelli.servipedia.ui.firstsection.browser

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.antonelli.servipedia.MainActivity
import com.antonelli.servipedia.R
import com.antonelli.servipedia.databinding.FragmentBrowserBinding
import com.antonelli.servipedia.entity.RubroModel
import com.antonelli.servipedia.utils.MainInterface
import com.antonelli.servipedia.utils.RubroSelListener
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BrowserFragment : Fragment() {

    private lateinit var binding: FragmentBrowserBinding
    private val viewModel: BrowserViewModel by viewModels()
    private lateinit var contexto: Context
    private lateinit var database: FirebaseFirestore

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
        binding = FragmentBrowserBinding.inflate(inflater)
        startCollects()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDatos()
        binding.clReload.setOnClickListener {
            binding.clReload.isClickable = false
            getDatos()
        }
    }

    private fun setAdapter() {
        binding.rvRubros.adapter = BrowserAdapter(
            viewModel.rubrosListCopy.value!!,
            object : RubroSelListener {
                override fun rubroSel(rubro: String) {
                    val bundle = Bundle()
                    bundle.putString("rubro", rubro)
                    findNavController().navigate(R.id.searchFragment, bundle)
                }
            }
        )
        binding.clBrowser.visibility = View.VISIBLE
        binding.clReload.visibility = View.GONE
        (activity as MainInterface).ocultarProgressBar()
        setBrowserBarListener()
    }

    private fun setBrowserBarListener() {
        binding.svBuscador.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                if (p0 != null) {
                    val aux = arrayListOf<RubroModel>()
                    viewModel.rubrosListCopy.value?.forEach {
                        if (it.name?.lowercase()?.contains(p0.lowercase()) == true) {
                            aux.add(it)
                        }
                    }
                    (binding.rvRubros.adapter as BrowserAdapter).filter(aux)
                }
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                if (p0 != null) {
                    val aux = arrayListOf<RubroModel>()
                    viewModel.rubrosListCopy.value?.forEach {
                        if (it.name?.lowercase()?.contains(p0.lowercase()) == true) {
                            aux.add(it)
                        }
                    }
                    (binding.rvRubros.adapter as BrowserAdapter).filter(aux)
                }
                return false
            }
        })
    }

    private fun getDatos() {
        (activity as MainInterface).mostrarProgressBar()
        if ((activity as MainActivity).isOnline()) {
            database.collection("rubros").orderBy("name").get().addOnSuccessListener {
                if (it != null) {
                    val documents = it.toObjects(RubroModel::class.java)
                    if (documents.isNotEmpty()) {
                        viewModel.rubrosListResponse.value = documents as ArrayList<RubroModel>
                    }
                } else {
                    (activity as MainInterface).ocultarProgressBar()
                    (activity as MainActivity).toastError()
                    binding.clBrowser.visibility = View.GONE
                    binding.clReload.visibility = View.VISIBLE
                    binding.clReload.isClickable = true
                }
            }.addOnFailureListener {
                (activity as MainInterface).ocultarProgressBar()
                (activity as MainActivity).toastError()
                binding.clBrowser.visibility = View.GONE
                binding.clReload.visibility = View.VISIBLE
                binding.clReload.isClickable = true
            }
        } else {
            (activity as MainInterface).ocultarProgressBar()
            binding.clBrowser.visibility = View.GONE
            binding.clReload.visibility = View.VISIBLE
            binding.clReload.isClickable = true
        }
    }

    private fun startCollects() {
        lifecycleScope.launch {
            viewModel.rubrosListResponse.collect { list ->
                if (list != null) {
                    viewModel.rubrosListCopy.value = list
                    setAdapter()
                    binding.clBrowser.visibility = View.VISIBLE
                    viewModel.rubrosListResponse.value = null
                }
            }
        }
    }
}
