package com.antonelli.servipedia.ui.firstsection.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antonelli.servipedia.dao.DBRepository
import com.antonelli.servipedia.entity.ServiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val repository: DBRepository) : ViewModel() {
    var servisResponse: MutableStateFlow<ArrayList<ServiModel>?> = MutableStateFlow(null)
    var servis: ArrayList<ServiModel>? = null
    var idsFavs: MutableStateFlow<ArrayList<String>?> = MutableStateFlow(null)

    fun getFavorites() = viewModelScope.launch {
        val aux = repository.getAllIds()
        idsFavs.value = aux as ArrayList<String>
    }

    fun addItem(servi: ServiModel) = viewModelScope.launch {
        repository.insertItem(servi)
    }

    fun deleteItem(servi: ServiModel) = viewModelScope.launch {
        repository.deleteItem(servi)
    }
}
