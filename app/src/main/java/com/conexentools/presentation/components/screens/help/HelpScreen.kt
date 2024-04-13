package com.conexentools.presentation.components.screens.help

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.conexentools.core.util.PreviewComposable
import com.conexentools.presentation.components.common.ScreenSurface

@Composable
fun HelpScreen(
  onNavigateBack: () -> Unit,
  ) {
  ScreenSurface(
    title = "Ayuda",
    lazyColumnModifier = Modifier.fillMaxHeight(),
    onNavigateBack = onNavigateBack
  ) {
    Text(
      text = "Test Instrumentado",
      style = MaterialTheme.typography.headlineSmall
    )

    Text(
      text = "Lista de Clientes",
      style = MaterialTheme.typography.headlineSmall
    )
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