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
  var checkDisplayPopUpWindowsWhileRunningInTheBackgroundXiaomiPermission by remember {
    mutableStateOf(
      false
    )
  }
  var showAccessInterruptionsDialog by remember { mutableStateOf(false) }
  var showRestartAppDialog by remember { mutableStateOf(false) }
  var allPermissionDialogsShowed by remember { mutableStateOf(false) }

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
      // If write permission is granted restart app to make Dagger Hilt create Database in primary external storage
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager())
        showRestartAppDialog = true
      checkDisplayPopUpWindowsWhileRunningInTheBackgroundXiaomiPermission = true
    }

  val writeExternalStorageLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
      // If write permission is granted restart app to make Dagger Hilt create Database in primary external storage
      if (au.isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE))
        showRestartAppDialog = true
      checkDisplayPopUpWindowsWhileRunningInTheBackgroundXiaomiPermission = true
    }

  val xiaomiOtherPermissionAppSettingsWindowLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
      showAccessInterruptionsDialog = true
    }

  val accessInterruptionsScreenLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
      allPermissionDialogsShowed = true
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

  // (CALL_PHONE - READ_CONTACTS) -> MANAGE_EXTERNAL_STORAGE
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
    } else if (appLaunchCount == 1 && !Environment.isExternalStorageManager()) {
      ScrollableAlertDialog(stringResource(R.string.MANAGE_EXTERNAL_STORAGE_PERMISSION_MESSAGE)) {
        val intent = au.openSettings(
          settingsMenuWindow =  Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
          flagActivityNewTask = false,
          onlyReturnIntent = true
        )
        manageExternalStorageLauncher.launch(intent)
        checkManageExternalStoragePermission = false
      }
    } else {
      checkManageExternalStoragePermission = false
      checkDisplayPopUpWindowsWhileRunningInTheBackgroundXiaomiPermission = true
    }
  }

  // WRITE_EXTERNAL_STORAGE -> Xiaomi display in background
  if (checkWriteExternalStoragePermission) {
    if (isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE) || appLaunchCount != 1) {
      checkWriteExternalStoragePermission = false
      checkDisplayPopUpWindowsWhileRunningInTheBackgroundXiaomiPermission = true
    } else {
      ScrollableAlertDialog(stringResource(R.string.MANAGE_EXTERNAL_STORAGE_PERMISSION_MESSAGE)) {
        writeExternalStorageLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        checkWriteExternalStoragePermission = false
      }
    }
  }

  // Xiaomi Display pop-up while running in the background -> Access Interruptions screen
  if (checkDisplayPopUpWindowsWhileRunningInTheBackgroundXiaomiPermission) {
    if (appLaunchCount == 1 && au.isMiuiWithApi28OrMore()) {
      ScrollableAlertDialog("Para ejecutar las pruebas automatizadas desde una computadora usando ADB cuando lo aplicación no se esté ejecutando en un primer plano, es necesario otorgar el permiso 'Mostrar ventanas emergente mientras se ejecuta en segundo plano'. A continuación si lo desea concédalo manualmente") {
        val intent = au.openXiaomiOtherPermissionAppSettingsWindow(flagActivityNewTask = false, onlyReturnIntent = true)
        xiaomiOtherPermissionAppSettingsWindowLauncher.launch(intent)
        checkDisplayPopUpWindowsWhileRunningInTheBackgroundXiaomiPermission = false
      }
    } else {
      checkDisplayPopUpWindowsWhileRunningInTheBackgroundXiaomiPermission = false
      showAccessInterruptionsDialog = true
    }
  }

  if (showAccessInterruptionsDialog) {
    if (!au.isNotificationPolicyAccessGranted()) {
      ScrollableAlertDialog("Para ejecutar las pruebas automatizadas es necesario poder activar y desactivar|restaurar según sea necesario el modo 'No Molestar' pues algunas notificaciones flotantes podrían hacer fallar las pruebas, a continuación conceda el acceso a interrupciones para Conexen Tools") {
        val intent = au.openSettings(
          settingsMenuWindow = Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS,
          flagActivityNewTask = false,
          onlyReturnIntent = true,
          includePackage = false
        )
        accessInterruptionsScreenLauncher.launch(intent)
        showAccessInterruptionsDialog = false
      }
    } else {
      showAccessInterruptionsDialog = false
      allPermissionDialogsShowed = true
    }
  }

  if (allPermissionDialogsShowed) {
    if (showRestartAppDialog) {
      ScrollableAlertDialog(
        text = "Reinicia la aplicación ahora para cargar la base de datos desde la carpeta '${
          stringResource(
            R.string.app_name
          )
        }' en el almacenamiento interno",
        confirmButtonText = "Reiniciar"
      ) {
        au.restartApp()
      }
    } else {
      onPermissionsRequestComplete()
    }
  }
}
