package com.antonelli.servipedia.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RubroModel(
    val id: String? = null,
    val name: String? = null,
    val image: String? = null
) : Parcelable
