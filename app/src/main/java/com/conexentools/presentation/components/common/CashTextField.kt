package com.conexentools.presentation.components.common

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.core.text.isDigitsOnly
import com.conexentools.core.util.log
import com.conexentools.core.util.moveFocusOnTabPressed

@Composable
fun CashTextField(
  modifier: Modifier = Modifier,
  value: String,
  onValueChange: (String) -> Unit,
  minValue: Int = 0,
  maxValue: Int? = null,
  label: String? = null
) {
  var isInvalidEntry by remember { mutableStateOf(false) }
  var supportingText: @Composable (() -> Unit)? by remember { mutableStateOf(null) }
  val focusManager = LocalFocusManager.current

  val maxValue_ = maxValue ?: 1000000

  fun isValidEntry() =
    value.isEmpty() || (value.isDigitsOnly() && value.take(maxValue_.toString().length).toInt() in minValue..maxValue_)

  TextField(
    modifier = Modifier
      .onFocusChanged { focusState ->
        if (!focusState.isFocused && !isValidEntry()) {
          isInvalidEntry = true
          supportingText = { Text("El valor debe estar entre $minValue y $maxValue_") }
        } else {
          supportingText = null
          isInvalidEntry = false
        }
      }
      .moveFocusOnTabPressed(FocusDirection.Down, focusManager)
      .then(modifier),
    supportingText = supportingText,
    isError = isInvalidEntry,
    value = value,
    prefix = { Text("$", color = MaterialTheme.colorScheme.primary) },
    suffix = { Text("CUP", color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)) },
    onValueChange = {
      if (it.isDigitsOnly())
        onValueChange(it)
    },
    label = label?.let { { Text(it) } },
    textStyle = MaterialTheme.typography.bodyLarge,
    keyboardOptions = KeyboardOptions.Default.copy(
      keyboardType = KeyboardType.Number,
      imeAction = ImeAction.Next
    ),
    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
    singleLine = true,
  )
}