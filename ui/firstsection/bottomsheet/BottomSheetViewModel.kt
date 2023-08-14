package com.antonelli.servipedia.ui.firstsection.bottomsheet

import androidx.lifecycle.ViewModel
import com.antonelli.servipedia.entity.ReseniaModel
import com.antonelli.servipedia.entity.ServiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class BottomSheetViewModel @Inject constructor() : ViewModel() {
    var givenRating: Int = 0
    var givenComment: String = ""
    var cantVotos: Int = 0
    var rating: Double = 0.0
    var servi: ServiModel? = null
    val resList: MutableStateFlow<ArrayList<ReseniaModel>?> = MutableStateFlow(null)
    var resListAux: ArrayList<ReseniaModel> = arrayListOf()
}
