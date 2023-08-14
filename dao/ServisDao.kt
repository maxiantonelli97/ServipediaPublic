package com.antonelli.servipedia.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.antonelli.servipedia.entity.ServiModel

@Dao
interface ServisDao {

    @Query("Select * from favs WHERE userId=:userId")
    suspend fun getAll(userId: String): List<ServiModel>

    @Insert
    suspend fun insert(servi: ServiModel)

    @Delete
    suspend fun delete(servi: ServiModel)

    @Query("Select id from favs")
    suspend fun getIds(): List<String>
}
