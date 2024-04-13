package com.conexentools.presentation.components.screens.home.components

import android.Manifest
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.conexentools.R
import com.conexentools.domain.repository.AndroidUtils
import com.conexentools.presentation.components.common.ScrollableAlertDialog

@Composable
fun RequestAppPermissions(
  au: AndroidUtils,
  appLaunchCount: Int,
  onPermissionsRequestComplete: () -> Unit
) {

  fun isGranted(permission: String) = au.isPermissionGranted(permission)

  var checkReadSmsPermission by remember { mutableStateOf(true) }
  var checkCallAndReadContactsPermission by remember { mutableStateOf(false) }
  var checkManageExternalStoragePermission by remember { mutableStateOf(false) }
  var checkWriteExternalStoragePermission by remember { mutableStateOf(false) }
  var checkDisplayOverOtherAppsPermission by remember { mutableStateOf(false) }

  val readSmsLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
      checkCallAndReadContactsPermission = true
    }

  val callReadContactsLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) {
    checkManageExternalStoragePermission = true
  }

  val manageExternalStorageLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
      checkDisplayOverOtherAppsPermission = true
    }

  val writeExternalStorageLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
      checkDisplayOverOtherAppsPermission = true
    }

  // READ_SMS -> (CALL_PHONE - READ_CONTACTS)
  if (checkReadSmsPermission) {
    if (isGranted(Manifest.permission.READ_SMS)) {
      checkCallAndReadContactsPermission = true
      checkReadSmsPermission = false
    } else {
      ScrollableAlertDialog(stringResource(R.string.READ_SMS_PERMISSION_MESSAGE)) {
        checkReadSmsPermission = false
        readSmsLauncher.launch(Manifest.permission.READ_SMS)
      }
    }
  }

  // (CALL_PHONE - READ_CONTACTS) -> MANAGE_EXTERNAL_STORAGE | WRITE_EXTERNAL_STORAGE
  if (checkCallAndReadContactsPermission) {

    if (isGranted(Manifest.permission.CALL_PHONE) && isGranted(Manifest.permission.READ_CONTACTS)) {
      checkManageExternalStoragePermission = true
      checkCallAndReadContactsPermission = false
    } else {
      ScrollableAlertDialog(stringResource(R.string.CALL_PHONE_READ_CONTACTS_PERMISSION_MESSAGE)) {
        checkCallAndReadContactsPermission = false
        callReadContactsLauncher.launch(
          arrayOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CONTACTS,
          )
        )
      }
    }
  }

  // MANAGE_EXTERNAL_STORAGE fallbacks to WRITE_EXTERNAL_STORAGE
  if (checkManageExternalStoragePermission) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
      checkManageExternalStoragePermission = false
      checkWriteExternalStoragePermission = true
    } else if (!Environment.isExternalStorageManager() && appLaunchCount == 1) {
      ScrollableAlertDialog(stringResource(R.string.MANAGE_EXTERNAL_STORAGE_PERMISSION_MESSAGE)) {
        manageExternalStorageLauncher.launch(
          au.openSettings(
            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
            onlyReturnIntent = true
          )
        )
        checkManageExternalStoragePermission = false
      }
    } else {
      checkManageExternalStoragePermission = false
      checkDisplayOverOtherAppsPermission = true
    }
  }

  // WRITE_EXTERNAL_STORAGE -> Display over other apps
  if (checkWriteExternalStoragePermission) {
    if (isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
      checkWriteExternalStoragePermission = false
      checkDisplayOverOtherAppsPermission = true
    } else if (appLaunchCount == 1) {
      ScrollableAlertDialog(stringResource(R.string.MANAGE_EXTERNAL_STORAGE_PERMISSION_MESSAGE)) {
        writeExternalStorageLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        checkWriteExternalStoragePermission = false
      }
    }
  }

  // Display over other apps
  if (checkDisplayOverOtherAppsPermission) {
    // TODO Implement
    checkDisplayOverOtherAppsPermission = false
    onPermissionsRequestComplete()
//    if (au.canDrawOverlays()){
//      showDisplayOverOtherAppsPermissionDialog = false
//      onPermissionsRequestComplete()
//    }
  }


//  var showRuntimePermissionDialog by remember {
//    mutableStateOf(
//      Build.VERSION.SDK_INT < Build.VERSION_CODES.R && !isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
//          !isGranted(Manifest.permission.READ_SMS) ||
//          isGranted(Manifest.permission.READ_CONTACTS)
//    )
//  }
//  var showRuntimePermissionDialog by remember { mutableStateOf(false) }

//  var runtimePermissionsLauncher =
//    rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//      runtimePermissionsAttended = true
//    }
//  val permissionsRequester = Permission.requestPermissions(
//    Permission(Manifest.permission.READ_CONTACTS, context, au = au),
//    Permission(Manifest.permission.CALL_PHONE, context, au = au),
//    Permission(
//      Manifest.permission.READ_SMS, context,
//      permissionDeniedMessage = "Debe conceder el permiso a leer mensajes para poder iniciar el proceso de automatización",
//      au = au
//    ),
//    Permission(Manifest.permission.SYSTEM_ALERT_WINDOW, context, au = au),
//    Permission(
//      Manifest.permission.WRITE_EXTERNAL_STORAGE,
//      context,
//      maxSdkVersion = Build.VERSION_CODES.Q,
//      au = au
//    ),
//    componentActivityInstance = context,
//    requestPermissionsImmediately = false
//  )
}
