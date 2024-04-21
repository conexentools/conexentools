package com.conexentools.presentation.components.screens.add_edit_client

import android.Manifest
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Contacts
import androidx.compose.material.icons.rounded.RemoveCircleOutline
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.conexentools.R
import com.conexentools.core.app.Constants
import com.conexentools.core.util.PreviewComposable
import com.conexentools.core.util.log
import com.conexentools.core.util.moveFocusOnTabPressed
import com.conexentools.core.util.navigateAndPopDestinationFromTheBackStack
import com.conexentools.core.util.pickContact
import com.conexentools.core.util.toFormattedDate
import com.conexentools.data.local.model.Client
import com.conexentools.domain.repository.AndroidUtils
import com.conexentools.presentation.components.common.CreditCardTextField
import com.conexentools.presentation.components.common.CubanPhoneNumberTextField
import com.conexentools.presentation.components.common.PrimaryIconButton
import com.conexentools.presentation.components.common.ScreenSurface
import com.conexentools.presentation.components.common.cleanCubanMobileNumber
import com.conexentools.presentation.navigation.AddEditClientScreenParameterManager
import com.conexentools.presentation.navigation.Screen
import contacts.core.util.phoneList
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditClientScreen(
  client: MutableState<Client>,
  isNewClient: Boolean,
  au: AndroidUtils,
  onSubmit: (Client) -> Unit,
  onCancel: () -> Unit,
  onOmit: (() -> Unit)?,
  onNavigateBack: () -> Unit,
) {

  // States
  var name by remember(client.value.name) { mutableStateOf(client.value.name) }
  var phoneNumber by remember(client.value.phoneNumber) {
    mutableStateOf(
      client.value.phoneNumber ?: ""
    )
  }

  var cardNumber by remember(client.value.cardNumber) {
    mutableStateOf(
      client.value.cardNumber ?: ""
    )
  }
  var quickMessage by remember(client.value.quickMessage) {
    mutableStateOf(
      if (isNewClient)
        "💸💸💳📲💸💸"
      else
        client.value.quickMessage ?: ""
    )
  }

  if (client.value.quickMessage != quickMessage)
    client.value.quickMessage = quickMessage

  var latestRechargeDateISOString by remember(client.value.latestRechargeDateISOString) {
    mutableStateOf(
      client.value.latestRechargeDateISOString
    )
  }

  var rechargesMade by remember(client.value.rechargesMade) {
    mutableIntStateOf(
      client.value.rechargesMade ?: 0
    )
  }

  var imageUri by remember(client.value.imageUriString) { mutableStateOf(client.value.imageUriString) }

  val quickMessageTrailingIcon = @Composable {
    IconButton(
      onClick = {
        client.value.quickMessage = null
        quickMessage = ""
      }
    ) {
      Icon(
        imageVector = Icons.Rounded.Cancel,
        contentDescription = null
      )
    }
  }

  val pickContactLauncher = pickContact(au = au) { contact ->
    if (contact != null) {
      name = contact.displayNamePrimary ?: ""
      val number = contact.phoneList().firstOrNull()?.normalizedNumber?.cleanCubanMobileNumber()
      phoneNumber = number ?: ""
      imageUri = contact.photoUri?.toString() ?: contact.photoThumbnailUri?.toString()
      client.value.name = name
      client.value.phoneNumber = number
      client.value.imageUriString = imageUri
      log("name: $name")
      log("phoneNumber: $phoneNumber")
      log("onEditionClient.imageUriString: ${client.value.imageUriString}")
      log("imageUri: $imageUri")
    }
  }

  ScreenSurface(
    title = if (isNewClient) "Nuevo Cliente" else client.value.name,
    bottomContent = {
      Column(modifier = Modifier.padding(Constants.Dimens.ExtraLarge)) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
          horizontalArrangement = Arrangement.SpaceAround,
          verticalAlignment = Alignment.CenterVertically
        ) {

          // Cancel Button
          Button(
            onClick = onCancel,
            modifier = Modifier.fillMaxHeight(),
          ) {
            Text(
              text = "Cancelar",
              style = MaterialTheme.typography.titleMedium
            )
          }

          // Omit Button
          if (onOmit != null) {
            Button(
              onClick = onOmit,
              modifier = Modifier.fillMaxHeight(),
            ) {
              Text(
                text = "Omitir",
                style = MaterialTheme.typography.titleMedium
              )
            }
          }

          // Add|Edit Button
          Button(
            modifier = Modifier.fillMaxHeight(),
            onClick = {
              if (name.isEmpty()) {
                au.toast("El campo 'Nombre' no puede estar vacío", vibrate = true)
              } else if (cardNumber.length in 1..15) {
                au.toast("El número de la tarjeta debe tener 16 dígitos", vibrate = true)
                au.toast("Deje el campo vacío para no adjuntar una tarjeta al cliente")
              } else if (phoneNumber.length in 1..7) {
                au.toast("El número de teléfono debe tener 8 dígitos", vibrate = true)
                au.toast("Deje el campo vacío para no adjuntar un número de teléfono al cliente")
              } else {
                onSubmit(client.value)
              }
            }
          ) {
            Text(
              text = if (isNewClient) "Añadir" else "Editar",
              style = MaterialTheme.typography.titleMedium
            )
          }
        }
      }
    },
    onNavigateBack = onNavigateBack,
  ) {

    if (!imageUri.isNullOrEmpty()) {
      AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
          .data(imageUri)
          .build(),
        placeholder = painterResource(id = R.drawable.contact_image_placeholder_02),
        contentDescription = null,
        modifier = Modifier
          .size(200.dp)
          .padding(Constants.Dimens.ExtraLarge)
          .clip(CircleShape),
        contentScale = ContentScale.Crop,
      )
    }

    Spacer(modifier = Modifier.height(Constants.Dimens.Small))

    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {

      // Name - Number
      Row(
        verticalAlignment = Alignment.CenterVertically,
      ) {

        Column(
          modifier = Modifier.weight(3f),
          verticalArrangement = Arrangement.Center,
        ) {

          val focusManager = LocalFocusManager.current

          // Client Name
          OutlinedTextField(
            modifier = Modifier
              .fillMaxWidth()
              .moveFocusOnTabPressed(FocusDirection.Down, focusManager),
            label = { Text("Nombre") },
            value = name,
            onValueChange = {
              client.value.name = it
              name = it
            },
            keyboardOptions = KeyboardOptions.Default.copy(
              imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
              onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
          )

          Spacer(modifier = Modifier.height(Constants.Dimens.Small))

          // Client Number
          CubanPhoneNumberTextField(
            modifier = Modifier.fillMaxWidth(),
            value = phoneNumber,
            onValueChange = {
              client.value.phoneNumber = it
              phoneNumber = it
            },
            isOutlinedTextField = true,
          )
        }

        Spacer(modifier = Modifier.width(Constants.Dimens.MegaSmall))

        Box(
          contentAlignment = Alignment.Center
        ) {
          PrimaryIconButton(imageVector = Icons.Rounded.Contacts) {
            if (au.isPermissionGranted(Manifest.permission.READ_CONTACTS)){
              pickContactLauncher.launch(null)
            } else {
              au.toast("Permiso para leer contactos requerido", vibrate = true)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(Constants.Dimens.Medium))

      CreditCardTextField(
        value = cardNumber,
        onValueChange = {
          client.value.cardNumber = it
          cardNumber = it
        },
        isFourDigitsCard = false
      )

      Spacer(modifier = Modifier.height(Constants.Dimens.Medium))

      Text("Mensaje")
      TextField(
        value = quickMessage,
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = if (quickMessage.isEmpty()) null else quickMessageTrailingIcon,
        singleLine = false,
        onValueChange = {
          if (it.length <= Constants.MAX_QUICK_MESSAGE_LENGTH) {
            quickMessage = it
            client.value.quickMessage = it
          }
        }
      )

      Spacer(modifier = Modifier.height(Constants.Dimens.Medium))

      if (!isNewClient) {
        var size by remember { mutableStateOf(IntSize.Zero) }

        Box(
          modifier = Modifier.onGloballyPositioned { coordinates ->
            size = coordinates.size
          }

        ) {
          Row {

            PrimaryIconButton(
              imageVector = Icons.Rounded.RemoveCircleOutline,
              enabled = rechargesMade > 0
            ) {
              if (rechargesMade > 0) {
                rechargesMade--
                client.value.rechargesMade = rechargesMade
              }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "Recargas Realizadas:",
                style = MaterialTheme.typography.bodyLarge
              )

              Text(
                text = rechargesMade.toString(),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
              )
            }

            PrimaryIconButton(Icons.Rounded.AddCircleOutline) {
              rechargesMade++
              client.value.rechargesMade = rechargesMade
            }
          }

          if (rechargesMade > 0) {
            Box(
              modifier = Modifier.absoluteOffset(x = LocalDensity.current.run { size.width.toDp() })
            ) {
              PrimaryIconButton(Icons.Rounded.RestartAlt) {
                client.value.rechargesMade = 0
                rechargesMade = 0
              }
            }
          }
        }

        fun updateRechargeDate() {
          latestRechargeDateISOString = Instant.now().toString()
          client.value.latestRechargeDateISOString = latestRechargeDateISOString
        }

        if (!latestRechargeDateISOString.isNullOrEmpty()) {
          Spacer(modifier = Modifier.height(Constants.Dimens.Small))
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {

            // Delete
            PrimaryIconButton(Icons.Rounded.Cancel) {
              client.value.latestRechargeDateISOString = null
              latestRechargeDateISOString = null
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {

              Text(
                text = "Última Recarga:",
                fontSize = MaterialTheme.typography.bodyLarge.fontSize
              )
              Text(
                text = try {
                  latestRechargeDateISOString!!.toFormattedDate()
                } catch (exc: Exception) {
                  au.toast(
                    "No se pudo formatear la fecha de la última recarga realizada",
                    vibrate = true
                  )
                  au.toast(exc.localizedMessage)
                  "error"
                },
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
              )
            }

            // Reset
            PrimaryIconButton(Icons.Rounded.RestartAlt, onClick = ::updateRechargeDate)
          }
        } else {
          Button(
            onClick = ::updateRechargeDate,
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.secondary
            )
          ) {
            Text(
              text = "Actualizar Fecha de Recarga",
              fontSize = MaterialTheme.typography.bodyLarge.fontSize,
            )
          }
        }
      }
    }
  }
}

@Preview(showBackground = true, apiLevel = 33)
@Preview(showBackground = true, apiLevel = 33, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewAddEditClientScreen() {
  PreviewComposable {
    AddEditClientScreen(
//      client = null,
      client = remember {
        mutableStateOf(
          Client(
            id = 0,
//        name = "Ebaristo oaisdfpgoahsdofpiauspdoifapsdkiofja;ksdljfa;sdlkfjfpiauspdoifapsdkiofja;kfpiauspdoifapsdkiofja;ksdljfa;sdlkfjfpiauspdoifapsdkiofja;ksdljristo oaisdfpgoahsdofpiauspdoifapsdkiofja;ksdljfa;sdlkfjfpiauspdoifapsdkiofja;kfpiauspdoifapsdkiofja;ksdljfa;sdlristo oaisdfpgoahsdofpiauspdoifapsdkiofja;ksdljfa;sdlkfjfpiauspdoifapsdkiofja;kfpiauspdoifapsdkiofja;ksdljfa;sdlkfjfpiauspdoifapsdkiofja;ksdljkfjfpiauspdoifapsdkiofja;ksdljfa;sdlkfjsdljfa;sdlkfjdkslalksjdfkdl;alksdjfk;lasdkljf",
            name = "Ebaristo",
            phoneNumber = "5579",
            imageUriString = "asd",
            quickMessage = "",
            rechargesMade = 45
          )
        )
      },
      onSubmit = {},
      onCancel = {},
      onOmit = {},
      onNavigateBack = {},
      isNewClient = false,
      au = AndroidUtils.create(),
    )
  }
}