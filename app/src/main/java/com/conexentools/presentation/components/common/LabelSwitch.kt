package com.conexentools.presentation.components.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun LabelSwitch(
  label: String,
  checked: MutableState<Boolean>,
  onCheckedChange: ((Boolean) -> Unit)? = null,
) {

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = {
        checked.value = !checked.value
        onCheckedChange?.invoke(checked.value)
      }),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {

    Text(
      text = label,
      style = MaterialTheme.typography.titleMedium
    )

    Switch(
      colors = SwitchDefaults.colors(
        checkedThumbColor = MaterialTheme.colorScheme.primary,
        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
        uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
        uncheckedTrackColor = MaterialTheme.colorScheme.secondaryContainer,
      ),
      checked = checked.value,
      onCheckedChange = {
        checked.value = it
        onCheckedChange?.invoke(it)
      }
    )
  }
}