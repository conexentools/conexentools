package com.conexentools.presentation.components.screens.home.pages.client_list

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.Message
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.twotone.AttachMoney
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material.icons.twotone.Edit
import androidx.compose.material.icons.twotone.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.conexentools.R
import com.conexentools.core.app.Constants.Dimens
import com.conexentools.core.util.PreviewComposable
import com.conexentools.data.local.model.Client
import com.conexentools.data.repository.AndroidUtilsImpl
import com.conexentools.domain.repository.AndroidUtils
import com.conexentools.presentation.components.common.RadialProgressTimeIndicator
import com.conexentools.presentation.components.common.cleanNumberString
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClientCard(
  client: Client,
  onEdit: (Client) -> Unit,
  onRecharge: (Client) -> Unit,
  onSendMessage: (String, String?) -> Unit,
  onDelete: (Client) -> Unit,
  onClientRechargeCounterReset: (Client) -> Unit,
  showDivider: Boolean = true,
  au: AndroidUtils
) {

  // Actions
  val edit = SwipeAction(icon = rememberVectorPainter(Icons.TwoTone.Edit),
    background = Color.Green,
    onSwipe = {
      onEdit(client)
    }
  )

  val recharge = SwipeAction(icon = rememberVectorPainter(Icons.TwoTone.AttachMoney),
    background = Color.Yellow,
    onSwipe = {
      onRecharge(client)
    }
  )

  val sendMessage = SwipeAction(icon = rememberVectorPainter(Icons.AutoMirrored.TwoTone.Message),
    background = Color.Magenta,
    onSwipe = {
      if (client.phoneNumber.isNullOrEmpty()) {
        au.toast(
          "Adjunte un número de teléfono a este cliente para poder enviarle un mensaje por WA",
          vibrate = true
        )
      } else {
        onSendMessage(client.phoneNumber!!.cleanNumberString(), client.quickMessage)
      }
    }
  )

  val call = SwipeAction(icon = rememberVectorPainter(Icons.TwoTone.Phone),
    background = Color.Cyan,
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
    startActions = listOf(edit, recharge, sendMessage),
    endActions = listOf(call, delete),
    modifier = Modifier.background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.02f))
  ) {

    Column(
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(Dimens.ClientCardHeight)
          .padding(PaddingValues(horizontal = Dimens.Medium)),
        verticalAlignment = Alignment.CenterVertically
      ) {

        // Contact Image
        Box(
          modifier = Modifier.size(55.dp)
        ) {
          if (client.imageUriString.isNullOrEmpty()) {
            Icon(
              imageVector = Icons.Filled.AccountCircle,
              modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .border(
                  width = Dimens.MegaSmall,
                  shape = CircleShape,
                  color = MaterialTheme.colorScheme.primary
                ),
              tint = MaterialTheme.colorScheme.primary,
              contentDescription = null
            )
          } else {
            AsyncImage(
              model = ImageRequest.Builder(LocalContext.current).data(client.imageUriString)
                .crossfade(true)
                .build(),
              alignment = Alignment.Center,
              placeholder = painterResource(R.drawable.contact_image_placeholder_02),
              modifier = Modifier.clip(CircleShape),
              contentDescription = null
            )
          }
        }

        Spacer(modifier = Modifier.width(Dimens.Medium))

        Column(
          verticalArrangement = Arrangement.Center,
          modifier = Modifier.weight(1f)
        ) {
          Text(
            text = client.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
          client.cardNumber?.let {
            Text(
              it.replace(" ", "").replace("-", "").chunked(4).joinToString("-"),
              style = MaterialTheme.typography.titleSmall,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.combinedClickable(onLongClick = {
                au.setClipboard(client.cardNumber!!)
                au.toast("Número de tarjeta copiado al portapapeles")
              }, onClick = {})
            )
          }
        }

        Spacer(modifier = Modifier.width(Dimens.Small))

        var showResetRechargeTimerDialog by remember { mutableStateOf(false) }

        val remainingTimeForNextRechargeToBeAvailable =
          client.getRemainingTimeForNextRechargeToBeAvailable()

        if (remainingTimeForNextRechargeToBeAvailable != null) {
          Surface(modifier = Modifier.size(55.dp)) {
            RadialProgressTimeIndicator(
              value = remainingTimeForNextRechargeToBeAvailable.seconds.toFloat(),
              maxValue = 60 * 60 * 24f, //24h
              timeNumericTextRepresentation = remainingTimeForNextRechargeToBeAvailable.numericRepresentationPart,
              timeUnit = remainingTimeForNextRechargeToBeAvailable.unit,
              strokeWidthRatio = 0.3f,
              modifier = Modifier.combinedClickable(
                onClick = { },
                onLongClick = {
                  showResetRechargeTimerDialog = true
                },
              )
            )
          }
        }

        if (showResetRechargeTimerDialog) {
          AlertDialog(
            onDismissRequest = { showResetRechargeTimerDialog = false },
            title = {
              Text(text = "Reiniciar Contador?")
            },
            confirmButton = {
              TextButton(
                onClick = {
                  client.latestRechargeDateISOString = null
                  showResetRechargeTimerDialog = false
                  onClientRechargeCounterReset(client)
                }) {
                Text("Si")
              }
            },
            dismissButton = {
              TextButton(onClick = {
                showResetRechargeTimerDialog = false
              }) {
                Text("No")
              }
            },
          )
        }
      }
      if (showDivider) {
        HorizontalDivider(
          modifier = Modifier.padding(horizontal = Dimens.Large),
          color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.17f)
        )
      }
    }
  }
}

@Preview(showBackground = true, apiLevel = 33)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, apiLevel = 33)
@Composable
fun PreviewClientCard() {
  PreviewComposable(fillMaxSize = false) {
    LazyColumn {
      items(clientsForTesting) {
        ClientCard(
          client = it,
          onEdit = {},
          onSendMessage = { _, _ -> },
          onRecharge = {},
          onDelete = {},
          onClientRechargeCounterReset = {},
          au = AndroidUtilsImpl(LocalContext.current),
        )
      }
    }
  }
}