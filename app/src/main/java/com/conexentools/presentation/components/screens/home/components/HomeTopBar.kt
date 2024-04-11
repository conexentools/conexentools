package com.conexentools.presentation.components.screens.home.components

import android.Manifest
import android.content.res.Configuration
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.ContactMail
import androidx.compose.material.icons.rounded.QueuePlayNext
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.conexentools.R
import com.conexentools.core.util.PreviewComposable
import com.conexentools.domain.repository.AndroidUtils
import com.conexentools.presentation.components.screens.home.enums.HomeScreenPage
import com.conexentools.presentation.components.screens.home.pages.client_list.SearchAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
  onSearchBarTextChange: (String) -> Unit,
  onSettings: () -> Unit,
  onAbout: () -> Unit,
  onHelp: () -> Unit,
  onPageChange: (HomeScreenPage) -> Unit,
  page: MutableState<HomeScreenPage>,
  au: AndroidUtils
) {
  Column(
    modifier = Modifier.height(65.dp)
  ) {

    var isSearchingClients by remember { mutableStateOf(false) }
    var isMoreDropDownMenuExpanded by remember { mutableStateOf(false) }

    val writeExternalStorageLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    AnimatedVisibility(
      visible = !isSearchingClients,
    ) {
      TopAppBar(
        title = {
          Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
          )
        },
        colors = topAppBarColors(
          containerColor = Color.Transparent,
          titleContentColor = MaterialTheme.colorScheme.onBackground,
          actionIconContentColor = MaterialTheme.colorScheme.primary
        ),
        actions = {
          Row (modifier = Modifier.animateContentSize()){
            // Page Switcher Button
            IconButton(
              onClick = {
                onPageChange(
                  if (page.value.isInstrumentedTestPage())
                    HomeScreenPage.CLIENT_LIST
                  else {
                    if (isSearchingClients)
                      isSearchingClients = false
                    HomeScreenPage.INSTRUMENTED_TEST
                  }
                )
              }) {
              Icon(
                imageVector = if (page.value.isInstrumentedTestPage()) Icons.Rounded.ContactMail else Icons.Rounded.QueuePlayNext,
                contentDescription = null,
              )
            }

            // Search Button
            AnimatedVisibility(
              visible = page.value == HomeScreenPage.CLIENT_LIST,
              enter = fadeIn(animationSpec = tween(durationMillis = 200)),
              exit = fadeOut(animationSpec = tween(durationMillis = 200))
            ) {
              IconButton(
                onClick = {
                  isSearchingClients = true
                }) {
                Icon(
                  imageVector = Icons.Default.Search,
                  contentDescription = null,
                )
              }
            }
          }

          // More Button
          IconButton(
            onClick = {
              isMoreDropDownMenuExpanded = !isMoreDropDownMenuExpanded
            }) {
            Icon(
              imageVector = Icons.Default.MoreVert,
              contentDescription = null,
            )

            // More DropDownMenu
            DropdownMenu(
              expanded = isMoreDropDownMenuExpanded,
              onDismissRequest = { isMoreDropDownMenuExpanded = false },
              modifier = Modifier.defaultMinSize(minWidth = 80.dp)
            ) {
              DropdownMenuItem(
                text = { Text("Configuración") },
                onClick = onSettings
              )
              DropdownMenuItem(
                text = { Text("Acerca de") },
                onClick = onAbout
              )
              DropdownMenuItem(
                text = { Text("Ayuda") },
                onClick = onHelp
              )
              if (!au.hasExternalStorageWriteReadAccess()){
                DropdownMenuItem(
                  text = { Text("Solicitar permiso para escribir en el almacenamiento externo") },
                  onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()){
                      au.openSettings(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    } else if (au.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                      writeExternalStorageLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    } else {
                      // User has selected Deny and don't ask again
                      au.openSettings(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    }
                  }
                )
              }
            }
          }
        }
      )
    }

    AnimatedVisibility(
      visible = isSearchingClients,
//      enter = slideInHorizontally(
//        initialOffsetX = { it },
//        animationSpec = tween(durationMillis = 300)
//      ) + expandVertically(
//        animationSpec = tween(delayMillis = 300)
//      ),
      exit = slideOutHorizontally(
        targetOffsetX = { -it },
        animationSpec = tween(durationMillis = 300)
      ) + shrinkVertically(
        animationSpec = tween(delayMillis = 300)
      )
    ) {
      SearchAppBar(
        onTextChange = onSearchBarTextChange,
        onNavigateBack = { isSearchingClients = false },
      )
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f))
  }
}

@Preview(showBackground = true, apiLevel = 33)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, apiLevel = 33)
@Composable
fun PreviewHomeTopBar() {
  PreviewComposable(fillMaxSize = false) {
    HomeTopBar(
      page = remember {
        mutableStateOf(HomeScreenPage.INSTRUMENTED_TEST)
      },
      onSearchBarTextChange = {},
      onPageChange = {},
      onSettings = {},
      onAbout = {},
      onHelp = {},
      au = AndroidUtils.create()
    )
  }
}