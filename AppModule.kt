package com.antonelli.servipedia

import android.content.Context
import com.antonelli.servipedia.dao.DBHelper
import com.antonelli.servipedia.dao.DBRepository
import com.antonelli.servipedia.dao.ServisDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    fun getDBRepository(servisDao: ServisDao): DBRepository {
        return DBRepository(servisDao)
    }

    @Provides
    fun providesServisDao(dBHelper: DBHelper): ServisDao {
        return dBHelper.userDao()
    }

    @Provides
    fun providesDBHelper(contexto: Context): DBHelper {
        return DBHelper.getDatabase(contexto)
    }

    @Provides
    fun provideContext(@ApplicationContext contexto: Context): Context {
        return contexto
    }
}
