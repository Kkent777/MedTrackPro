package com.chenglin.medtrackpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.chenglin.medtrackpro.data.seed.DatabaseSeeder
import com.chenglin.medtrackpro.navigation.MedTrackNavGraph
import com.chenglin.medtrackpro.ui.theme.MedTrackProTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MedTrackProTheme {
                var isSeedReady by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    DatabaseSeeder.seedIfNeeded(applicationContext)
                    isSeedReady = true
                }

                if (isSeedReady) {
                    MedTrackNavGraph()
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}
//MainActivity 只是入口
//不负责业务逻辑
//真正页面流程交给导航层
