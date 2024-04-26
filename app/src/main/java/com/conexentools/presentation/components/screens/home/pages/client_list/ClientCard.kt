package com.conexentools.presentation.components.screens.home.pages.client_list

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.Message
import androidx.compose.material.icons.twotone.AttachMoney
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material.icons.twotone.Edit
import androidx.compose.material.icons.twotone.Phone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.conexentools.core.app.Constants
import com.conexentools.core.util.PreviewComposable
import com.conexentools.data.local.model.Client
import com.conexentools.data.repository.AndroidUtilsImpl
import com.conexentools.domain.repository.AndroidUtils
import com.conexentools.presentation.components.common.Contact
import com.conexentools.presentation.components.common.RadialProgressTimeIndicator
import com.conexentools.presentation.components.common.ScrollableAlertDialog
import com.conexentools.presentation.components.common.sanitizeNumberString
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClientCard(
  client: Client,
  onEdit: (Client) -> Unit,
//  onTransferCash: (Client) -> Unit,
  onTransferCash: (Client, (canExecuteTransferCashInstrumentedTest: Boolean) -> Unit) -> Unit,
//  onRunTransferCashInstrumentedTest: (
//    recipientCard: String,
//    mobileToConfirm: String,
//    numberToSendWhatsAppMessage: String?
//  ) -> Unit,
//  defaultMobileToSendCashTransferConfirmation: String,
//  recipientReceiveMyMobileNumberAfterCashTransfer: MutableState<Boolean>,
//  cashToTransfer: MutableState<String>,
//  sendWhatsAppMessageOnTransferCashTestCompleted: MutableState<Boolean>,
//  whatsAppMessageToSendOnTransferCashTestCompleted: MutableState<String>,
//  transferCashInstrumentedTestAdbCommandGetter: (
//    recipientCard: String,
//    mobileToConfirm: String
//  ) -> String,
  onSendMessage: (String, String?) -> Unit,
  onDelete: (Client) -> Unit,
  onClientCardCounterReset: (Client) -> Unit,
  showDivider: Boolean = true,
  clientToTransferCash: MutableState<Client?>,
  showTransferCashDialog: MutableState<Boolean>,
  au: AndroidUtils
) {

//  var showTransferCashDialog by remember { mutableStateOf(false) }

  // This method is going to receive the parameters of the instrumented test TransferCash and is going to execute the instrumented test
//  var onRechargeClientRechargeButton = { }

  var counterToForceRecomposition by remember { mutableIntStateOf(0) }

  // Actions
  val edit = SwipeAction(icon = rememberVectorPainter(Icons.TwoTone.Edit),
    background = Color.Cyan,
    onSwipe = {
      onEdit(client)
    }
  )

  val transferCash = SwipeAction(icon = rememberVectorPainter(Icons.TwoTone.AttachMoney),
    background = Color.Yellow,
    onSwipe = {
      onTransferCash(
        client
      ) { canExecuteTransferCashInstrumentedTest ->
        if (canExecuteTransferCashInstrumentedTest){
          clientToTransferCash.value = client
          showTransferCashDialog.value = true
        }
        else {
          // Forcing to recompose Recharge Availability Remaining Time Indicator and Recharges Made counter since they may not get recomposed
          counterToForceRecomposition++
        }
      }
    }
  )

  val sendMessage = SwipeAction(icon = rememberVectorPainter(Icons.AutoMirrored.TwoTone.Message),
    background = Color.Magenta,
    onSwipe = {
      val errorMessage = if (client.phoneNumber.isNullOrEmpty()) {
        "Adjunte un número de teléfono a este cliente para poder enviarle un mensaje por WhatsApp"
      } else if (client.quickMessage.isNullOrEmpty()) {
        "Agregue un mensaje rápido para este cliente"
      } else {
        ""
      }

      if (errorMessage.isEmpty()) {
        onSendMessage(client.phoneNumber!!.sanitizeNumberString(), client.quickMessage)
      } else {
        au.toast(
          errorMessage,
          vibrate = true
        )
      }
    }
  )

  val call = SwipeAction(icon = rememberVectorPainter(Icons.TwoTone.Phone),
    background = Color.Green,
    onSwipe = {
      client.call(au = au)
    }
  )

  val delete = SwipeAction(icon = rememberVectorPainter(Icons.TwoTone.Delete),
    background = Color.Red,
    onSwipe = {
      onDelete(client)
    }
  )

  SwipeableActionsBox(
    startActions = listOf(edit, transferCash, sendMessage),
    endActions = listOf(call, delete),
    modifier = Modifier.background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.02f))
  ) {

    Contact(
      name = client.name,
      subtitle = client.cardNumber?.run {
        this.replace(" ", "").replace("-", "").chunked(4).joinToString("-")
      },
      onSubtitleLongClick = {
        au.setClipboard(it)
        au.toast("Número de tarjeta copiado al portapapeles")
      },
      imageUriString = client.imageUriString,
      showDivider = showDivider,
      backgroundColor = Color.Transparent,
      titleLetter = MaterialTheme.colorScheme.background
    ) {

      Spacer(modifier = Modifier.width(Constants.Dimens.Small))

      var showResetRechargeTimerDialog by remember { mutableStateOf(false) }

      // Recharge Availability Remaining Time Indicator
      with(client.getRemainingTimeForNextRechargeToBeAvailable()) {
        key(this, counterToForceRecomposition) {
          if (this != null) {
            Surface(modifier = Modifier.size(55.dp)) {
              RadialProgressTimeIndicator(
                value = this.seconds.toFloat(),
                maxValue = 60 * 60 * 24f, //24h
                timeNumericTextRepresentation = this.numericRepresentationPart,
                timeUnit = this.unit,
                strokeWidthRatio = 0.3f,
                modifier = Modifier
                  .clip(CircleShape)
                  .combinedClickable(
                    onClick = { },
                    onLongClick = {
                      showResetRechargeTimerDialog = true
                    },
                  )
              )
            }
          }
        }
      }

      // Reset Recharge Timer Dialog
      if (showResetRechargeTimerDialog) {
        ScrollableAlertDialog(
          title = "Reiniciar Contador?",
          isInfoDialog = false,
          yesNoDialog = true,
          onConfirm = {
            client.latestRechargeDateISOString = null
            showResetRechargeTimerDialog = false
            onClientCardCounterReset(client)
          },
          onDismiss = { showResetRechargeTimerDialog = false },
          onDismissRequest = { showResetRechargeTimerDialog = false }
        )
      }
    }

    // Recharges made counter
    with(client.rechargesMade) {
      key(this, counterToForceRecomposition) {
        Text(
          text = (this ?: 0).toString(),
          color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
          style = MaterialTheme.typography.labelMedium,
          modifier = Modifier.padding(Constants.Dimens.MegaSmall)
        )
      }
    }
  }

//  // Transfer Cash Dialog
//  if (showTransferCashDialog) {
//
//    var mobileToConfirm by remember { mutableStateOf(defaultMobileToSendCashTransferConfirmation.ifEmpty { client.phoneNumber ?: "" }) }
//    var creditCard by remember { mutableStateOf(client.cardNumber!!) }
//
//    var showAdbCommand by remember { mutableStateOf(false) }
//
//    AlertDialog(
//      title = { Text("Transferir Efectivo") },
//      onDismissRequest = {},
//      dismissButton = {
//        // Cancelar
//        if (!showAdbCommand) {
//          TextButton(onClick = { showTransferCashDialog = false }) {
//            Text("Cancelar")
//          }
//        }
//      },
//      confirmButton = {
//        Row {
//          // Atrás
//          if (showAdbCommand) {
//            TextButton(onClick = { showTransferCashDialog = false }) {
//              Text("Atrás")
//            }
//          } else {
//
//            // Show ADB command
//            TextButton(onClick = { showAdbCommand = true }) {
//              Text("Comando ADB")
//            }
//
//            // Transfer
//            TextButton(onClick = {
//
//              val errorMessage = if (creditCard.length != 16)
//                "El número de la tarjeta debe tener 16 dígitos"
//              else if (mobileToConfirm.length != 8)
//                "El número del móvil a confirmar debe tener 8 dígitos"
//              else ""
//
//              if (errorMessage.isEmpty()){
//                showTransferCashDialog = false
//                onRunTransferCashInstrumentedTest(
//                  creditCard,
//                  mobileToConfirm,
//                  client.phoneNumber
//                )
//              } else {
//                au.toast(errorMessage, vibrate = true)
//              }
//            }) {
//              Text("Transferir")
//            }
//          }
//        }
//      },
//      text = {
//        if (showAdbCommand) {
//          val command by remember(creditCard, mobileToConfirm) {
//            mutableStateOf(
//              transferCashInstrumentedTestAdbCommandGetter(
//                creditCard,
//                mobileToConfirm
//              )
//            )
//          }
//          Text(
//            text = command,
//            modifier = Modifier.clickable {
//              au.setClipboard(command)
//              au.toast("Comando copiado al portapapeles")
//            }
//          )
//        } else {
//          LazyColumn(
//            modifier = Modifier.fillMaxWidth(),
//            verticalArrangement = Arrangement.spacedBy(Constants.Dimens.Small)
//          ) {
//            item {
//              // Credit card
//              CreditCardTextField(
//                value = creditCard,
//                onValueChange = { creditCard = it }
//              )
//
//              // Cash
//              CashTextField(
//                value = cashToTransfer.value,
//                onValueChange = { cashToTransfer.value = it }
//              )
//
//              // Mobile to send transfer confirmation message
//              CubanPhoneNumberTextField(
//                value = mobileToConfirm,
//                onValueChange = { mobileToConfirm = it },
//                isOutlinedTextField = true,
//                label = { Text("Móvil a confirmar") }
//              )
//
//              // Recipient receive my phone number
//              LabelSwitch(
//                label = "Destinatario recibe mi número",
//                checked = recipientReceiveMyMobileNumberAfterCashTransfer
//              )
//
//              // Send WhatsApp Message On Transfer Cash Test Completed
//              LabelSwitch(
//                info = "Enviar mensaje por WhatsApp cuando la transferencia se complete satisfactoriamente?",
//                label = "Enviar mensaje",
//                checked = sendWhatsAppMessageOnTransferCashTestCompleted,
//                onCheckedChange = {
//                  if (it && client.phoneNumber == null){
//                    sendWhatsAppMessageOnTransferCashTestCompleted.value = false
//                    au.toast("Este cliente no tiene número asociado")
//                  }
//                }
//              )
//              if (sendWhatsAppMessageOnTransferCashTestCompleted.value) {
//                MultilineTextField(
//                  value = whatsAppMessageToSendOnTransferCashTestCompleted.value,
//                  onValueChange = { whatsAppMessageToSendOnTransferCashTestCompleted.value = it },
//                  onDeleteText = { whatsAppMessageToSendOnTransferCashTestCompleted.value = "" },
//                  label = "Mensaje"
//                )
//              }
//            }
//          }
//        }
//      }
//    )
//  }
}

@Preview(showBackground = true, apiLevel = 33)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, apiLevel = 33)
@Composable
private fun PreviewClientCard() {
  PreviewComposable(fillMaxSize = false) {
    LazyColumn {
      items(clientsForTesting) {
        ClientCard(
          client = it,
          onEdit = {},
          onSendMessage = { _, _ -> },
          onTransferCash = { _, _ -> },
          onDelete = {},
          au = AndroidUtilsImpl(LocalContext.current),
          onClientCardCounterReset = {},
//          defaultMobileToSendCashTransferConfirmation = "",
//          recipientReceiveMyMobileNumberAfterCashTransfer = remember { mutableStateOf(false) },
//          onRunTransferCashInstrumentedTest = { _, _, _ -> },
//          cashToTransfer = remember { mutableStateOf("") },
          showDivider = false,
          clientToTransferCash = remember { mutableStateOf(Client()) },
          showTransferCashDialog = remember { mutableStateOf(false) },
//          sendWhatsAppMessageOnTransferCashTestCompleted = remember { mutableStateOf(false) },
//          whatsAppMessageToSendOnTransferCashTestCompleted = remember { mutableStateOf("") },
//          transferCashInstrumentedTestAdbCommandGetter = { _, _ -> "Habitantvolutpat HabitantvolutpatHabitantvolutpatHabitantvolutpatHabitantvolutpatHabitantvolutpat" },
        )
      }
    }
  }
}