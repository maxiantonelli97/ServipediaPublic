package com.antonelli.servipedia.ui.firstsection.search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.antonelli.servipedia.R
import com.antonelli.servipedia.databinding.ServiItemBinding
import com.antonelli.servipedia.entity.ServiModel
import com.antonelli.servipedia.ui.firstsection.search.utils.IClickListener

class SearchAdapter(
    private var servisList: ArrayList<ServiModel>,
    private var iClickListener: IClickListener,
    private val logueado: Boolean
) :
    RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    private var viewHolder: ViewHolder? = null
    private var contexto: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        contexto = parent.context
        val binding = ServiItemBinding
            .inflate(LayoutInflater.from(contexto), parent, false)
        viewHolder = ViewHolder(binding)

        return viewHolder!!
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            tvName.text = servisList[position].name ?: "Sin nombre"
            tvDesc.text = servisList[position].desc ?: "Sin descripción"
            tvAddress.text = servisList[position].address ?: "Sin dirección"

            if (servisList[position].wsp == null) {
                ivWsp.setImageDrawable(
                    contexto?.resources?.let { it1 ->
                        ResourcesCompat.getDrawable(
                            it1,
                            R.drawable.wsp_disabled,
                            null
                        )
                    }
                )
            } else {
                ivWsp.setImageDrawable(
                    contexto?.resources?.let { it1 ->
                        ResourcesCompat.getDrawable(
                            it1,
                            R.drawable.ic_wsp,
                            null
                        )
                    }
                )
            }

            if (servisList[position].mapLink == null && (
                servisList[position].latitud == null ||
                    servisList[position].longitud == null
                )
            ) {
                ivMap.setImageDrawable(
                    contexto?.resources?.let { it1 ->
                        ResourcesCompat.getDrawable(
                            it1,
                            R.drawable.map_disabled,
                            null
                        )
                    }
                )
            } else {
                ivMap.setImageDrawable(
                    contexto?.resources?.let { it1 ->
                        ResourcesCompat.getDrawable(
                            it1,
                            R.drawable.ic_map,
                            null
                        )
                    }
                )
            }

            if (servisList[position].instagramLink == null) {
                ivInstagram.setImageDrawable(
                    contexto?.resources?.let { it1 ->
                        ResourcesCompat.getDrawable(
                            it1,
                            R.drawable.insta_disabled,
                            null
                        )
                    }
                )
            } else {
                ivInstagram.setImageDrawable(
                    contexto?.resources?.let { it1 ->
                        ResourcesCompat.getDrawable(
                            it1,
                            R.drawable.ic_insta,
                            null
                        )
                    }
                )
            }

            if (servisList[position].phone == null) {
                ivPhone.setImageDrawable(
                    contexto?.resources?.let { it1 ->
                        ResourcesCompat.getDrawable(
                            it1,
                            R.drawable.phone_disabled,
                            null
                        )
                    }
                )
            } else {
                ivPhone.setImageDrawable(
                    contexto?.resources?.let { it1 ->
                        ResourcesCompat.getDrawable(
                            it1,
                            R.drawable.ic_phone,
                            null
                        )
                    }
                )
            }

            tvRating.text =
                (Math.round((servisList[position].rating ?: 0.0) * 100.0) / 100.0).toString()

            if (logueado) {
                ivFavorito.visibility = View.VISIBLE
                if (servisList[position].favorite) {
                    ivFavorito.setImageDrawable(
                        contexto?.resources?.let { it1 ->
                            ResourcesCompat.getDrawable(
                                it1,
                                R.drawable.ic_baseline_favorite_24,
                                null
                            )
                        }
                    )
                } else {
                    ivFavorito.setImageDrawable(
                        contexto?.resources?.let { it1 ->
                            ResourcesCompat.getDrawable(
                                it1,
                                R.drawable.ic_baseline_favorite_border_24,
                                null
                            )
                        }
                    )
                }
            } else {
                ivFavorito.visibility = View.GONE
            }

            ivRating.setOnClickListener {
                servisList[position].let { it1 -> iClickListener.ratingClick(it1, position) }
            }

            ivPhone.setOnClickListener {
                servisList[position].phone?.let { it1 -> iClickListener.phoneClick(it1) }
            }

            ivInstagram.setOnClickListener {
                servisList[position].instagramLink?.let { it1 -> iClickListener.instagramClick(it1) }
            }

            ivMap.setOnClickListener {
                if (servisList[position].mapLink != null) {
                    servisList[position].mapLink?.let { it1 -> iClickListener.openMapLink(it1) }
                } else {
                    servisList[position].latitud?.let { latitud ->
                        servisList[position].longitud?.let { longitud ->
                            iClickListener.openMap(latitud, longitud)
                        }
                    }
                }
            }

            ivWsp.setOnClickListener {
                servisList[position].wsp?.let { it1 -> iClickListener.openWsp(it1) }
            }

            ivFavorito.setOnClickListener {
                if (iClickListener.isOnline()) {
                    if (servisList[position].favorite) {
                        ivFavorito.setImageDrawable(
                            contexto?.resources?.let { it1 ->
                                ResourcesCompat.getDrawable(
                                    it1,
                                    R.drawable.ic_baseline_favorite_border_24,
                                    null
                                )
                            }
                        )
                        servisList[position].favorite = false
                        iClickListener.favClick(false, servisList[position])
                    } else {
                        ivFavorito.setImageDrawable(
                            contexto?.resources?.let { it1 ->
                                ResourcesCompat.getDrawable(
                                    it1,
                                    R.drawable.ic_baseline_favorite_24,
                                    null
                                )
                            }
                        )
                        servisList[position].favorite = true
                        iClickListener.favClick(true, servisList[position])
                    }
                } else {
                    contexto?.getString(R.string.no_internet)
                        ?.let { it1 -> iClickListener.showToast(it1) }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return servisList.size
    }

    inner class ViewHolder(val binding: ServiItemBinding) : RecyclerView.ViewHolder(binding.root)
}
