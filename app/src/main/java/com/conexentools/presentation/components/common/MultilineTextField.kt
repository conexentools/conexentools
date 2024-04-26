package com.conexentools.presentation.components.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MultilineTextField(
  value: String,
  onValueChange: (String) -> Unit,
  onDeleteText: () -> Unit,
  label: String? = null,
  placeholder: String? = null,
) {
  val trailingIcon = @Composable {
    IconButton( onClick = onDeleteText) {
      Icon(
        imageVector = Icons.Rounded.Cancel,
        contentDescription = null
      )
    }
  }

  TextField(
    value = value,
    modifier = Modifier.fillMaxWidth(),
    trailingIcon = if (value.isEmpty()) null else trailingIcon,
    label = label?.let { { Text(it) } },
    placeholder = placeholder?.let { { Text(it) } },
    singleLine = false,
    onValueChange = onValueChange
  )
}