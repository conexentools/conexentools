package com.conexentools.presentation.components.screens.home.pages.instrumented_test

import android.Manifest
import android.annotation.SuppressLint
import android.content.res.Configuration
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.CardMembership
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Contacts
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.conexentools.BuildConfig
import com.conexentools.R
import com.conexentools.core.app.Constants
import com.conexentools.core.util.PreviewComposable
import com.conexentools.core.util.RootUtil
import com.conexentools.core.util.moveFocusOnTabPressed
import com.conexentools.core.util.pickContact
import com.conexentools.core.util.toFormattedDate
import com.conexentools.domain.repository.AndroidUtils
import com.conexentools.presentation.components.common.LabelSwitch
import com.conexentools.presentation.components.common.PrimaryIconButton
import com.conexentools.presentation.components.common.ScrollableAlertDialog
import contacts.core.util.phoneList

@SuppressLint("PrivateResource")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InstrumentedTestPage(
  au: AndroidUtils,
  whatsAppInstalledVersion: Pair<Long, String>?,
  transfermovilInstalledVersion: Pair<Long, String>?,
  instrumentationAppInstalledVersion: Pair<Long, String>?,
  // States
  firstClientNumber: MutableState<String>,
  secondClientNumber: MutableState<String?>,
  firstClientRecharge: MutableState<String>,
  secondClientRecharge: MutableState<String>,
  fetchDataFromWA: MutableState<Boolean>,
  pin: MutableState<String>,
  bank: MutableState<String>,
  cardToUseAlias: MutableState<String>,
  waContactImageUri: MutableState<Uri?>,
  rechargesAvailabilityDateISOString: MutableState<String?>,
  waContact: MutableState<String>,
) {

  val pickContactLauncher = pickContact(au = au) { contact ->
    if (contact != null) {
      val number =
        if (contact.phoneList().isNotEmpty()) contact.phoneList().first().number else null
      waContact.value = number ?: contact.displayNamePrimary ?: "sin nombre :("
      waContactImageUri.value = contact.photoThumbnailUri
    }
  }

  var maxPinLength by remember { mutableIntStateOf(if (bank.value == "Metropolitano") 4 else 5) }

  var isWrongPin by remember { mutableStateOf(false) }
  var pinTextFieldSupportingText: @Composable (() -> Unit)? by remember { mutableStateOf(null) }

  // Main Column
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(Constants.Dimens.Medium),
    verticalArrangement = Arrangement.spacedBy(Constants.Dimens.ExtraSmall),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {

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
            maxPinLength = if (it == "Metropolitano") 4 else 5
          },
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.width(Constants.Dimens.Small))

      val focusManager = LocalFocusManager.current

      // PIN
      OutlinedTextField(
        value = pin.value,
        onValueChange = {
          if (it.length <= maxPinLength) pin.value = it
        },
        supportingText = pinTextFieldSupportingText,
        label = { Text(text = "PIN") },
        isError = isWrongPin,
        modifier = Modifier
          .weight(0.7f)
          .onFocusChanged { focusState ->
            if (!focusState.isFocused && pin.value.isNotEmpty() && pin.value.length != maxPinLength) {
              isWrongPin = true
              pinTextFieldSupportingText =
                { Text("El PIN debe tener $maxPinLength dígitos") }
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
        visualTransformation = PasswordVisualTransformation()
      )
    }

    var showCardToUseAliasInfoDialog by remember { mutableStateOf(false) }

    if (showCardToUseAliasInfoDialog) {
      ScrollableAlertDialog(
        text = "Alias o texto parcial del alias de la tarjeta que vaya a usar para hacer la recarga|transferencia. Por ejemplo si su tarjeta se llama 'BANCO METROPOLITANO - 1234', puede poner el alias completo o solo 1234. Deje en blanco si tiene una sola tarjeta",
        isInfoDialog = true,
        title = null,
        yesNoDialog = false,
        onDismiss = null,
        onDismissRequest = { showCardToUseAliasInfoDialog = false },
        onConfirm = { showCardToUseAliasInfoDialog = false }
      )
    }

    // Alias of card to use
    OutlinedTextField(
      value = cardToUseAlias.value,
      onValueChange = {
        cardToUseAlias.value = it
      },
      label = {
        Text("Alias de la tarjeta a usar")
      },
      leadingIcon = {
        Icon(
          imageVector = Icons.Rounded.CardMembership,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary
        )
      },
      trailingIcon = {
        PrimaryIconButton(
          imageVector = Icons.Rounded.Info,
          modifier = Modifier
            .padding(Constants.Dimens.Small)
            .alpha(0.5f),
          onClick = { showCardToUseAliasInfoDialog = true }
        )
      },
      modifier = Modifier.fillMaxWidth(),
      keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
      maxLines = 1,
    )

    // Fetch Data from WA Switch
    LabelSwitch(
      label = "Obtener datos desde WA",
      info = "Active esta opción para buscar los datos de recarga en los últimos chats enviados por el contacto especificado, los cuales deben estar en el siguiente formato <numero_a_recargar>,<recarga> y ser visibles en la pantalla de conversación. Por ejemplo: \n\n55123456,1000\n55654321,500\n\nEl contacto debe ser el primer resultado en aparecer en la lista de contactos de WhatsApp cuando se introduzca en la barra de búsqueda el texto especificado como contacto, así que asegúrese de eso, de lo contrario la prueba automatizada fallará",
      checked = fetchDataFromWA
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
            else pickContactLauncher.launch(null)
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
            modifier = Modifier.padding(0.dp, Constants.Dimens.MegaSmall),
            fontWeight = FontWeight.Bold
          )
        }

        item {
          RechargeSlot(
            number = firstClientNumber.value,
            recharge = firstClientRecharge.value,
            showRemoveButton = secondClientNumber.value != null,
            onNumberChange = { firstClientNumber.value = it },
            onRechargeChange = { firstClientRecharge.value = it },
            onDelete = {
              if (secondClientNumber.value != null) {
                firstClientNumber.value = secondClientNumber.value!!
                firstClientRecharge.value = secondClientRecharge.value
                secondClientNumber.value = null
                secondClientRecharge.value = ""
              }
            }
          )
        }

        item {
          AnimatedVisibility(visible = secondClientNumber.value != null) {//showSecondClientSlot()) {
            RechargeSlot(
              number = secondClientNumber.value ?: "",
              recharge = secondClientRecharge.value,
              onNumberChange = { secondClientNumber.value = it },
              onRechargeChange = { secondClientRecharge.value = it },
              showRemoveButton = true,
              onDelete = {
                secondClientNumber.value = null
                secondClientRecharge.value = ""
              }
            )
          }
        }
      }
    }

    if (!rechargesAvailabilityDateISOString.value.isNullOrEmpty()) {

      var showDialog by remember { mutableStateOf(false) }

      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.combinedClickable(
          onLongClick = { showDialog = true },
          onClick = {}
        )
      ) {
        Spacer(modifier = Modifier.height(Constants.Dimens.ExtraSmall))
        Text(
          text = "Próximas recargas disponibles el: ",
          style = MaterialTheme.typography.bodyMedium
        )
        Text(
          text = rechargesAvailabilityDateISOString.value!!.toFormattedDate(),
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
        )
      }

      if (showDialog) {
        ScrollableAlertDialog(
          text = "Restaurar la fecha de disponibilidad de las recargas?",
          isInfoDialog = false,
          yesNoDialog = true,
          onConfirm = {
            showDialog = false
            rechargesAvailabilityDateISOString.value = null
          },
          onDismiss = { showDialog = false },
          onDismissRequest = { showDialog = false }
        )
      }
    }

    Spacer(modifier = Modifier.weight(1f))

    var showInstrumentedTestExtraInfoDialog by remember { mutableStateOf(false) }

    Row(
      horizontalArrangement = Arrangement.Start,
      modifier = Modifier.fillMaxWidth()
    ) {
      PrimaryIconButton(
        imageVector = Icons.Rounded.Info,
        modifier = Modifier
          .size(45.dp)
          .alpha(0.5f)
      ) { showInstrumentedTestExtraInfoDialog = true }
    }

    if (showInstrumentedTestExtraInfoDialog) {
      AlertDialog(
        onDismissRequest = { showInstrumentedTestExtraInfoDialog = false },
        confirmButton = {
          TextButton(
            content = { Text("Cerrar") },
            onClick = { showInstrumentedTestExtraInfoDialog = false }
          )
        },
        text = {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            InstrumentedTestRequiredAppsVersionTable(
              waVersion = whatsAppInstalledVersion,
              transfermovilVersion = transfermovilInstalledVersion,
              conexenToolsInstA = instrumentationAppInstalledVersion,
            )

            Row(
              modifier = Modifier.padding(Constants.Dimens.Medium),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Dispositivo Rooteado: ")
              Text(
                text = if (RootUtil.isDeviceRooted) "SI" else "NO",
                style = TextStyle.Default.copy(fontWeight = FontWeight.Bold),
                color = if (RootUtil.isDeviceRooted) Color.Green else Constants.Colors.ERROR
              )
            }
          }
        }
      )
    }
  }
}

@Composable
private fun InstrumentedTestRequiredAppsVersionTable(
  waVersion: Pair<Long, String>?,
  transfermovilVersion: Pair<Long, String>?,
  conexenToolsInstA: Pair<Long, String>?,
) {

  Row(
    verticalAlignment = Alignment.Top,
    horizontalArrangement = Arrangement.Center,
    modifier = Modifier.height(130.dp)
  ) {

    Column {
      Box(modifier = Modifier.size(50.dp))
      Text("Instalado")
      Text("Probado")
    }

    Spacer(modifier = Modifier.width(Constants.Dimens.Medium))

    VerticalDivider(modifier = Modifier.padding(vertical = Constants.Dimens.Medium))

    // WhatsApp
    RequiredAppColumn(
      version = waVersion,
      drawableRes = R.drawable.whatsapp,
      testedVersionCode = BuildConfig.TESTED_WA_VERSION_CODE,
      testedVersionName = BuildConfig.TESTED_WA_VERSION_NAME
    )

    VerticalDivider(modifier = Modifier.padding(vertical = Constants.Dimens.Medium))

    // Transfermóvil
    RequiredAppColumn(
      version = transfermovilVersion,
      drawableRes = R.drawable.transfermovil,
      testedVersionCode = BuildConfig.TESTED_TM_VERSION_CODE,
      testedVersionName = BuildConfig.TESTED_TM_VERSION_NAME
    )

    VerticalDivider(modifier = Modifier.padding(vertical = Constants.Dimens.Medium))

    // Conexen Tools - Instrumentation App
    RequiredAppColumn(
      version = conexenToolsInstA,
      drawableRes = R.drawable.conexen_tools_ia,
      testedVersionCode = stringResource(id = R.string.iaVersionCode),
      testedVersionName = stringResource(id = R.string.iaVersionName),
    )
  }
}

@Composable
private fun RequiredAppColumn(
  version: Pair<Long, String>?,
  @DrawableRes drawableRes: Int,
  testedVersionCode: String,
  testedVersionName: String
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.padding(horizontal = Constants.Dimens.Small)
  ) {
    Image(
      modifier = Modifier.size(50.dp),
      painter = painterResource(drawableRes),
      contentDescription = null
    )
    VersionText(version)
    Text(testedVersionName)

    val isRedWarning = version == null
    val isWarning = !isRedWarning && (version!!.first != testedVersionCode.toLong())
    val isAllGood = !isRedWarning && !isWarning

    Icon(
      imageVector = with(Icons.Rounded) { if (isAllGood) CheckCircle else Warning },
      tint = if (isRedWarning) Constants.Colors.ERROR else if (isWarning) Constants.Colors.WARNING else Color.Green,
      contentDescription = null
    )
  }
}

@Composable
private fun VersionText(version: Pair<Long, String>?) {
  Text(
    text = version?.second ?: "NO",
    color = if (version == null) Color.Red else MaterialTheme.colorScheme.onBackground
  )
}

@Suppress("RedundantNullableReturnType")
@Preview(showBackground = true, apiLevel = 33)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, apiLevel = 33)
@Composable
private fun PreviewInstrumentedTestRequiredAppsVersionTable() {
  val waVersion: Pair<Long, String>? = Pair(12, "12.2.6")
  val transfermovilVersion: Pair<Long, String>? = null
  val conexenToolsInstA: Pair<Long, String>? = Pair(12, "156.2.6")
  PreviewComposable(
    fillMaxSize = false
  ) {
    InstrumentedTestRequiredAppsVersionTable(
      waVersion = waVersion,
      transfermovilVersion = transfermovilVersion,
      conexenToolsInstA = conexenToolsInstA
    )
  }
}
