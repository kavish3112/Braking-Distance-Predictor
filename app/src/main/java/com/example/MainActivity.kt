package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.MainAppScaffold
import com.example.ui.theme.BrakingPredictorTheme
import com.example.ui.viewmodel.BrakingViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: BrakingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BrakingPredictorTheme {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }
}
