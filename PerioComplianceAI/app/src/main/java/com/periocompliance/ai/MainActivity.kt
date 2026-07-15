package com.periocompliance.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.periocompliance.ai.navigation.PerioNavHost
import com.periocompliance.ai.ui.theme.PerioComplianceTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            PerioComplianceTheme {
                PerioNavHost()
            }
        }
    }
}
