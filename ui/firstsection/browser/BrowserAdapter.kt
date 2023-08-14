package com.antonelli.servipedia.ui.firstsection.browser

import android.content.Context
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.antonelli.servipedia.R
import com.antonelli.servipedia.databinding.RubroItemBinding
import com.antonelli.servipedia.entity.RubroModel
import com.antonelli.servipedia.utils.RubroSelListener

class BrowserAdapter(
    private var rubrosList: ArrayList<RubroModel>,
    private var iClickListener: RubroSelListener
) :
    RecyclerView.Adapter<BrowserAdapter.ViewHolder>() {

    var viewHolder: ViewHolder? = null
    private var contexto: Context? = null
    private val ancho = Resources.getSystem().displayMetrics.widthPixels / 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        contexto = parent.context
        val binding = RubroItemBinding
            .inflate(LayoutInflater.from(contexto), parent, false)
        viewHolder = ViewHolder(binding)

        val aux = binding.cardItem.layoutParams
        aux.width = ancho
        aux.height = ancho
        binding.cardItem.layoutParams = aux
        return viewHolder!!
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            tvRubro.text = rubrosList[position].name

            cardItem.setOnClickListener {
                rubrosList[position].id?.let { it1 -> iClickListener.rubroSel(it1) }
            }

            try {
                val imageBytes = Base64.decode(rubrosList[position].image, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                ivRubro.setImageBitmap(decodedImage)
            } catch (_: Error) {
                contexto?.resources?.let { it1 ->
                    ResourcesCompat.getDrawable(
                        it1,
                        R.drawable.baseline_close_24,
                        null
                    )
                }
            }
        }
    }

    fun filter(newList: ArrayList<RubroModel>) {
        rubrosList = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return rubrosList.size
    }

    inner class ViewHolder(val binding: RubroItemBinding) : RecyclerView.ViewHolder(binding.root)
}
