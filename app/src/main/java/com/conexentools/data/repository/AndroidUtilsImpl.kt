package com.conexentools.data.repository

import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_NO_HISTORY
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.ContactsContract
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.net.toUri
import com.conexentools.BuildConfig
import com.conexentools.core.util.log
import com.conexentools.core.util.logError
import com.conexentools.domain.repository.AndroidUtils
import contacts.core.Contacts
import contacts.core.entities.Contact
import contacts.core.equalTo
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URLEncoder
import javax.inject.Inject


class AndroidUtilsImpl @Inject constructor(
  @ApplicationContext private val context: Context,
) : AndroidUtils {

  private var isProcessingToast = false
  private var latestToastMessage = ""

  override fun setClipboard(text: String, label: String) {
    val clipboard: ClipboardManager? = getSystemService(context, ClipboardManager::class.java)
    val clip = ClipData.newPlainText(label, text)
    clipboard!!.setPrimaryClip(clip)
  }

  override fun vibrate() {
    val vibrator = getSystemService(context, Vibrator::class.java) as Vibrator
    if (vibrator.hasVibrator()) {
      vibrator.vibrate(
        VibrationEffect.createOneShot(
          500,
          VibrationEffect.DEFAULT_AMPLITUDE
        )
      )
    }
  }

  override fun toast(
    message: String?,
    shortToast: Boolean,
    vibrate: Boolean
  ) {
    if (message.isNullOrBlank() || isProcessingToast && latestToastMessage == message)
      return
    isProcessingToast = true
    latestToastMessage = message
    val t = Toast.makeText(context, message, if (shortToast) Toast.LENGTH_SHORT else Toast.LENGTH_LONG)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) t.addCallback(object : Toast.Callback() {
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
  }

  override fun openBrowser(url: String) {
    val urlIntent = Intent(
      Intent.ACTION_VIEW,
      url.toUri()
    )
    urlIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    ContextCompat.startActivity(context, urlIntent, null)
  }

  override fun fetchContactByUri(contactUri: Uri): Contact? {
    val cr = context.contentResolver
    val queryFields = arrayOf(ContactsContract.Contacts._ID)
    var cursor: Cursor? = null
    var contact: Contact? = null
    try {
      cursor = cr.query(contactUri, queryFields, null, null, null)!!
      cursor.moveToFirst()
      val contactId =
        cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
      contact = Contacts(context)
        .query()
        .where { Contact.Id equalTo contactId }
        .find().first()

    } catch (ex: Exception) {
      logError("Error fetching contact by uri")
      logError(ex.toString())
      logError(ex.stackTraceToString())
    } finally {
      cursor?.close()
    }
    return contact
  }

  override fun call(number: String) {
    try {
      val callIntent = Intent(Intent.ACTION_CALL, "tel:$number".toUri())
        .apply {
          addFlags(FLAG_ACTIVITY_NEW_TASK)
          addFlags(FLAG_ACTIVITY_NO_HISTORY)
          addFlags(FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }
      context.startActivity(callIntent)
    } catch (ex: Exception) {
      toast("Unable to make the call to number: $number", vibrate = true)
      toast(ex.message)
      ex.printStackTrace()
    }
  }

  override fun executeCommand(command: String, su: Boolean): Int {
    var c = command
    try {
      if (su)
        c = "su -c $c"

      log(c)
      val process = Runtime.getRuntime().exec(c)

      // Wait for the process to finish
      process.waitFor()

      // Read the output (if any)
      val reader = BufferedReader(InputStreamReader(process.inputStream))
      var line: String?
      while (reader.readLine().also { line = it } != null) {
        println(line)
      }
      val readerE = BufferedReader(InputStreamReader(process.errorStream))
      var line2: String?
      while (readerE.readLine().also { line2 = it } != null) {
        System.err.println(line2)
      }
      // Check the exit value (0 for success)
      val exitValue = process.exitValue()
      println("Process exited with value: $exitValue")
      return exitValue
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return -1
  }

  override fun isPermissionGranted(permission: String): Boolean = ContextCompat.checkSelfPermission(
    context,
    permission
  ) == PackageManager.PERMISSION_GRANTED

  override fun canDrawOverlays(): Boolean = Settings.canDrawOverlays(context)

  override fun sendWaMessage(number: String, message: String?) {
    val packageManager: PackageManager = context.packageManager
    try {
      var uri = "https://wa.me/$number"
      if (!message.isNullOrEmpty()) {
        uri += "?text=" + URLEncoder.encode(message, "UTF-8")
      }
      val intent = Intent(Intent.ACTION_VIEW)
      intent.setPackage("com.whatsapp")
      intent.setData(uri.toUri())
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      if (intent.resolveActivity(packageManager) != null) {
        context.startActivity(intent)
      }
    } catch (e: java.lang.Exception) {
      e.printStackTrace()
    }
  }

  override fun hasExternalStorageReadWriteAccess(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager() ||
        Build.VERSION.SDK_INT < Build.VERSION_CODES.R &&
        isPermissionGranted(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
        isPermissionGranted(android.Manifest.permission.READ_EXTERNAL_STORAGE)
  }

  override fun composeEmail(recipientAddress: String, subject: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
      data = Uri.parse("mailto:$recipientAddress")
      putExtra(Intent.EXTRA_SUBJECT, subject)
      addFlags(FLAG_ACTIVITY_NEW_TASK)
      addFlags(FLAG_ACTIVITY_NO_HISTORY)
      addFlags(FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    }

    if (intent.resolveActivity(context.packageManager) != null) {
      context.startActivity(intent)
    } else {
      toast("Al parecer no tiene instalado ningún cliente de correo electrónico", vibrate = true)
    }
  }

  override fun openSettings(
    settingsMenuWindow: String,
    flagActivityNewTask: Boolean,
    onlyReturnIntent: Boolean,
    includePackage: Boolean
  ): Intent {
    val intent = Intent(
      settingsMenuWindow
    ).apply {
      if (includePackage)
        data = "package:${BuildConfig.APPLICATION_ID}".toUri()
      addFlags(FLAG_ACTIVITY_NO_HISTORY)
      addFlags(FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
      if (flagActivityNewTask)
        addFlags(FLAG_ACTIVITY_NEW_TASK)
    }

    if (!onlyReturnIntent)
      context.startActivity(intent)
    return intent
  }

  override fun getPackageVersion(packageName: String): Pair<Long, String>? {
    return try {
      val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
      val verCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageInfo.longVersionCode
      } else {
        -1
      }
      val verName = packageInfo.versionName
      return Pair(verCode, verName)
    } catch (e: Exception){
      null
    }
  }

  override fun restartApp() {
    val pm: PackageManager = context.packageManager
    val intent = pm.getLaunchIntentForPackage(context.packageName)
    val mainIntent = Intent.makeRestartActivityTask(intent!!.component)
    context.startActivity(mainIntent)
    Runtime.getRuntime().exit(0)
  }

  override fun launchPackage(packageName: String, clearOutPreviousInstances: Boolean): Boolean {
    val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false

    // Clear out any previous instances
    if (clearOutPreviousInstances)
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
    context.startActivity(intent)
    return true
  }

  override fun openXiaomiOtherPermissionAppSettingsWindow(
    flagActivityNewTask: Boolean,
    onlyReturnIntent: Boolean
    ): Intent {
    val intent = Intent("miui.intent.action.APP_PERM_EDITOR")
    intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity")
    intent.putExtra("extra_pkgname", context.packageName).apply {
      addFlags(FLAG_ACTIVITY_NO_HISTORY)
      addFlags(FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
      if (flagActivityNewTask)
        addFlags(FLAG_ACTIVITY_NEW_TASK)
    }
    if (!onlyReturnIntent)
      context.startActivity(intent)
    return intent
  }

  override fun isMiuiWithApi28OrMore(): Boolean {
    val isMiUi = getSystemProperty("ro.miui.ui.version.name")?.isNotBlank() == true
    return isMiUi && Build.VERSION.SDK_INT >= 28
  }

  override fun getSystemProperty(propName: String): String? {
    val line: String
    var input: BufferedReader? = null
    try {
      val p = Runtime.getRuntime().exec("getprop $propName")
      input = BufferedReader(InputStreamReader(p.inputStream), 1024)
      line = input.readLine()
      input.close()
    } catch (ex: IOException) {
      return null
    } finally {
      if (input != null) {
        try {
          input.close()
        } catch (e: IOException) {
          e.printStackTrace()
        }
      }
    }
    return line
  }

  override fun isNotificationPolicyAccessGranted(): Boolean {
    val notificationManager = getSystemService(context, NotificationManager::class.java) as NotificationManager
    return notificationManager.isNotificationPolicyAccessGranted
  }
}

