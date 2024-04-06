package com.conexentools.presentation.components.screens.home.pages.instrumented_test

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import com.conexentools.core.util.getImageByResourceID
import com.conexentools.core.util.moveFocusOnTabPressed
import com.conexentools.presentation.components.common.PrimaryIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MappedIconDropDownMenu(
  modifier: Modifier = Modifier,
  defaultItem: String,
  label: String,
  // Item : Resource ID
  items: Map<String, Int?>,
  onItemSelected: (String) -> Unit,
) {

  var isDropdownExpanded by remember { mutableStateOf(false) }
  val dropdownIndicatorRotation by animateFloatAsState(
    targetValue = if (isDropdownExpanded) 180f else 0f, label = label
  )
  var selectedItem by remember { mutableStateOf(defaultItem) }

  var leadingIconResourceID by remember {
    mutableStateOf(items[defaultItem])
  }

  val focusManager = LocalFocusManager.current

  var selectedItemOnTextFieldClicked by remember { mutableStateOf(selectedItem) }

  ExposedDropdownMenuBox(
    modifier = modifier,
    expanded = isDropdownExpanded,
    onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
  ) {
    OutlinedTextField(
      value = selectedItem,
      onValueChange = {},
      label = { Text(text = label) },
      modifier = Modifier
        .pointerInput(Unit) {
          awaitEachGesture {
            awaitFirstDown(pass = PointerEventPass.Initial)
            val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
            if (upEvent != null) {
              selectedItemOnTextFieldClicked = selectedItem
            }
          }
        }
        .moveFocusOnTabPressed(FocusDirection.Right, focusManager)
        .menuAnchor(),

      textStyle = MaterialTheme.typography.bodyMedium,
      singleLine = true,
      readOnly = true,
      leadingIcon = leadingIconResourceID?.let {
        getImageByResourceID(
          resourceID = it,
          tintColor = MaterialTheme.colorScheme.primary
        )
      },
      trailingIcon = {
        PrimaryIconButton(
          imageVector = Icons.Default.ArrowDropDown,
          modifier = Modifier.rotate(dropdownIndicatorRotation)
        )
      },
//      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
      keyboardOptions = KeyboardOptions.Default.copy(
        imeAction = ImeAction.Next,
      ),
      keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Right) }),
    )

    ExposedDropdownMenu(
      expanded = isDropdownExpanded,
      onDismissRequest = { isDropdownExpanded = false }
    ) {

      items.forEach { item ->
        if (item.key != selectedItemOnTextFieldClicked) {
          DropdownMenuItem(
            text = { Text(item.key) },
            leadingIcon = item.value?.let {
              getImageByResourceID(
                resourceID = it,
                tintColor = MaterialTheme.colorScheme.primary
              )
            },
            onClick = {
              selectedItem = item.key
              onItemSelected(item.key)
              leadingIconResourceID = item.value
              isDropdownExpanded = false
            }
          )
        }
      }
    }
  }
}
