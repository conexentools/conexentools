package com.conexentools.domain.repository

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import contacts.core.entities.Contact

interface AndroidUtils {
  fun setClipboard(text: String, label: String = "")
  fun vibrate()
  fun toast(message: String?, duration: Int = Toast.LENGTH_LONG, vibrate: Boolean = false)
  fun openBrowser(url: String)
  fun fetchContactByUri(contactUri: Uri): Contact?
  fun call(number: String)
  fun isPermissionGranted(permission: String): Boolean
  fun canDrawOverlays(): Boolean
  fun sendWaMessage(number: String, message: String?)
  fun executeCommand(command: String, su: Boolean)
  fun composeEmail(recipientAddress: String, subject: String = "")
  fun openSettings(settingsMenu: String, onlyReturnIntent: Boolean = false): Intent
  fun hasExternalStorageWriteReadAccess(): Boolean
  fun shouldShowRequestPermissionRationale(permission: String): Boolean
  fun getPackageVersion(packageName: String): Pair<Long, String>?

  companion object {
    fun create(): AndroidUtils {
      return object : AndroidUtils {
        override fun setClipboard(text: String, label: String) {
          TODO("Not yet implemented")
        }

        override fun vibrate() {
          TODO("Not yet implemented")
        }

        override fun toast(message: String?, duration: Int, vibrate: Boolean) {
          TODO("Not yet implemented")
        }

        override fun openBrowser(url: String) {
          TODO("Not yet implemented")
        }

        override fun fetchContactByUri(contactUri: Uri): Contact? {
          TODO("Not yet implemented")
        }

        override fun call(number: String) {
          TODO("Not yet implemented")
        }

        override fun isPermissionGranted(permission: String): Boolean {
          TODO("Not yet implemented")
        }

        override fun canDrawOverlays(): Boolean {
          TODO("Not yet implemented")
        }

        override fun executeCommand(command: String, su: Boolean) {
          TODO("Not yet implemented")
        }

        override fun composeEmail(recipientAddress: String, subject: String) {
          TODO("Not yet implemented")
        }

        override fun openSettings(settingsMenu: String, onlyReturnIntent: Boolean): Intent {
          TODO("Not yet implemented")
        }

        override fun hasExternalStorageWriteReadAccess(): Boolean {
          TODO("Not yet implemented")
        }

        override fun shouldShowRequestPermissionRationale(permission: String): Boolean {
          TODO("Not yet implemented")
        }

        override fun getPackageVersion(packageName: String): Pair<Long, String>? {
          TODO("Not yet implemented")
        }

        override fun sendWaMessage(number: String, message: String?) {
          TODO("Not yet implemented")
        }
      }
    }
  }

}