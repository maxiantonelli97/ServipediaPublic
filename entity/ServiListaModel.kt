package com.antonelli.servipedia.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ServiListaModel(
    val lista: ArrayList<ServiModel> = arrayListOf()
) : Parcelable
