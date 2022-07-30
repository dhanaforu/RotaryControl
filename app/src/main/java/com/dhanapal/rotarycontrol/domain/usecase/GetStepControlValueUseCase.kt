package com.dhanapal.rotarycontrol.domain.usecase

import com.dhanapal.rotarycontrol.domain.repository.Repository
import javax.inject.Inject

class GetStepControlValueUseCase @Inject constructor(private val repository: Repository) {
    suspend operator fun invoke(): Int {
        return repository.getInitialValueForStepControl()
    }
}