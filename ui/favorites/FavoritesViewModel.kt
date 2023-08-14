package com.antonelli.servipedia.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antonelli.servipedia.dao.DBRepository
import com.antonelli.servipedia.entity.ServiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(private val repository: DBRepository) : ViewModel() {

    var servis: ArrayList<ServiModel>? = arrayListOf()
    var servisResponse: MutableStateFlow<List<ServiModel>?> = MutableStateFlow(null)
    fun getFavorites(idUser: String) {
        viewModelScope.launch {
            val aux = repository.getAllItems(idUser)
            servisResponse.value = aux
        }
    }

    fun deleteFavorito(servi: ServiModel) = viewModelScope.launch {
        repository.deleteItem(servi)
    }
}
