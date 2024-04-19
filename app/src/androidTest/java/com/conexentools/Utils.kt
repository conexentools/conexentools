package com.conexentools

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Telephony
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.test.platform.app.InstrumentationRegistry


class Utils {
  companion object {

    val context = InstrumentationRegistry.getInstrumentation().context

    fun getMessages(sender: String): Map<Int, String> {
      val contentResolver =
        InstrumentationRegistry.getInstrumentation().targetContext.contentResolver
      val cursor = contentResolver.query(Telephony.Sms.CONTENT_URI, null, null, null, null)!!
      val messages = mutableMapOf<Int, String>()
      if (cursor.moveToFirst()) {
        do {
          val senderName = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
          val message = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY))
          val id = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms._ID))
          if (senderName == sender)
            messages[id.toInt()] = message
        } while (cursor.moveToNext())
      }
      cursor.close()
      return messages.toSortedMap().toMap()
    }

    fun toast(
      message: String,
      duration: Int = Toast.LENGTH_LONG,
      vibrate: Boolean = false,
      waitForToastToHide: Boolean = false
    ) {
      if (message.isEmpty())
        return
      val handler = Handler(Looper.getMainLooper())

      handler.post {
        Toast.makeText(context, message, duration).show()
        log(message)
        if (vibrate)
          vibrate()
        if (waitForToastToHide)
          Thread.sleep(if (duration == Toast.LENGTH_SHORT) 2000 else 3500)
      }
    }

    fun vibrate() {
      val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java) as Vibrator
      if (vibrator.hasVibrator()) {
        vibrator.vibrate(
          VibrationEffect.createOneShot(
            500,
            VibrationEffect.DEFAULT_AMPLITUDE
          )
        ) // New vibrate method for API Level 26 or higher
      }
    }

    fun getPackageVersion(packageName: String): Pair<Long, String>? {
      return try {
        val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
        val verCode = packageInfo.longVersionCode
        val verName = packageInfo.versionName
        return Pair(verCode, verName)
      } catch (e: Exception) { //PackageManager.NameNotFoundException) {
        null
      }
    }

    fun log(message: String) = Log.i(BuildConfig.LOG_TAG, message)
    fun logError(message: String) = Log.e(BuildConfig.LOG_TAG, message)

    fun setClipboard(text: String, label: String = "") {
      val clipboard: ClipboardManager? =
        ContextCompat.getSystemService(context, ClipboardManager::class.java)
      val clip = ClipData.newPlainText(label, text)
      clipboard!!.setPrimaryClip(clip)
    }
  }
}
