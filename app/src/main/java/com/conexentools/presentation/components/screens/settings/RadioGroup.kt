package com.conexentools.presentation.components.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.conexentools.core.app.Constants

@Composable
fun RadioGroup(
  items: Iterable<RadioButtonItem>,
  selected: Int,
  onItemSelect: ((Int) -> Unit)?,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.selectableGroup()
  ) {
    items.forEach { item ->
      RadioGroupItem(
        item = item,
        selected = selected == item.id,
        onClick = { onItemSelect?.invoke(item.id) },
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}

data class RadioButtonItem(
  val id: Int,
  val title: String,
)

@Composable
private fun RadioGroupItem(
  item: RadioButtonItem,
  selected: Boolean,
  onClick: ((Int) -> Unit)?,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier
      .selectable(
        selected = selected,
        onClick = { onClick?.invoke(item.id) },
        role = Role.RadioButton
      )
      .padding(Constants.Dimens.Medium),
    verticalAlignment = Alignment.CenterVertically
  ) {
    RadioButton(
      selected = selected,
      onClick = null,
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = item.title,
      style = MaterialTheme.typography.bodyMedium,
    )
  }
}