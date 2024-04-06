package com.conexentools.presentation.components.screens.home.pages.instrumented_test

import android.Manifest
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Contacts
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.conexentools.R
import com.conexentools.core.app.Constants
import com.conexentools.core.util.moveFocusOnTabPressed
import com.conexentools.core.util.toFormattedDate
import com.conexentools.domain.repository.AndroidUtils
import com.conexentools.presentation.components.common.CreditCardTextField
import com.conexentools.presentation.components.common.LabelSwitch
import com.conexentools.presentation.components.common.PrimaryIconButton

@Composable
fun InstrumentedTestPage(
  onPickContact: () -> Unit,
  au: AndroidUtils,
  // States
  firstClientNumber: MutableState<String?>,
  secondClientNumber: MutableState<String?>,
  firstClientRecharge: MutableState<String?>,
  secondClientRecharge: MutableState<String?>,
  fetchDataFromWA: MutableState<Boolean>,
  pin: MutableState<String>,
  bank: MutableState<String>,
  cardLast4Digits: MutableState<String>,
  waContactImageUri: MutableState<Uri?>,
  rechargesAvailabilityDateISOString: MutableState<String?>,
  waContact: MutableState<String>,
  paddingValues: PaddingValues,
  maxPinLength: MutableIntState
) {
  // Main Column
  Column(
    modifier = Modifier
      .fillMaxSize()
//          .verticalScroll(scrollState)
      .padding(
//        paddingValues
        PaddingValues(
          start = Constants.Dimens.Large,
          top = paddingValues.calculateTopPadding() + Constants.Dimens.Small,
          end = Constants.Dimens.Large,
          bottom = Constants.Dimens.Large,
        )
      ),
    verticalArrangement = Arrangement.spacedBy(4.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {

    var isPinVisible by remember { mutableStateOf(false) }
    val pinVisibilityTintColor by animateColorAsState(
      targetValue = MaterialTheme.colorScheme.primary.copy(alpha = if (isPinVisible) 1f else 0.4f),
      animationSpec = tween(650),
      label = ""
    )

    var isWrongPin by remember { mutableStateOf(false) }
    var pinTextFieldSupportingText: @Composable (() -> Unit)? by remember { mutableStateOf(null) }

    Row(modifier = Modifier.fillMaxWidth()) {
      // Bank
      key(bank.value) {
        MappedIconDropDownMenu(
          defaultItem = bank.value,
          label = "Banco",
          items = mapOf(
            "Bandec" to R.drawable.logo_bandec,
            "BPA" to R.drawable.logo_bpa,
            "Metropolitano" to R.drawable.logo_metro,
          ),
          onItemSelected = {
            bank.value = it
            maxPinLength.intValue = if (it == "Metropolitano") 4 else 5
          },
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.width(Constants.Dimens.Small))

      val focusManager = LocalFocusManager.current

      // PIN
      OutlinedTextField(value = pin.value,
        onValueChange = {
          if (it.length <= maxPinLength.intValue) pin.value = it
        },
        supportingText = pinTextFieldSupportingText,
        label = { Text(text = "PIN") },
        isError = isWrongPin,
        modifier = Modifier
          .weight(0.7f)
          .onFocusChanged { focusState ->
            if (!focusState.isFocused && pin.value.isNotEmpty() && pin.value.length != maxPinLength.intValue) {
              isWrongPin = true
              pinTextFieldSupportingText =
                { Text("El PIN debe tener ${maxPinLength.intValue} dígitos") }
            } else {
              isWrongPin = false
              pinTextFieldSupportingText = null
            }
          }
          .moveFocusOnTabPressed(FocusDirection.Down, focusManager),
        keyboardOptions = KeyboardOptions.Default.copy(
          imeAction = ImeAction.Next, keyboardType = KeyboardType.Number
        ),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
        singleLine = true,
        trailingIcon = {
          IconButton(
            onClick = { isPinVisible = !isPinVisible },
          ) {
            Icon(
              painter = painterResource(id = com.google.android.material.R.drawable.design_ic_visibility),
              tint = pinVisibilityTintColor,
              contentDescription = stringResource(R.string.pin_visibility_icon_content_description)
            )
          }
        },
        visualTransformation = if (isPinVisible) VisualTransformation.None
        else PasswordVisualTransformation()
      )
    }

    var showCardLast4DigitsInfoDialog by remember { mutableStateOf(false) }

    if (showCardLast4DigitsInfoDialog) {
      AlertDialog(
        onDismissRequest = {
          showCardLast4DigitsInfoDialog = false
        },
        text = {
          Text(
            text = "Últimos 4 dígitos de la tarjeta en CUP que vaya a usar para hacer la recarga. Deje sin especificar si tiene una sola tarjeta asociada al banco seleccionado."
          )
        },
        confirmButton = {
          TextButton(onClick = {
            showCardLast4DigitsInfoDialog = false
          }) {
            Text("OK")
          }
        },
      )
    }

    // Card Last 4 Digits
    CreditCardTextField(onValueChange = { cardLast4Digits.value = it },
      value = cardLast4Digits.value,
      isFourDigitsCard = true,
      trailingIcon = {
        IconButton(onClick = {
          showCardLast4DigitsInfoDialog = true
        }) {
          Icon(
            imageVector = Icons.Rounded.Info,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            contentDescription = null
          )
        }
      })

    // Fetch Data from WA Switch
    LabelSwitch(
      label = stringResource(R.string.data_from_wa_switch_label),
      checked = fetchDataFromWA,
    )

    AnimatedVisibility(visible = fetchDataFromWA.value) {
      var leadingIcon: @Composable (() -> Unit)? by remember {
        mutableStateOf(
          null
        )
      }

      leadingIcon = if (waContactImageUri.value != null) {
        {
          AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(waContactImageUri.value)
              .build(),
            placeholder = painterResource(R.drawable.contact_image_placeholder),
            contentDescription = null,
            modifier = Modifier
              .padding(Constants.Dimens.Small)
              .size(40.dp)
              .clip(CircleShape),
            contentScale = ContentScale.Crop,
          )
        }
      } else null

      // WA Contact
      OutlinedTextField(
        leadingIcon = leadingIcon,
        value = waContact.value,
        colors = TextFieldDefaults.colors(
          focusedIndicatorColor = MaterialTheme.colorScheme.primary,
          unfocusedIndicatorColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        onValueChange = { waContact.value = it },
        label = { Text("Contacto WA") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        trailingIcon = {
          PrimaryIconButton(Icons.Rounded.Contacts) {
            if (!au.isPermissionGranted(Manifest.permission.READ_CONTACTS))
              au.toast(
                message = "Permiso para leer contactos requerido",
                vibrate = true
              )
            else onPickContact()
          }
        },
      )
    }

    AnimatedVisibility(visible = !fetchDataFromWA.value) {
      LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Constants.Dimens.ExtraSmall)
      ) {
        item {
          Text(
            text = "Recargas",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(0.dp, 2.dp),
            fontWeight = FontWeight.Bold
          )
        }

        fun showSecondClientSlot() = secondClientNumber.value != null

        item {
          RechargeSlot(
            number = firstClientNumber,
            recharge = firstClientRecharge,
            showRemoveButton = showSecondClientSlot(),
            onDelete = {
              firstClientNumber.value = secondClientNumber.value
              firstClientRecharge.value = secondClientRecharge.value
              secondClientNumber.value = null
              secondClientRecharge.value = null
            }
          )
        }

        item {
          AnimatedVisibility(visible = showSecondClientSlot()) {
            RechargeSlot(
              number = secondClientNumber,
              recharge = secondClientRecharge,
              showRemoveButton = true,
              onDelete = {
                secondClientNumber.value = null
                secondClientRecharge.value = null
              }
            )
          }
        }
      }
    }
  }

  if (rechargesAvailabilityDateISOString.value != null) {
    Spacer(modifier = Modifier.height(Constants.Dimens.ExtraSmall))
    Text(
      text = "Próximas recargas disponibles el:",
    )
    Text(
      text = rechargesAvailabilityDateISOString.value!!.toFormattedDate(),
      style = TextStyle.Default.copy(
        fontWeight = FontWeight.Bold
      )
    )
  }
}