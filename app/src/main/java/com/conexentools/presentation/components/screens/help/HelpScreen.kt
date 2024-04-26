package com.conexentools.presentation.components.screens.help

import android.content.res.Configuration
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.conexentools.R
import com.conexentools.core.app.Constants
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
    Text(text = stringResource(R.string.help_screen_text))

    Spacer(modifier = Modifier.height(Constants.Dimens.Medium))

    Text(
      text = "🚨 AVISO 🚨",
      style = MaterialTheme.typography.headlineLarge,
      color = Color.Red
      )
    Text(text = stringResource(R.string.warning_message_part_1))
    Text(
      text = stringResource(R.string.warning_message_part_2),
      style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
    )
    Text(text = stringResource(R.string.warning_message_part_3))
  }
}

@Preview(apiLevel = 33)
@Preview(apiLevel = 33, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewHelpScreen() {
  PreviewComposable {
    HelpScreen { }
  }
}