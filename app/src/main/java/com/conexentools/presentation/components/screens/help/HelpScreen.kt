package com.conexentools.presentation.components.screens.help

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.conexentools.core.util.PreviewComposable
import com.conexentools.presentation.components.common.ScreenSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
  onNavigateBack: () -> Unit,
  ) {
  ScreenSurface(
    title = "Ayuda",
    lazyColumnModifier = Modifier.fillMaxHeight(),
    onNavigateBack = onNavigateBack
  ) {
    Text(text = "Aquí se supone que iba algún material explicando minuciosamente el funcionamiento de cada parte de la aplicación pero me dio mucho pereza la verdad ponerme a hacer todo eso, cosas que pasan")
  }
}

@Preview(apiLevel = 33)
@Preview(apiLevel = 33, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewHelpScreen() {
  PreviewComposable {
    HelpScreen { }
  }
}