package com.dhanapal.rotarycontrol.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhanapal.rotarycontrol.domain.usecase.GetFreeControlValueUseCase
import com.dhanapal.rotarycontrol.domain.usecase.GetStepControlValueUseCase
import com.dhanapal.rotarycontrol.domain.usecase.UpdateFreeControlValueUseCase
import com.dhanapal.rotarycontrol.domain.usecase.UpdateStepControlValueUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getFreeControlValueUseCase: GetFreeControlValueUseCase,
    private val getStepControlValueUseCase: GetStepControlValueUseCase,
    private val setFreeControlValueUseCase: UpdateFreeControlValueUseCase,
    private val setStepControlValueUseCase: UpdateStepControlValueUseCase
): ViewModel() {

    private val _freeControlValue = MutableLiveData<Int?>()
    val freeControlValue:LiveData<Int?> = _freeControlValue

    private val _stepControlValue = MutableLiveData<Int?>()
    val stepControlValue:LiveData<Int?> = _stepControlValue

    fun fetchInitialValues() = viewModelScope.launch {
        _freeControlValue.value = getFreeControlValueUseCase()
        _stepControlValue.value = getStepControlValueUseCase()
    }

    fun updateFreeControlValue(initialValue: Int) = viewModelScope.launch {
        setFreeControlValueUseCase(initialValue)
    }

    fun updateStepControlValue(initialValue: Int) = viewModelScope.launch {
        setStepControlValueUseCase(initialValue)
    }
}