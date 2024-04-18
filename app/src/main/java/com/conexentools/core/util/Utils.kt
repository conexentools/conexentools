package com.conexentools.core.util

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.view.KeyEvent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import com.conexentools.BuildConfig
import com.conexentools.domain.repository.AndroidUtils
import contacts.core.entities.Contact
import java.time.Duration
import java.time.Instant

fun getImageByResourceID(
  resourceID: Int = -1,
  tintColor: Color? = null
): @Composable (() -> Unit)? {
  return if (resourceID == -1) null else {
    {
      Image(
        painter = painterResource(id = resourceID),
        colorFilter = tintColor?.let { ColorFilter.tint(it) },
        contentDescription = ""
      )
    }
  }
}

@Composable
fun pickContact(au: AndroidUtils, onResult: (Contact?) -> Unit): ManagedActivityResultLauncher<Void?, Uri?> {
  return rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickContact()
  ) {
    val contact: Contact? = it?.let(au::fetchContactByUri)
    onResult(contact)
  }
}

fun getRemainingTimeUntilDate(dateISOString: String?): RemainingTimeTextRepresentation? {
  return getRemainingTimeUntilDate(dateISOString?.toInstant())
}

fun getRemainingTimeUntilDate(date: Instant?): RemainingTimeTextRepresentation? {

  if (date == null)
    return null

  val now = Instant.now()
  val duration = Duration.between(now, date)
  if (duration.isNegative)
    return null

  val days = duration.toDays()
  val hours = duration.toHours()
  val minutes = duration.toMinutes()
  val seconds = duration.toMillis() / 1000

  val numberUnit = if (days > 0)
    Pair(days, "d")
  else if (hours > 0)
    Pair(hours, "h")
  else if (minutes > 0)
    Pair(minutes, "m")
  else
    Pair(seconds, "s")

  return RemainingTimeTextRepresentation(
    seconds,
    numberUnit.first.toString(),
    numberUnit.second
  )
}

fun log(message: Any) = Log.i(BuildConfig.LOG_TAG, message.toString())
fun logError(message: Any) = Log.e(BuildConfig.LOG_TAG, message.toString())
