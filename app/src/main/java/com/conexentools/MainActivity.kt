package com.conexentools

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.conexentools.core.util.log
import com.conexentools.domain.repository.AndroidUtils
import com.conexentools.presentation.HomeScreenViewModel
import com.conexentools.presentation.components.common.enums.AppTheme
import com.conexentools.presentation.navigation.Screen
import com.conexentools.presentation.navigation.SetUpNavGraph
import com.conexentools.presentation.theme.ConexenToolsTheme
import com.conexentools.presentation.theme.DarkTheme
import com.conexentools.presentation.theme.LocalTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  @Inject
  lateinit var au: AndroidUtils

  @SuppressLint("InlinedApi")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    lateinit var navController: NavHostController

    setContent {

      val hvm: HomeScreenViewModel = hiltViewModel()

      val isSystemInDarkTheme = isSystemInDarkTheme()
      val darkTheme by remember {
        derivedStateOf {
          when (hvm.appTheme.value) {
            AppTheme.MODE_AUTO -> DarkTheme(isSystemInDarkTheme)
            AppTheme.MODE_DAY -> DarkTheme(false)
            AppTheme.MODE_NIGHT -> DarkTheme(true)
          }
        }
      }

      CompositionLocalProvider(LocalTheme provides darkTheme) {
        ConexenToolsTheme(
          darkTheme = darkTheme.isDark
        ) {
          Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
          ) {
            navController = rememberNavController()
            SetUpNavGraph(
              navController = navController,
              startDestination = Screen.Home,
              au = au
            )
          }
        }
      }
    }
  }
}
