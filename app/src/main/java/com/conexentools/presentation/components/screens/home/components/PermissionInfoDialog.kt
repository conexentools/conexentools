package com.conexentools.presentation.components.screens.home.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
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
fun PermissionInfoDialog(
  text: String,
  onOkButtonClicked: () -> Unit
) {
  AlertDialog(
    title = {
      Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
      ) {
        Icon(
          imageVector = Icons.Rounded.Info,
          tint = MaterialTheme.colorScheme.primary,
          contentDescription = null,
          modifier = Modifier.size(35.dp)
        )
      }
    },
    onDismissRequest = { },
    confirmButton = {
      TextButton(onOkButtonClicked) {
        Text("OK")
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
fun PreviewPermissionsInfoDialog() {
  PreviewComposable(fillMaxSize = true) {
    PermissionInfoDialog(text = "How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.How gutless. You taste like a wind.") {

    }
  }
}