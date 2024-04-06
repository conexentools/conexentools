package com.conexentools.domain.repository

import android.net.Uri
import android.widget.Toast
import com.conexentools.data.model.RemainingTimeTextRepresentation
import contacts.core.entities.Contact
import java.time.Instant

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

        override fun sendWaMessage(number: String, message: String?) {
          TODO("Not yet implemented")
        }
      }
    }
  }

}