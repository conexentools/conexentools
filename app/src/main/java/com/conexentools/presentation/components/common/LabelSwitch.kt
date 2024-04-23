package com.conexentools.presentation.components.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import com.conexentools.core.app.Constants

@Composable
fun LabelSwitch(
  label: String,
  info: String? = null,
  checked: MutableState<Boolean>,
  onCheckedChange: ((Boolean) -> Unit)? = null,
) {

  var showInfoText by remember {
    mutableStateOf(false)
  }

  if (showInfoText) {
    ScrollableAlertDialog (
      text = info!!,
      onDismissRequest = { showInfoText = false },
      onConfirm = { showInfoText = false }
    )
  }

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

    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.titleMedium
      )
      if (!info.isNullOrEmpty()){
        PrimaryIconButton(
          imageVector = Icons.Rounded.Info,
          modifier = Modifier
            .padding(Constants.Dimens.Small)
            .alpha(0.5f),
          onClick = { showInfoText = true }
        )
      }
    }

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