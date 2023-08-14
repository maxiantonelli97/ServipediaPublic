package com.antonelli.servipedia.ui.firstsection.search.utils

import com.antonelli.servipedia.entity.ServiModel

interface IClickListener {
    fun phoneClick(phone: String)
    fun instagramClick(link: String)
    fun favClick(setFavorite: Boolean, servi: ServiModel)
    fun isOnline(): Boolean
    fun showToast(texto: String)
    fun openMap(latitud: String, longitud: String)
    fun openMapLink(link: String)
    fun openWsp(wsp: String)

    fun ratingClick(servi: ServiModel, position: Int)
}
