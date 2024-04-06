package com.conexentools.core.util

import android.icu.text.DateFormat
import android.view.KeyEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import java.time.Instant
import java.util.Date

@Composable
fun <viewModel : LifecycleObserver> viewModel.ObserveLifecycleEvents(lifecycle: Lifecycle) {
  DisposableEffect(lifecycle) {
    lifecycle.addObserver(this@ObserveLifecycleEvents)
    onDispose {
      lifecycle.removeObserver(this@ObserveLifecycleEvents)
    }
  }
}

fun String.truncate(maxLength: Int): String {
  return if (this.length > maxLength) {
    this.substring(0, maxLength - 3) + "..."
  } else {
    this
  }
}

fun String.toFormattedDate(): String {
  val date = Date.from(Instant.parse(this))
  return DateFormat.getDateTimeInstance().format(date)
}

fun String?.toInstant(): Instant? {
  return if (this == null)
    null
  else
    Instant.parse(this)
}

fun Modifier.moveFocusOnTabPressed(direction: FocusDirection, focusManager: FocusManager): Modifier {
  return then(onPreviewKeyEvent {
    if (it.key == Key.Tab && it.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
      focusManager.moveFocus(direction)
      true
    } else {
      false
    }
  })
}