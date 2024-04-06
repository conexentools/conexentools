package com.conexentools.presentation.components.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.conexentools.core.util.cubanCardNumberFilter
import com.conexentools.core.util.moveFocusOnTabPressed
import com.conexentools.core.util.textFilter
import com.conexentools.presentation.theme.LocalTheme
import kotlin.math.min

@Composable
fun CreditCardTextField(
  modifier: Modifier = Modifier,
  value: String,
//  valueGetter: () -> String,
  onValueChange: (String) -> Unit,
  isFourDigitsCard: Boolean = false,
  trailingIcon: @Composable (() -> Unit)? = null,
  focusDirection: FocusDirection = FocusDirection.Down,
  onNext: (KeyboardActionScope.() -> Unit)? = null,
  onDone: (KeyboardActionScope.() -> Unit)? = null,
) {

  val focusManager = LocalFocusManager.current
  val digits = if (isFourDigitsCard) 4 else 16

//  var cardNumber by remember {
//    mutableStateOf(valueGetter())
//  }
//  var cardNumber by remember { mutableStateOf(value) }
  var supportingText: @Composable (() -> Unit)? by remember { mutableStateOf(null) }
  var isInvalidCard by remember { mutableStateOf(false) }
  OutlinedTextField(
    modifier = modifier
      .fillMaxWidth()
      .onFocusChanged { focusState ->
        if (!focusState.isFocused && value.isNotEmpty() && value.length != digits) {
          isInvalidCard = true
          supportingText = { Text("$digits dígitos requeridos") }
        } else {
          supportingText = null
          isInvalidCard = false
        }
      }
      .moveFocusOnTabPressed(focusDirection, focusManager),
    label = { Text("Tarjeta") },
    supportingText = supportingText,
    isError = isInvalidCard,
    leadingIcon = {
      Icon(
        imageVector = Icons.Rounded.CreditCard,
        tint = MaterialTheme.colorScheme.primary,
        contentDescription = null
      )
    },
    trailingIcon = trailingIcon,
    value = value,
//    placeholder = { Text("XXXX-XXXX-XXXX-XXXX") },
    onValueChange = {
      var v = it.cleanNumberString()
      if (v.isDigitsOnly()) {
        if (v.length > digits)
          v = v.take(digits)
        onValueChange(v)
//        value = v
      }
    },
    visualTransformation = LocalTheme.current.isDark.let { darkTheme ->
      { annotatedString ->
        if (isFourDigitsCard)
          textFilter(
            text = annotatedString,
            mask = "XXXX",
            separator = ' ',
            darkTheme = darkTheme,
            transformedToOriginalOffsetTranslator = { min(it, value.length) },
//            originalToTransformedOffsetTranslator = { offset -> min(offset, it.text.length) }
          )
        else
          cubanCardNumberFilter(annotatedString, darkTheme = darkTheme)
      }
    },
    keyboardOptions = KeyboardOptions.Default.copy(
      keyboardType = KeyboardType.Number,
      imeAction = if (onDone != null) ImeAction.Done else ImeAction.Next
    ),
    keyboardActions = KeyboardActions(
      onNext = {
        focusManager.moveFocus(focusDirection)
        if (onNext != null) {
          onNext()
        }
      },
      onDone = onDone
    ),
    singleLine = true
  )
}