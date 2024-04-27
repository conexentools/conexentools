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
  onSendMessage: (String, String?) -> Unit,
  onDelete: (Client) -> Unit,
  onClientCardCounterReset: (Client) -> Unit,
  showDivider: Boolean = true,
  clientToTransferCash: MutableState<Client?>,
  showTransferCashDialog: MutableState<Boolean>,
  au: AndroidUtils
) {

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
      clientToTransferCash.value = client
      showTransferCashDialog.value = true
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
    Text(
      text = (client.rechargesMade ?: 0).toString(),
      color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
      style = MaterialTheme.typography.labelMedium,
      modifier = Modifier.padding(Constants.Dimens.MegaSmall)
    )
  }
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
          onDelete = {},
          au = AndroidUtilsImpl(LocalContext.current),
          onClientCardCounterReset = {},
          showDivider = false,
          clientToTransferCash = remember { mutableStateOf(Client()) },
          showTransferCashDialog = remember { mutableStateOf(false) },
        )
      }
    }
  }
}