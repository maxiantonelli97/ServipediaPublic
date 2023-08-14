package com.antonelli.servipedia.ui.firstsection.browser

import androidx.lifecycle.ViewModel
import com.antonelli.servipedia.entity.RubroModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class BrowserViewModel @Inject constructor() : ViewModel() {
    var rubrosListResponse: MutableStateFlow<ArrayList<RubroModel>?> = MutableStateFlow(null)
    var rubrosListCopy: MutableStateFlow<ArrayList<RubroModel>?> = MutableStateFlow(null)
}
