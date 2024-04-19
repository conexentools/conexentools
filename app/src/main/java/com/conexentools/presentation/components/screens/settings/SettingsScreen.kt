package com.conexentools.presentation.components.screens.settings

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.conexentools.core.app.Constants
import com.conexentools.core.util.PreviewComposable
import com.conexentools.presentation.components.common.LabelSwitch
import com.conexentools.presentation.components.common.ScreenSurface
import com.conexentools.presentation.components.common.ScrollableAlertDialog
import com.conexentools.presentation.components.common.enums.AppTheme
import com.conexentools.presentation.components.common.enums.ScreenSurfaceContentContainer
import com.conexentools.presentation.theme.LocalTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  appTheme: MutableState<AppTheme>,
  alwaysWaMessageByIntent: MutableState<Boolean>,
  savePin: MutableState<Boolean>,
  onNavigateBack: () -> Unit
) {

  ScreenSurface(
    title = "Configuración",
    surfaceModifier = Modifier.widthIn(0.dp, 350.dp),
    lazyColumnHorizontalAlignment = Alignment.Start,
    contentContainer = ScreenSurfaceContentContainer.LazyColumn,
    onNavigateBack = onNavigateBack,
    ) {
    val themeItems by remember {
      mutableStateOf(
        listOf(
          RadioButtonItem(
            id = AppTheme.MODE_DAY.ordinal,
            title = "Claro",
          ),
          RadioButtonItem(
            id = AppTheme.MODE_NIGHT.ordinal,
            title = "Oscuro",
          ),
          RadioButtonItem(
            id = AppTheme.MODE_AUTO.ordinal,
            title = "Sistema",
          ),
        )
      )
    }

    fun themeTitle(id: Int) = themeItems[id].title

    var selectedItem by remember { mutableStateOf(themeTitle(appTheme.value.ordinal)) }

    Spacer(modifier = Modifier.height(Constants.Dimens.Large))

    var showThemeSelectorDialog by remember { mutableStateOf(false) }
    var showPinAlertDialog by remember { mutableStateOf(false) }
    var wasPinAlertDialogShowed by remember { mutableStateOf(false) }

    if (showThemeSelectorDialog) {
      AlertDialog(
        onDismissRequest = { showThemeSelectorDialog = false },
        title = {
          Text(
            text = "Tema",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
          )
        },
        confirmButton = { },
        text = {
          RadioGroup(
            items = themeItems,
            selected = appTheme.value.ordinal,
            onItemSelect = {
              appTheme.value = AppTheme.fromOrdinal(it)
              selectedItem = themeTitle(it)
              showThemeSelectorDialog = false
            }
          )
        }
      )
    }

    if (showPinAlertDialog){
      ScrollableAlertDialog(
        text = "Tenga en cuenta que guardar el PIN de su tarjeta Telebanca puede ser muy peligroso pues este estará siempre expuesto en la aplicación",
        isInfoDialog = false
      ) {
        showPinAlertDialog = false
        wasPinAlertDialogShowed = true
      }
    }

    // Theme
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clickable {
          showThemeSelectorDialog = true
        }
    ) {
      Text(
        text = "Tema",
        style = MaterialTheme.typography.titleMedium
      )
      Text(
        text = selectedItem,
        style = MaterialTheme.typography.bodyMedium
      )
    }

    LabelSwitch(
      label = "Siempre API WA Message",
      checked = alwaysWaMessageByIntent,
    )

    LabelSwitch(
      label = "Guardar PIN",
      checked = savePin,
    ) {
      if (it && !wasPinAlertDialogShowed)
        showPinAlertDialog = true
    }
  }
}

@Preview(apiLevel = 33)
@Preview(apiLevel = 33, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewSettingsScreen() {
  PreviewComposable {
    SettingsScreen(
      appTheme = LocalTheme.current.isDark.let { remember { mutableStateOf(if (it) AppTheme.MODE_NIGHT else AppTheme.MODE_DAY) } },
      alwaysWaMessageByIntent = remember { mutableStateOf(true) },
      savePin = remember { mutableStateOf(false) },
      onNavigateBack = {},
    )
  }
}