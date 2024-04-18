package com.conexentools

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Telephony
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.test.platform.app.InstrumentationRegistry


class Utils {
  companion object {
    private var isProcessingToast = false
    private var latestToastMessage = ""

    val context = InstrumentationRegistry.getInstrumentation().targetContext

    fun getMessages(sender: String): Map<Int, String> {
      val contentResolver = InstrumentationRegistry.getInstrumentation().targetContext.contentResolver
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

    fun toast(message: String, duration: Int = Toast.LENGTH_LONG, vibrate: Boolean = false, waitForToastToHide: Boolean = false) {
//      val handler = Handler(Looper.getMainLooper())
      if (message.isEmpty() || isProcessingToast && latestToastMessage == message)
        return
      isProcessingToast = true
      latestToastMessage = message
      val t = Toast.makeText(context, message, duration)
//      handler.post {
//        Toast.makeText(
//          context, message, duration
//        ).show()
//      }

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) t.addCallback(object: Toast.Callback() {
        override fun onToastHidden() {
          isProcessingToast = false
        }
      })
      else
        isProcessingToast = false
      t.show()
      log(message)
      if (vibrate)
        vibrate()
      if (waitForToastToHide)
        while(isProcessingToast)
          Thread.sleep(1000)
      //        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> Toast.makeText(InstrumentationRegistry.getInstrumentation().getContext(), message, duration).show());
    }

    fun vibrate() {
      val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java) as Vibrator
      if (vibrator.hasVibrator()) {
        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)) // New vibrate method for API Level 26 or higher
      }
    }

    fun getPackageVersion(packageName: String): Pair<Long, String>? {
      return try {
        val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
        val verCode = packageInfo.longVersionCode
        val verName = packageInfo.versionName
        return Pair(verCode, verName)
      } catch (e: Exception){ //PackageManager.NameNotFoundException) {
        null
      }
    }

    fun log(message: String, isError: Boolean = false) {
      if (isError)
        Log.e(BuildConfig.LOG_TAG, message)
      else
        Log.i(BuildConfig.LOG_TAG, message)
    }

    fun setClipboard(text: String, label: String = "") {
      val clipboard: ClipboardManager? =
        ContextCompat.getSystemService(context, ClipboardManager::class.java)
      val clip = ClipData.newPlainText(label, text)
      clipboard!!.setPrimaryClip(clip)
    }
  }
}
