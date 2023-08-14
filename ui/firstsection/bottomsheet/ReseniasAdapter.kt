package com.antonelli.servipedia.ui.firstsection.bottomsheet

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.antonelli.servipedia.databinding.ReseniaItemBinding
import com.antonelli.servipedia.entity.ReseniaModel
import com.antonelli.servipedia.utils.IClickReseniaListener

class ReseniasAdapter(
    private var resList: List<ReseniaModel>,
    private var iClickListener: IClickReseniaListener
) :
    RecyclerView.Adapter<ReseniasAdapter.ViewHolder>() {

    var viewHolder: ViewHolder? = null
    private var contexto: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        contexto = parent.context
        val binding = ReseniaItemBinding
            .inflate(LayoutInflater.from(contexto), parent, false)
        viewHolder = ViewHolder(binding)

        return viewHolder!!
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            tvComment.text = resList[position].comentario
            tvRating.text = resList[position].valor.toString()
            if (resList[position].isOwn) {
                ivDelete.visibility = View.VISIBLE
                ivDelete.setOnClickListener {
                    iClickListener.deleteReseniaI(position)
                }
            } else {
                ivDelete.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int {
        return resList.size
    }

    inner class ViewHolder(val binding: ReseniaItemBinding) : RecyclerView.ViewHolder(binding.root)
}
