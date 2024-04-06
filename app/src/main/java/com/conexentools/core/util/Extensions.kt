package com.conexentools.core.util

import android.icu.text.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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