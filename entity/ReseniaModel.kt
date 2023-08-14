package com.antonelli.servipedia.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReseniaModel(
    val valor: Int? = null,
    val comentario: String? = null,
    val idUser: String? = null,
    var isOwn: Boolean = false
) : Parcelable
