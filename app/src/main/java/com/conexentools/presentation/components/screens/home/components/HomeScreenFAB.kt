package com.conexentools.presentation.components.screens.home.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import com.conexentools.core.app.Constants
import com.conexentools.domain.repository.AndroidUtils
import com.conexentools.presentation.components.screens.home.enums.HomeScreenPage

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreenFAB(
  firstClientNumber: MutableState<String>,
  secondClientNumber: MutableState<String?>,
  firstClientRecharge: MutableState<String>,
  secondClientRecharge: MutableState<String>,
  fetchDataFromWA: MutableState<Boolean>,
  rechargesAvailabilityDateISOString: MutableState<String?>,
  page: MutableState<HomeScreenPage?>,
  showAdbRunCommandDialog: MutableState<Boolean>,
  onRunInstrumentedTest: () -> Unit,
  onAddClient: () -> Unit,
  onBatchAddClient: () -> Unit,
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
        targetValue = if (page.value!!.isInstrumentedTestPage()) 0f else 62f,
        animationSpec = tween(durationMillis = Constants.FAB_ANIMATION_DURATION),
        label = ""
      )
      val showAdbCommandButtonAlpha by animateFloatAsState(
        targetValue = if (page.value!!.isInstrumentedTestPage()) 1f else 0f,
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

    fun canAddRecharge() =
      page.value!!.isInstrumentedTestPage() && !fetchDataFromWA.value && secondClientNumber.value == null

    Column (
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
              if (secondClientRecharge.value.isEmpty())
                secondClientRecharge.value = firstClientRecharge.value
            }
          },
          modifier = Modifier
            .absoluteOffset(0.dp, addRechargeButtonYOffset.dp)
            .alpha(addRechargeButtonAlpha)
        ) {
          Icon(
            Icons.Rounded.Add, contentDescription = null
          )
        }
      }

      Box(
        modifier = Modifier.weight(1f),
        contentAlignment = Alignment.Center
      ) {
        FloatingActionButton(
          modifier = Modifier
            .alpha(if (page.value!!.isInstrumentedTestPage() && !rechargesAvailabilityDateISOString.value.isNullOrEmpty()) 0.25f else 1f),
          onClick = { },
        ) {
          Icon(
            imageVector = if (page.value!!.isInstrumentedTestPage()) Icons.Rounded.PlayArrow else Icons.Rounded.Add,
            contentDescription = null
          )
          Box(
            modifier = Modifier
              .fillMaxSize()
              .clipToBounds()
              .combinedClickable(
                onLongClick = {
                  if (page.value!!.isClientListPage())
                    onBatchAddClient()
                },
                onClick = {
                  if (page.value!!.isInstrumentedTestPage())
                    onRunInstrumentedTest()
                  else
                    onAddClient()
                }
              )
          )
        }
      }
    }
  }
}
