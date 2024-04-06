package com.conexentools.core.util

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.conexentools.presentation.theme.ConexenToolsTheme
import com.conexentools.presentation.theme.DarkTheme
import com.conexentools.presentation.theme.LocalTheme

@Composable
fun PreviewComposable(
  modifier: Modifier = Modifier,
  fillMaxSize: Boolean = true,
  content: @Composable (() -> Unit)? = null,
) {
  val darkTheme = DarkTheme(isSystemInDarkTheme())

  var m = modifier
  if (fillMaxSize)
    m = m.fillMaxSize()

  CompositionLocalProvider(LocalTheme provides darkTheme) {
    ConexenToolsTheme(darkTheme = darkTheme.isDark) {
      Surface(
        modifier = m,
        color = MaterialTheme.colorScheme.background
      ) {
        content?.invoke()
      }
    }
  }
}

@Preview(showBackground = true, apiLevel = 33)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, apiLevel = 33)
@Composable
fun TestingPreview() {
  PreviewComposable(fillMaxSize = false) {
    Column {

      Box(
        modifier = Modifier
          .width(50.dp)
          .height(20.dp)
          .background(MaterialTheme.colorScheme.secondary)
      )
      Box(
        modifier = Modifier
          .width(50.dp)
          .height(20.dp)
          .background(MaterialTheme.colorScheme.onSecondary)
      )
      Box(
        modifier = Modifier
          .width(50.dp)
          .height(20.dp)
          .background(MaterialTheme.colorScheme.secondaryContainer)
      )
      Box(
        modifier = Modifier
          .width(50.dp)
          .height(20.dp)
          .background(MaterialTheme.colorScheme.onSecondaryContainer)
      )
    }
  }
}