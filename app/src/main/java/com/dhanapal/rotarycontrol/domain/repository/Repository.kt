package com.dhanapal.rotarycontrol.domain.repository

interface Repository {

    suspend fun getInitialValueForStepControl(): Int

    suspend fun setInitialValueForStepControl(initialValue: Int)

    suspend fun getInitialValueForFreeControl(): Int

    suspend fun setInitialValueForFreeControl(initialValue: Int)
}