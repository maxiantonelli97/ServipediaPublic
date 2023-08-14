package com.antonelli.servipedia.ui.favorites

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
import com.antonelli.servipedia.MainActivity
import com.antonelli.servipedia.databinding.FragmentFavoriteBinding
import com.antonelli.servipedia.entity.ServiModel
import com.antonelli.servipedia.ui.firstsection.search.utils.IClickListener
import com.antonelli.servipedia.utils.MainInterface
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavoritesFragment : Fragment() {

    private lateinit var binding: FragmentFavoriteBinding
    private val viewModel: FavoritesViewModel by viewModels()
    private lateinit var contexto: Context
    private lateinit var servisAdapter: FavoritesAdapter
    private lateinit var database: FirebaseFirestore
    private var userId: String? = null

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
        binding = FragmentFavoriteBinding.inflate(inflater)
        startCollects()
        startListeners()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        (activity as MainInterface).mostrarProgressBar()
        userId = (activity as MainActivity).getuser()?.id
        if (userId != null) {
            viewModel.getFavorites(userId!!)
        } else {
            (activity as MainInterface).ocultarProgressBar()
            (activity as MainActivity).toastError()
            binding.noItems.visibility = View.GONE
            binding.rvServis.visibility = View.GONE
            binding.ivReload.visibility = View.VISIBLE
        }
    }

    private fun startListeners() {
        binding.ivReload.setOnClickListener {
            binding.ivReload.isClickable = false
            binding.ivReload.visibility = View.GONE
            (activity as MainInterface).mostrarProgressBar()
            if (userId != null) {
                viewModel.getFavorites(userId!!)
            } else {
                (activity as MainInterface).ocultarProgressBar()
                (activity as MainActivity).toastError()
                binding.noItems.visibility = View.GONE
                binding.rvServis.visibility = View.GONE
                binding.ivReload.visibility = View.VISIBLE
                binding.ivReload.isClickable = true
            }
        }
    }

    private fun startCollects() {
        lifecycleScope.launch {
            viewModel.servisResponse.collect {
                if (it != null) {
                    if (it.isEmpty()) {
                        (activity as MainInterface).ocultarProgressBar()
                        binding.noItems.visibility = View.VISIBLE
                    } else {
                        binding.noItems.visibility = View.GONE
                        viewModel.servis = it as ArrayList<ServiModel>
                        setAdapter(viewModel.servis!!)
                    }
                    viewModel.servisResponse.value = null
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        binding.noItems.visibility = View.GONE
        binding.rvServis.visibility = View.GONE
        binding.ivReload.visibility = View.GONE
    }

    private fun setAdapter(servisList: ArrayList<ServiModel>) {
        (activity as MainInterface).ocultarProgressBar()
        binding.rvServis.visibility = View.VISIBLE
        binding.ivReload.visibility = View.GONE
        binding.noItems.visibility = View.GONE
        servisAdapter = FavoritesAdapter(
            servisList,
            object : IClickListener {

                override fun phoneClick(phone: String) {
                    try {
                        val intent = Intent(Intent.ACTION_DIAL)
                        intent.data = Uri.parse("tel:$phone")
                        contexto.startActivity(intent)
                    } catch (e: Exception) {
                        (activity as MainActivity).toastError()
                        binding.noItems.visibility = View.GONE
                        binding.rvServis.visibility = View.GONE
                        binding.ivReload.visibility = View.VISIBLE
                        binding.ivReload.isClickable = true
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
                    deleteFavorito(servi)
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

                override fun ratingClick(servi: ServiModel, position: Int) {
                    // Not implemented
                }
            }
        )
        binding.rvServis.adapter = servisAdapter
        (activity as MainInterface).ocultarProgressBar()
    }

    private fun deleteFavorito(servi: ServiModel) {
        if (userId != null) {
            viewModel.deleteFavorito(servi)
            viewModel.servis?.remove(servi)
            servisAdapter.notifyDataSetChanged()
            if (viewModel.servis?.size == 0) {
                binding.noItems.visibility = View.VISIBLE
                binding.rvServis.visibility = View.GONE
            }
        } else {
            (activity as MainActivity).toastError()
            binding.noItems.visibility = View.GONE
            binding.rvServis.visibility = View.GONE
            binding.ivReload.visibility = View.VISIBLE
            binding.ivReload.isClickable = true
        }
    }
}
