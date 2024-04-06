package com.conexentools.presentation.components.screens.home

import android.Manifest
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.conexentools.core.app.Constants
import com.conexentools.domain.repository.AndroidUtils
import com.conexentools.presentation.components.screens.home.enums.HomeScreenPage

@Composable
fun HomeScreenFAB(
  runInstrumentationTest: () -> Unit,
  updateRechargeAvailability: () -> Unit,
  savePreferencesAction: () -> Unit,
  firstClientNumber: MutableState<String?>,
  secondClientNumber: MutableState<String?>,
  firstClientRecharge: MutableState<String?>,
  secondClientRecharge: MutableState<String?>,
  fetchDataFromWA: MutableState<Boolean>,
  pin: MutableState<String>,
  rechargesAvailabilityDateISOString: MutableState<String?>,
  page: MutableState<HomeScreenPage>,
  au: AndroidUtils,
  showAdbRunCommandDialog: MutableState<Boolean>,
  maxPinLength: MutableIntState,
  onAddClient: () -> Unit
) {
  Row(
    verticalAlignment = Alignment.Bottom, modifier = Modifier
      .size(Constants.Dimens.HomeScreenFabContainer)
  ) {

    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {

      val showAdbCommandButtonXOffset by animateFloatAsState(
        targetValue = if (page.value.isInstrumentedTestPage()) 0f else 62f,
        animationSpec = tween(durationMillis = Constants.FAB_ANIMATION_DURATION),
        label = ""
      )
      val showAdbCommandButtonAlpha by animateFloatAsState(
        targetValue = if (page.value.isInstrumentedTestPage()) 1f else 0f,
        animationSpec = tween(durationMillis = Constants.FAB_ANIMATION_DURATION + 100),
        label = ""
      )

      Spacer(modifier = Modifier.weight(1f))

      // Show ADB Command
      Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
        SmallFloatingActionButton(
          onClick = { showAdbRunCommandDialog.value = true },
          modifier = Modifier
            .alpha(showAdbCommandButtonAlpha)
            .absoluteOffset(showAdbCommandButtonXOffset.dp, 0.dp)
        ) {
          Icon(
            Icons.Rounded.Terminal, contentDescription = null
          )
        }
      }
    }

//        Spacer(modifier = Modifier.width(10.dp))

    fun canAddRecharge() =
      page.value.isInstrumentedTestPage() && !fetchDataFromWA.value && secondClientNumber.value == null

    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.Bottom,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {

      val addRechargeButtonYOffset by animateFloatAsState(
        targetValue = if (canAddRecharge()) 0f else 62f,
        animationSpec = tween(durationMillis = Constants.FAB_ANIMATION_DURATION),
        label = ""
      )
      val addRechargeButtonAlpha by animateFloatAsState(
        targetValue = if (canAddRecharge()) 1f else 0f,
        animationSpec = tween(durationMillis = Constants.FAB_ANIMATION_DURATION + 100),
        label = ""
      )

      // Add Recharge Slot
      Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
        SmallFloatingActionButton(
          onClick = {
            if (canAddRecharge()) {
              secondClientNumber.value = firstClientNumber.value
              secondClientRecharge.value = firstClientRecharge.value
            }
          },
          modifier = Modifier
            .absoluteOffset(0.dp, addRechargeButtonYOffset.dp)
            .alpha(addRechargeButtonAlpha)
        ) {
          Icon(
            Icons.Rounded.Add, contentDescription = "Add target contact"
          )
        }
      }

//          Spacer(modifier = Modifier.height(10.dp))

      Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
        FloatingActionButton(
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary,
          modifier = Modifier.alpha(if (rechargesAvailabilityDateISOString.value == null) 1f else 0.25f),
          onClick = {
            if (page.value.isInstrumentedTestPage()) {

              if (!fetchDataFromWA.value && (firstClientNumber.value!!.length != 8 || (secondClientNumber.value != null && secondClientNumber.value!!.length != 8))) {
                au.toast(
                  "El número del cliente debe tener 8 dígitos", vibrate = true
                )
                return@FloatingActionButton
              }
              if (!fetchDataFromWA.value && (firstClientRecharge.value!!.isEmpty() || firstClientRecharge.value!!.toInt() !in 25..1250 || (secondClientRecharge.value != null && secondClientRecharge.value!!.toInt() !in 25..1250))) {
                au.toast(
                  "Cada recarga debe estar entre $25 y $1250", vibrate = true
                )
                return@FloatingActionButton
              }
              if (pin.value.length != maxPinLength.intValue) {
                au.toast(
                  "El PIN debe tener ${maxPinLength.intValue} dígitos", vibrate = true
                )
                return@FloatingActionButton
              }
              if (!au.isPermissionGranted(Manifest.permission.READ_SMS)) {
                au.toast(
                  "Permiso para leer mensajes requerido", vibrate = true
                )
                return@FloatingActionButton
              }
              if (!au.canDrawOverlays()) {
                au.toast(
                  "Display pop-up windows permission is necessary to launch the instrumented test",
                  vibrate = true
                )
                return@FloatingActionButton
              }

              updateRechargeAvailability()
              savePreferencesAction()
              runInstrumentationTest()
            } else {
              onAddClient()
            }
          },
        ) {
          Icon(
            imageVector = if (page.value.isInstrumentedTestPage()) Icons.Rounded.PlayArrow else Icons.Rounded.Add,
            contentDescription = "Run Test Button"
          )
        }
      }
    }
  }
}