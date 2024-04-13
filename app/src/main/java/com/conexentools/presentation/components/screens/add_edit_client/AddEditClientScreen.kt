package com.conexentools.presentation.components.screens.add_edit_client

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
import com.conexentools.core.util.pickContact
import com.conexentools.core.util.toFormattedDate
import com.conexentools.data.local.model.Client
import com.conexentools.domain.repository.AndroidUtils
import com.conexentools.presentation.components.common.CreditCardTextField
import com.conexentools.presentation.components.common.CubanPhoneNumberTextField
import com.conexentools.presentation.components.common.PrimaryIconButton
import com.conexentools.presentation.components.common.ScreenSurface
import com.conexentools.presentation.components.common.cleanCubanMobileNumber
import contacts.core.util.phoneList
import java.time.Instant

@Composable
fun AddEditClientScreen(
  client: Client,
  isNewClient: Boolean,
  au: AndroidUtils,
//  onEdit: (Client) -> Unit,
//  onAdd: (Client) -> Unit,
  onSubmit: (Client) -> Unit,
  onCancel: () -> Unit,
  onNavigateBack: () -> Unit,
//  lastClientOnBatch: Client? = null
//  onClientAddedFromContactPicker: ((Client) -> Unit)?,
) {

  // States
  var name by remember(client.name) { mutableStateOf(client.name) }
  var phoneNumber by remember(client.phoneNumber) {
    mutableStateOf(
      client.phoneNumber ?: ""
    )
  }
  var cardNumber by remember(client.cardNumber) {
    mutableStateOf(
      client.cardNumber ?: ""
    )
  }
  var quickMessage by remember(client.quickMessage) {
    mutableStateOf(
      if (isNewClient)
        "💸💸💳📲💸💸"
      else
        client.quickMessage ?: ""
    )
  }
  
  var latestRechargeDateISOString by remember(client.latestRechargeDateISOString) { mutableStateOf(client.latestRechargeDateISOString) }
  var rechargesMade by remember(client.rechargesMade) { mutableIntStateOf(client.rechargesMade ?: 0) }
  var imageUri by remember(client.imageUriString) { mutableStateOf(client.imageUriString) }

  val quickMessageTrailingIcon = @Composable {
    IconButton(
      onClick = {
        client.quickMessage = null
        quickMessage = ""
      }
    ) {
      Icon(
        imageVector = Icons.Rounded.Cancel,
        contentDescription = null
      )
    }
  }

//  if (!latestRechargeDateISOString.isNullOrEmpty()) {
//    try {
//      log("isoString: $latestRechargeDateISOString")
//      val date = Date.from(Instant.parse(latestRechargeDateISOString))
//      latestRechargeDateISOString = DateFormat.getDateTimeInstance().format(date)
//    } catch (exc: Exception) {
//      au.toast("No se pudo formatear la fecha de la última recarga realizada", vibrate = true)
//      au.toast(exc.localizedMessage)
//    }
//  }

  val pickContactLauncher = pickContact(au = au) { contact ->
    if (contact != null) {
      name = contact.displayNamePrimary ?: ""
      val number = contact.phoneList().firstOrNull()?.normalizedNumber?.cleanCubanMobileNumber()
      phoneNumber = number ?: ""
      imageUri = contact.photoUri?.toString() ?: contact.photoThumbnailUri?.toString()
      client.name = name
      client.phoneNumber = number
      client.imageUriString = imageUri
      log("name: $name")
      log("phoneNumber: $phoneNumber")
      log("onEditionClient.imageUriString: ${client.imageUriString}")
      log("imageUri: $imageUri")
    }
  }

  ScreenSurface(
    title = if (isNewClient) "Nuevo Cliente" else client.name,
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
//              } else if (client == null) onAdd(onEditionClient) else onEdit(onEditionClient)
              } else {
                if (client.quickMessage != quickMessage)
                  client.quickMessage = quickMessage
                onSubmit(client)
//                onClientAddedFromContactPicker?.invoke(client)
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
              client.name = it
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
              client.phoneNumber = it
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
            pickContactLauncher.launch(null)
          }
        }
      }

      Spacer(modifier = Modifier.height(Constants.Dimens.Medium))

      CreditCardTextField(
        value = cardNumber,
        onValueChange = {
          client.cardNumber = it
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
            client.quickMessage = it
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
                client.rechargesMade = rechargesMade
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
              client.rechargesMade = rechargesMade
            }
          }

          if (rechargesMade > 0) {
            Box(
              modifier = Modifier.absoluteOffset(x = LocalDensity.current.run { size.width.toDp() })
            ) {
              PrimaryIconButton(Icons.Rounded.RestartAlt) {
                client.rechargesMade = 0
                rechargesMade = 0
              }
            }
          }
        }

        fun updateRechargeDate() {
          latestRechargeDateISOString = Instant.now().toString()
          client.latestRechargeDateISOString = latestRechargeDateISOString
        }

        if (!latestRechargeDateISOString.isNullOrEmpty()) {
          Spacer(modifier = Modifier.height(Constants.Dimens.Small))
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {

            // Delete
            PrimaryIconButton(Icons.Rounded.Cancel) {
              client.latestRechargeDateISOString = null
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
fun PreviewAddEditClientScreen() {
  PreviewComposable {
    AddEditClientScreen(
//      client = null,
      client = Client(
        id = 0,
//        name = "Ebaristo oaisdfpgoahsdofpiauspdoifapsdkiofja;ksdljfa;sdlkfjfpiauspdoifapsdkiofja;kfpiauspdoifapsdkiofja;ksdljfa;sdlkfjfpiauspdoifapsdkiofja;ksdljristo oaisdfpgoahsdofpiauspdoifapsdkiofja;ksdljfa;sdlkfjfpiauspdoifapsdkiofja;kfpiauspdoifapsdkiofja;ksdljfa;sdlristo oaisdfpgoahsdofpiauspdoifapsdkiofja;ksdljfa;sdlkfjfpiauspdoifapsdkiofja;kfpiauspdoifapsdkiofja;ksdljfa;sdlkfjfpiauspdoifapsdkiofja;ksdljkfjfpiauspdoifapsdkiofja;ksdljfa;sdlkfjsdljfa;sdlkfjdkslalksjdfkdl;alksdjfk;lasdkljf",
        name = "Ebaristo",
        phoneNumber = "5579",
//        cardNumber = "",
//        latestRechargeDateISOString = "2024-03-16T17:10:24.459198500Z",
        imageUriString = "asd",
        quickMessage = "",
        rechargesMade = 45
      ),
//      onAdd = {},
//      onEdit = {},
      onSubmit = {},
      onCancel = {},
      onNavigateBack = {},
//      onClientAddedFromContactPicker = {},
      isNewClient = false,
      au = AndroidUtils.create(),
    )
  }
}