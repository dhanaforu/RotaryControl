package com.dhanapal.rotarycontrol.data.repository

import android.content.SharedPreferences
import com.dhanapal.rotarycontrol.domain.repository.Repository
import javax.inject.Inject

class RepositoryImpl @Inject constructor(private val sharedPreferences: SharedPreferences) :
    Repository {

    override suspend fun getInitialValueForStepControl(): Int {
        return sharedPreferences.getInt(STEP_CONTROL, 0)
    }

    override suspend fun setInitialValueForStepControl(initialValue: Int) {
        sharedPreferences.edit().putInt(STEP_CONTROL, initialValue).apply()
    }

    override suspend fun getInitialValueForFreeControl(): Int {
        return sharedPreferences.getInt(FREE_CONTROL, 0)
    }

    override suspend fun setInitialValueForFreeControl(initialValue: Int) {
        sharedPreferences.edit().putInt(FREE_CONTROL, initialValue).apply()
    }

    companion object {
        private const val STEP_CONTROL = "_stepControl"
        private const val FREE_CONTROL = "_freeControl"
    }

}