package com.dhanapal.rotarycontrol.domain.usecase

import com.dhanapal.rotarycontrol.domain.repository.Repository
import javax.inject.Inject

class UpdateStepControlValueUseCase @Inject constructor(private val repository: Repository) {
    suspend operator fun invoke(initialValue: Int) {
        repository.setInitialValueForStepControl(initialValue)
    }
}