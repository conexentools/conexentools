package com.conexentools

import android.app.NotificationManager
import android.app.NotificationManager.INTERRUPTION_FILTER_PRIORITY
import android.app.UiAutomation
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.provider.Telephony
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.LENGTH_SHORT
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.net.toUri
import androidx.test.platform.app.InstrumentationRegistry
import java.util.regex.Pattern


class Utils {
  companion object {

    val context = InstrumentationRegistry.getInstrumentation().context
    val notificationManager = getSystemService(context, NotificationManager::class.java) as NotificationManager
    val userSetInterruptionFilter = notificationManager.currentInterruptionFilter

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
      isShortToast: Boolean = false,
      vibrate: Boolean = false,
      waitForToastToHide: Boolean = false
    ) {
      if (message.isEmpty())
        return
      val handler = Handler(Looper.getMainLooper())

      handler.post {
        Toast.makeText(context, message, if (isShortToast) LENGTH_SHORT else LENGTH_LONG).show()
        log(message)
        if (vibrate)
          vibrate()
      }
      if (waitForToastToHide)
        Thread.sleep(if (isShortToast) 2000 else 3500)
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

    fun printPackageVersionInfo(
      name: String,
      installedVersion: Pair<Long, String>?,
      testedVersion: Pair<Long, String>,
    ) {
      if (installedVersion == null)
        toast("$name is not installed", vibrate = true, waitForToastToHide = true)
      else
        toast("Installed $name:\n\t" +
            "Version Code: ${installedVersion.first}\n\t" +
            "Version Name: ${installedVersion.second}", waitForToastToHide = true)

      toast("Tested $name:\n\t" +
          "Version Code: ${testedVersion.first}\n\t" +
          "Version Name: ${testedVersion.second}", waitForToastToHide = true)
    }

    fun getPatternForResourceID(resourceName: String): Pattern {
      return Pattern.compile(".*:id/$resourceName")
    }

    fun UiAutomation.grantRuntimePermissions(packageName: String, vararg permissions: String) {
      permissions.forEach {
        grantRuntimePermission(packageName, it)
      }
    }

    fun turnOnDoNotDisturbMode(){
      if (notificationManager.currentInterruptionFilter != INTERRUPTION_FILTER_PRIORITY)
        notificationManager.setInterruptionFilter(INTERRUPTION_FILTER_PRIORITY)
    }

    fun restoreInterruptionFilter() {
      if (notificationManager.currentInterruptionFilter != userSetInterruptionFilter)
        notificationManager.setInterruptionFilter(userSetInterruptionFilter)
    }

    fun openAccessToInterruptionsScreen() {
      val intent = Intent(
        Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
      ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    }
  }
}
