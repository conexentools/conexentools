package com.conexentools.presentation.components.common

import android.content.res.Configuration
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.conexentools.core.util.PreviewComposable
import com.conexentools.core.util.cubanMobileNumberFilter
import com.conexentools.core.util.log
import com.conexentools.core.util.moveFocusOnTabPressed
import com.conexentools.presentation.theme.LocalTheme

fun String.cleanNumberString(): String = this
  .replace(" ", "")
  .replace("-", "")
  .removePrefix("+")

fun String.cleanCubanMobileNumber(): String {
  var number = this.cleanNumberString()
  if (number.isDigitsOnly() && number.length == 10)
    number = number.removePrefix("53")
  return number
}

@Composable
fun CubanPhoneNumberTextField(
  modifier: Modifier = Modifier,
  value: String,
  onValueChange: (String) -> Unit = {},
  isOutlinedTextField: Boolean,
  placeholder: @Composable (() -> Unit)? = { Text("Número") },
  label: @Composable (() -> Unit)? = placeholder,
  focusDirection: FocusDirection = FocusDirection.Down,
  onNext: (KeyboardActionScope.() -> Unit)? = null,
  onDone: (KeyboardActionScope.() -> Unit)? = null,
) {

  val focusManager = LocalFocusManager.current

  var isInvalidNumber by remember { mutableStateOf(false) }
  var supportingText: @Composable (() -> Unit)? by remember { mutableStateOf(null) }
  val _onValueChange: (String) -> Unit = {
    val v = it.cleanCubanMobileNumber()
    if (v.isDigitsOnly() && v.length <= 8) {
      onValueChange(v)
    }
    if (isInvalidNumber) {
      isInvalidNumber = false
      supportingText = null
    }
  }
  val prefix: @Composable (() -> Unit) = { Text("+53") }
  val leadingIcon: @Composable (() -> Unit) = {
    Icon(
      imageVector = Icons.Rounded.Phone,
      tint = MaterialTheme.colorScheme.primary,
      contentDescription = null
    )
  }

  val mod: Modifier = Modifier
    .onFocusChanged { focusState ->
      if (!focusState.isFocused && value.isNotEmpty() && value.length != 8) {
        isInvalidNumber = true
        supportingText = { Text("8 dígitos requeridos") }
      } else {
        supportingText = null
        isInvalidNumber = false
      }
    }
    .moveFocusOnTabPressed(focusDirection, focusManager)
    .defaultMinSize(minWidth = 200.dp)
    .then(modifier)

  val keyboardOptions = KeyboardOptions.Default.copy(
    keyboardType = KeyboardType.Number,
    imeAction = if (onDone != null) ImeAction.Done else ImeAction.Next
  )
  val keyboardActions = KeyboardActions(
    onNext = {
      focusManager.moveFocus(focusDirection)
      if (onNext != null) {
        onNext()
      }
    },
    onDone = onDone
  )
  val darkTheme = LocalTheme.current.isDark

  if (isOutlinedTextField) {
    OutlinedTextField(
      modifier = mod,
      value = value,
      isError = isInvalidNumber,
      supportingText = supportingText,
      onValueChange = _onValueChange,
      placeholder = placeholder,
      prefix = prefix,
      leadingIcon = leadingIcon,
      keyboardOptions = keyboardOptions,
      keyboardActions = keyboardActions,
      visualTransformation = { cubanMobileNumberFilter(it, darkTheme = darkTheme) },
      label = label,
      singleLine = true
    )
  } else {
    TextField(
      modifier = mod,
      value = value,
      leadingIcon = leadingIcon,
      isError = isInvalidNumber,
      supportingText = supportingText,
      onValueChange = _onValueChange,
      placeholder = placeholder,
      prefix = prefix,
      keyboardOptions = keyboardOptions,
      keyboardActions = keyboardActions,
      singleLine = true,
      visualTransformation = { cubanMobileNumberFilter(it, darkTheme = darkTheme) }
    )
  }
}

@Preview(showBackground = true, apiLevel = 33)
@Preview(showBackground = true, apiLevel = 33, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenPreview() {
  PreviewComposable(
    fillMaxSize = false,
  ) {
    CubanPhoneNumberTextField(
      value = "55797140",
      onValueChange = {},
      isOutlinedTextField = true,
      placeholder = null,
      label = null,
      focusDirection = FocusDirection.Down,
      onNext = {},
      onDone = {}
    )
  }
}
