package com.antonelli.servipedia.dao

import androidx.annotation.WorkerThread
import com.antonelli.servipedia.entity.ServiModel
import javax.inject.Inject

class DBRepository @Inject constructor(private val servisDao: ServisDao) {

    @WorkerThread
    suspend fun getAllItems(userId: String): List<ServiModel> {
        return servisDao.getAll(userId)
    }

    @WorkerThread
    suspend fun insertItem(servi: ServiModel) {
        servisDao.insert(servi)
    }

    @WorkerThread
    suspend fun deleteItem(servi: ServiModel) {
        servisDao.delete(servi)
    }

    @WorkerThread
    suspend fun getAllIds(): List<String> {
        return servisDao.getIds()
    }
}
