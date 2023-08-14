package com.antonelli.servipedia.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "favs")
@Parcelize
data class ServiModel(
    @PrimaryKey() @ColumnInfo(name = "id")
    var id: String = "",
    @ColumnInfo(name = "name") var name: String? = null,
    @ColumnInfo(name = "phone") var phone: String? = null,
    @ColumnInfo(name = "wsp") var wsp: String? = null,
    @ColumnInfo(name = "desc") var desc: String? = null,
    @ColumnInfo(name = "rubros") var rubros: ArrayList<String> = arrayListOf(),
    @ColumnInfo(name = "favorite") var favorite: Boolean = false,
    @ColumnInfo(name = "instagramLink") var instagramLink: String? = null,
    @ColumnInfo(name = "address") var address: String? = null,
    @ColumnInfo(name = "latitud") var latitud: String? = null,
    @ColumnInfo(name = "longitud") var longitud: String? = null,
    @ColumnInfo(name = "mapLink") var mapLink: String? = null,
    @ColumnInfo(name = "rating") var rating: Double? = null,
    var votes: ArrayList<String> = arrayListOf(),
    @ColumnInfo(name = "userId") var userId: String? = null
) : Parcelable
