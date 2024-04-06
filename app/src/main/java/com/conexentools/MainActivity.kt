package com.conexentools

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.conexentools.core.util.Permission
import com.conexentools.domain.repository.AndroidUtils
import com.conexentools.presentation.HomeScreenViewModel
import com.conexentools.presentation.components.common.enums.AppTheme
import com.conexentools.presentation.navigation.Screen
import com.conexentools.presentation.navigation.SetUpNavGraph
import com.conexentools.presentation.theme.ConexenToolsTheme
import com.conexentools.presentation.theme.DarkTheme
import com.conexentools.presentation.theme.LocalTheme
import contacts.core.entities.Contact
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  @Inject
  lateinit var au: AndroidUtils

  @SuppressLint("InlinedApi")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    lateinit var navController: NavHostController


//    Permission.requestManageExternalStoragePermission(this)

    setContent {

      val hvm: HomeScreenViewModel = hiltViewModel()

      val darkTheme = when (hvm.appTheme.value) {
        AppTheme.MODE_AUTO -> DarkTheme(isSystemInDarkTheme())
        AppTheme.MODE_DAY -> DarkTheme(false)
        AppTheme.MODE_NIGHT -> DarkTheme(true)
      }

      CompositionLocalProvider(LocalTheme provides darkTheme) {
        ConexenToolsTheme(
          darkTheme = darkTheme.isDark
        ) {
          //val hvm: HomeScreenViewModel = viewModel(LocalContext.current as ComponentActivity),

//        mainViewModel = viewModel(factory = MainViewModelFactory(context = this))
          Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
          ) {
            navController = rememberNavController()
            SetUpNavGraph(
              navController = navController,
              startDestination = Screen.Home,
              au = au
            )
//          val state by hvm.state.collectAsState()


//          ClientListTestingComposable()

//          homeViewModel.state.collect{

          }
//        val state: HomeState = homeViewModel.state.collect()
//        var state: HomeState = HomeState(HomeLoadingState.Loading)
//        LaunchedEffect(homeViewModel.state) {
//          homeViewModel.viewModelScope.launch {
//            homeViewModel.state.collect { state = it }
//          }
//        }


//          }
//        }
//        homeViewModel.state.collectAsState()
//        homeViewModel.viewModelScope.launch {
//
//        }

//          CheckPermissions(popUpWindowPermissionActivityLauncher)
        }
      }
    }
  }
}

//  @Composable
//  private fun CheckPermissions(
//    popUpWindowPermissionActivityLauncher: ActivityResultLauncher<Intent>,
//    permissionsRequester: (() -> Unit)?
//  ) {
////    permissionsRequester?.let { it() }
//    val permissionRequesterLauncher = rememberLauncherForActivityResult(
//      contract = ActivityResultContracts.PickContact()
//    ) {
//      val contact: Contact? = it?.let(au::fetchContactByUri)
//      onResult(contact)
//    }
//
////    val permissionsRequester = Permission.requestPermissions(
////      Permission(Manifest.permission.READ_CONTACTS, this, au = au),
////      Permission(Manifest.permission.CALL_PHONE, this, au = au),
////      Permission(
////        Manifest.permission.READ_SMS, this,
////        permissionDeniedMessage = "Debe conceder el permiso a leer mensajes para poder iniciar el proceso de automatización",
////        au = au
////      ),
////      Permission(Manifest.permission.SYSTEM_ALERT_WINDOW, this, au = au),
////      Permission(
////        Manifest.permission.WRITE_EXTERNAL_STORAGE,
////        this,
////        maxSdkVersion = Build.VERSION_CODES.Q,
////        au = au
////      ),
////      componentActivityInstance = this,
////      requestPermissionsImmediately = false
////    )
//
////    var showDisplayPopUpInBackgroundPermissionAlertDialog by remember { mutableStateOf(true) }
//
//    if (!Settings.canDrawOverlays(this)) {
//
//      val xiaomiPopUpWindowPermissionActivityLauncher =
//        GetStartActivityForResultLauncher {
//          GetStartActivityForResultLauncher
//        }
////        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//////          log("xiaomiPopUpWindowPermissionActivityLauncher result: $it")
////          if ("xiaomi" == Build.MANUFACTURER.lowercase(Locale.ROOT)) { //TODO && Android SDK < 11
////            val intent = Intent("miui.intent.action.APP_PERM_EDITOR")
////            intent.setClassName(
////              "com.miui.securitycenter",
////              "com.miui.permcenter.permissions.PermissionsEditorActivity"
////            )
////            intent.putExtra("extra_pkgname", packageName)
////            startActivity(intent)
//////      specialPermissionsAppMenuActivityLauncher.launch(intent)
////          }
////        }
//
//      var showDisplayPopUpPermissionAlertDialog by remember { mutableStateOf(true) }
//      if (showDisplayPopUpPermissionAlertDialog) {
//        AlertDialog(
//          onDismissRequest = { },
//          text = { Text(text = "A continuación acepte el permiso 'Mostrar sobre otras aplicaciones' o relacionado y posteriormente el permiso 'Mostrar sobre otras aplicaciones mientras se ejecuta en segundo plano' o relacionado.") },
//          confirmButton = {
//            TextButton(onClick = {
//              showDisplayPopUpPermissionAlertDialog = false
//              val intent =
//                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, "package:$packageName".toUri())
//
//              val permissionRequester = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//
//                // Launching xiaomi app special permissions form
//                if ("xiaomi" == Build.MANUFACTURER.lowercase(Locale.ROOT)) { //TODO && Android SDK < 11
//                  val intent = Intent("miui.intent.action.APP_PERM_EDITOR")
//                  intent.setClassName(
//                    "com.miui.securitycenter",
//                    "com.miui.permcenter.permissions.PermissionsEditorActivity"
//                  )
//                  intent.putExtra("extra_pkgname", packageName)
//
//                  registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
//
//                    // Requesting all file access permission
//                  }
//                  startActivity(intent)
////      specialPermissionsAppMenuActivityLauncher.launch(intent)
//                }
//              }
//              xiaomiPopUpWindowPermissionActivityLauncher.launch(intent)
//              rememberLauncherForActivityResult(
//                ActivityResultContracts.StartActivityForResult(),
//                {})
//              popUpWindowPermissionActivityLauncher.launch()
//
//            }) {
//              Text("OK")
//            }
//          },
//        )
//      }
//    }
//  }
//}

//@Composable
//fun GetStartActivityForResultLauncher(onResult: (ActivityResult) -> Unit): ManagedActivityResultLauncher<Intent, ActivityResult> {
//  return rememberLauncherForActivityResult(
//    contract = ActivityResultContracts.StartActivityForResult(),
//    onResult = onResult
//  )
//}