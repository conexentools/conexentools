package com.conexentools.presentation.components.common

import android.content.res.Configuration
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.conexentools.core.util.PreviewComposable

@Composable
fun ScrollableAlertDialog(
  text: String = "",
  isInfoDialog: Boolean? = true,
  title: String? = null,
  confirmButtonText: String = "OK",
  yesNoDialog: Boolean = false,
  onDismiss: (() -> Unit)? = null,
  onDismissRequest: () -> Unit = {},
  onConfirm: () -> Unit
) {
  AlertDialog(
    icon = isInfoDialog?.let {
      {
        Icon(
          imageVector = if (it) Icons.Rounded.Info else Icons.Rounded.Warning,
          tint = MaterialTheme.colorScheme.primary,
          contentDescription = null,
          modifier = Modifier.size(35.dp)
        )
      }
    },
    title = title?.let { { Text(it) } },
    onDismissRequest = onDismissRequest,
    confirmButton = {
      TextButton(onConfirm) {
        Text(if (yesNoDialog) "Si" else confirmButtonText)
      }
    },
    dismissButton = {
      if (onDismiss != null) {
        TextButton(onDismiss) {
          Text(if (yesNoDialog) "No" else "Cancelar")
        }
      }
    },
    text = {
      LazyColumn {
        item {
          Text(
            text = text,
            style = MaterialTheme.typography.titleMedium
          )
        }
      }
    }
  )
}

@Preview(showBackground = true, apiLevel = 33)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, apiLevel = 33)
@Composable
private fun PreviewPermissionsInfoDialog() {
  PreviewComposable(fillMaxSize = true) {
    ScrollableAlertDialog(
      text = "How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.",
      isInfoDialog = false,
      yesNoDialog = false,
      onConfirm = {},
      onDismiss = null
    )
  }
}