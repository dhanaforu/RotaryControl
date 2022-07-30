package com.dhanapal.rotarycontrol.presentation.home

import android.os.Bundle
import android.os.Handler
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.dhanapal.rotarycontrol.R
import com.dhanapal.rotarycontrol.presentation.widgets.RotaryControlWidget
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private var stepRotaryControl: RotaryControlWidget? = null
    private var freeRotaryControl: RotaryControlWidget? = null

    private val viewModel by viewModels<HomeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        stepRotaryControl = findViewById(R.id.stepRotaryControl)
        stepRotaryControl?.listener = object : RotaryControlWidget.RotaryControlWidgetListener {
            override fun onRotate(value: Int, displayText: String) {
                viewModel.updateStepControlValue(value)
            }
        }

        freeRotaryControl = findViewById(R.id.freeRotaryControl)
        freeRotaryControl?.listener = object : RotaryControlWidget.RotaryControlWidgetListener {
            override fun onRotate(value: Int, displayText: String) {
                viewModel.updateFreeControlValue(value)
            }
        }

        observeValues()

        viewModel.fetchInitialValues()
    }

    private fun observeValues() {
        viewModel.freeControlValue.observe(this) { value ->
            println("print freeControlValue - " + value)
            if (value != null) {
                freeRotaryControl?.setInitialValue(value)
            }
        }

        viewModel.stepControlValue.observe(this) { value ->
            println("print stepControlValue - " + value)

            if (value != null) {
                stepRotaryControl?.setInitialValue(value)
            }
        }
    }
}