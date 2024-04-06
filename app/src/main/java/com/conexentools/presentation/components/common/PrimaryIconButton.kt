package com.conexentools.presentation.components.common

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun PrimaryIconButton(
  imageVector: ImageVector,
  enabled: Boolean = true,
  modifier: Modifier = Modifier,
  onClick: () -> Unit = {},
) {
  IconButton(
    modifier = modifier,
    onClick = onClick,
    enabled = enabled,
  ) {
    Icon(
      imageVector = imageVector,
      tint = MaterialTheme.colorScheme.primary.copy(alpha = if (enabled) 1f else 0.4f),
      contentDescription = null,
    )
  }
}