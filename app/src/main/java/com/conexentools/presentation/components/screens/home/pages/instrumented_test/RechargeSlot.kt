package com.conexentools.presentation.components.screens.home.pages.instrumented_test

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RemoveCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.conexentools.core.app.Constants
import com.conexentools.core.util.moveFocusOnTabPressed
import com.conexentools.presentation.components.common.CubanPhoneNumberTextField

@Composable
fun RechargeSlot(
  number: String,
  recharge: String,
  onNumberChange: (String) -> Unit,
  onRechargeChange: (String) -> Unit,
  showRemoveButton: Boolean,
  onDelete: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .animateContentSize(),
    verticalAlignment = Alignment.Top
  ) {
//
    val focusManager = LocalFocusManager.current

    // Number
    CubanPhoneNumberTextField(
      modifier = Modifier.weight(3.5f),
      value = number,
      onValueChange = onNumberChange,
      isOutlinedTextField = false,
      focusDirection = FocusDirection.Right
    )

    Spacer(modifier = Modifier.width(Constants.Dimens.ExtraSmall))

    // Recharge
    var isInvalidRecharge by remember { mutableStateOf(false) }
    var rechargeSlotSupportingText: @Composable (() -> Unit)? by remember { mutableStateOf(null) }

    TextField(
      modifier = Modifier
        .weight(2.5f)
        .onFocusChanged { focusState ->
          if (!focusState.isFocused && recharge.isNotEmpty() && recharge.toInt() !in 25..1250) {
            isInvalidRecharge = true
            rechargeSlotSupportingText = { Text("La recarga debe estar entre 25 y 1250") }
          } else {
            rechargeSlotSupportingText = null
            isInvalidRecharge = false
          }
        }
        .moveFocusOnTabPressed(FocusDirection.Down, focusManager),
      supportingText = rechargeSlotSupportingText,
      isError = isInvalidRecharge,
      value = recharge,
      prefix = { Text("$", color = MaterialTheme.colorScheme.primary) },
      suffix = { Text("CUP", color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)) },
      onValueChange = {
        if (it.isDigitsOnly() && it.length <= 4)
          onRechargeChange(it)
      },
      keyboardOptions = KeyboardOptions.Default.copy(
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Next
      ),
      keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
      singleLine = true
    )

    Spacer(modifier = Modifier.width(Constants.Dimens.ExtraSmall))

    AnimatedVisibility(visible = showRemoveButton) {
      IconButton(
        onClick = {
          if (showRemoveButton) {
            focusManager.clearFocus()
            onDelete()
          }
        },
        modifier = Modifier.width(24.dp),
      ) {
        Icon(
          imageVector = Icons.Rounded.RemoveCircle,
          tint = Color.Red,
          contentDescription = null
        )
      }
    }
  }
}


