package com.conexentools.data.model

import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.conexentools.domain.repository.AndroidUtils

class Permission(
  val permission: String,
  private val componentActivityInstance: ComponentActivity,
  permissionGrantedMessage: String? = null,
  permissionDeniedMessage: String? = null,
  au: AndroidUtils,
  var permissionGrantedAction: () -> Unit = {
    au.toast(
      permissionGrantedMessage
    )
  },
  val maxSdkVersion: Int? = null,
  var permissionDeniedAction: () -> Unit = {
    au.toast(
      permissionDeniedMessage,
      vibrate = true
    )
  },
//  shouldShowRequestPermissionRationaleAction: (() -> Unit)? = null
) {

  //  val shouldShowRequestPermissionRationaleAction: (() -> Unit)? = shouldShowRequestPermissionRationaleAction

  companion object {
    fun requestPermissions(
      vararg permissions: Permission,
      componentActivityInstance: ComponentActivity,
      requestPermissionsImmediately: Boolean = true
    ): (() -> Unit)? {
      val notGrantedPermissions = permissions.filterNot { it.isGranted() }
      if (notGrantedPermissions.isEmpty())
        return null
      val activityLauncher =
        componentActivityInstance.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
          for (result in it.map { r -> r.value }.zip(notGrantedPermissions))
            if (result.first) result.second.permissionGrantedAction() else result.second.permissionDeniedAction()
        }
      val launcher: () -> Unit =
        { activityLauncher.launch(notGrantedPermissions.map { it.permission }.toTypedArray()) }
      return if (requestPermissionsImmediately) {
        launcher()
        null
      } else
        launcher
    }
  }

  fun isGranted(): Boolean {
    return (maxSdkVersion != null && Build.VERSION.SDK_INT > maxSdkVersion) ||
        ContextCompat.checkSelfPermission(
          componentActivityInstance,
          permission
        ) == PackageManager.PERMISSION_GRANTED
  }

  fun requestPermission() {
    if (isGranted())
      return

    val requestPermissionLauncher = componentActivityInstance.registerForActivityResult(
      ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean -> if (isGranted) permissionGrantedAction() else permissionDeniedAction() }
    requestPermissionLauncher.launch(permission)
    /*
    when {
      ActivityCompat.shouldShowRequestPermissionRationale(componentActivityInstance, permission) -> {
        shouldShowRequestPermissionRationaleAction?.let{it()}
        // In an educational UI, explain to the user why your app requires this
        // permission for a specific feature to behave as expected, and what
        // features are disabled if it's declined. In this UI, include a
        // "cancel" or "no thanks" button that lets the user continue
        // using your app without granting the permission.
      }
      else -> {
        requestPermissionLauncher.launch(permission)
      }
    }
    */
  }
}