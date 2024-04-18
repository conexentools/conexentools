package com.conexentools.presentation.components.screens.contact_picker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.conexentools.core.app.Constants
import com.conexentools.core.util.log
import com.conexentools.domain.repository.AndroidUtils
import com.conexentools.presentation.components.common.Contact
import com.conexentools.presentation.components.common.PrimaryIconButton
import com.conexentools.presentation.components.common.ScreenSurface
import com.conexentools.presentation.components.common.SearchAppBar
import com.conexentools.presentation.components.common.enums.ScreenSurfaceContentContainer
import contacts.core.Contacts
import contacts.core.entities.Contact
import kotlinx.coroutines.launch
import my.nanihadesuka.compose.LazyColumnScrollbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactPickerScreen(
  onNavigateBack: () -> Unit, onSelectionDone: (List<Contact>) -> Unit,
  multiContactSelectionOnly: Boolean = false,
  au: AndroidUtils
) {
  var areContactsSelectable by remember { mutableStateOf(multiContactSelectionOnly) }
  var isLoadingClients by remember { mutableStateOf(true) }

  val context = LocalContext.current
  val phoneContacts: SnapshotStateList<ContactPickerContact> = remember { mutableStateListOf() }

  val searchTerm = remember { mutableStateOf("") }
  val coroutineScope = rememberCoroutineScope()

  fun filterContacts(query: String?) {
    isLoadingClients = true
    coroutineScope.launch {
      if (query == null) {
        phoneContacts.addAll(
          Contacts(context).query().find()
            .sortedBy { contact ->
              contact.displayNamePrimary ?: contact.displayNameAlt ?: ""
            }.map { ContactPickerContact(it) }
        )
      } else {
        for (contact in phoneContacts) {
          with(searchTerm.value) {
            contact.visible.value = contact.name.contains(this, ignoreCase = true) ||
                contact.phone != null && contact.phone.contains(this, ignoreCase = true)
          }
        }
      }
    }.invokeOnCompletion {
      isLoadingClients = false
    }
  }

  LaunchedEffect(true) {
    phoneContacts.clear()
    filterContacts(null)
  }

  LaunchedEffect(searchTerm.value) {
    log("Querying all contacts")
    filterContacts(searchTerm.value)
  }

  var showDoneButton by remember { mutableStateOf(false) }
  var isSearchingContacts by remember { mutableStateOf(false) }

  val customTopAppBar: @Composable () -> Unit = {
    SearchAppBar(
      text = searchTerm,
      onNavigateBack = { isSearchingContacts = false },
    )
  }

  fun selectedContact(): List<ContactPickerContact> {
    return phoneContacts.filter { it.isSelected.value }
  }

  ScreenSurface(
    title = "Contactos",
    titleTextAlign = TextAlign.Left,
    onNavigateBack = onNavigateBack,
    customTopAppBar = if (isSearchingContacts) customTopAppBar else null,
    surfaceModifier = Modifier.widthIn(0.dp, 500.dp),
    defaultTopAppBarActions = {

      // Select|Deselect All Button
      AnimatedVisibility(visible = areContactsSelectable) {
        phoneContacts.let {
          val selectedContactsCount = selectedContact().count()
          key(selectedContactsCount) {

            showDoneButton = selectedContactsCount > 0

            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "$selectedContactsCount/${it.count()}",
                style = MaterialTheme.typography.titleLarge
              )

              val selectAll = selectedContactsCount in 0..<it.count()
              key(selectAll) {
                Box {
                  PrimaryIconButton(if (selectAll) Icons.Default.SelectAll else Icons.Default.Deselect) {
                    for (contact in it) {
                      contact.isSelected.value = selectAll
                    }
                  }
                }
              }
            }
          }
        }
      }

      // Search Button
      PrimaryIconButton(Icons.Default.Search) {
        isSearchingContacts = true
      }

      // Submit Changes Button
      AnimatedVisibility(visible = showDoneButton) {
        PrimaryIconButton(Icons.Default.Done) {
          onSelectionDone(selectedContact().map { it.contact })
        }
      }
    },
    contentContainer = ScreenSurfaceContentContainer.Surface
  ) {

    if (isLoadingClients) {
      CircularProgressIndicator()
    } else {
      val listState = rememberLazyListState()
      LazyColumnScrollbar(
        listState = listState,
      ) {
        LazyColumn(
          state = listState,
          verticalArrangement = Arrangement.spacedBy(Constants.Dimens.Small),
        ) {
          items(phoneContacts.filter { it.visible.value }) { contact ->
            key(contact.isSelected.value) {
              Contact(
                name = contact.name,
                imageUriString = contact.imageUriString,
                showDivider = false,
                subtitle = contact.phone,
                onSelected = {
                  if (!areContactsSelectable) {
                    areContactsSelectable = true
                    contact.isSelected.value = true
                  }
                },
                onSubtitleLongClick = {
                  if (it.isNotEmpty()) {
                    au.setClipboard(it)
                    au.toast("Número copiado al portapapeles")
                  }
                },
                onClick = {
                  if (areContactsSelectable) {
                    contact.isSelected.value = !contact.isSelected.value
                  }
                },
                extraContent = null,
                isSelected = contact.isSelected.value,
              )
            }
          }
        }
      }
    }
  }
}

