package com.dhanapal.rotarycontrol.di

import android.content.Context
import android.content.SharedPreferences
import com.dhanapal.rotarycontrol.presentation.RotaryControlApplication
import com.dhanapal.rotarycontrol.data.repository.RepositoryImpl
import com.dhanapal.rotarycontrol.domain.repository.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun provideApplication(@ApplicationContext app: Context): RotaryControlApplication {
        return app as RotaryControlApplication
    }

    @Provides
    @Singleton
    fun provideContext(application: RotaryControlApplication): Context {
        return application.applicationContext
    }

    @Singleton
    @Provides
    fun provideSharedPreference(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(context.packageName + "_preferences", Context.MODE_PRIVATE)
    }

    @Singleton
    @Provides
    fun provideRepository(sharedPreferences: SharedPreferences): Repository {
        return RepositoryImpl(sharedPreferences)
    }

}