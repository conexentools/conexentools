package com.conexentools.presentation.components.screens.contact_picker

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.protobuf.ByteString
import com.conexentools.core.app.Constants
import com.conexentools.core.util.PreviewComposable
import com.conexentools.core.util.log
import com.conexentools.presentation.components.common.Contact
import com.conexentools.presentation.components.common.PrimaryIconButton
import com.conexentools.presentation.components.common.ScreenSurface
import com.conexentools.presentation.components.common.SearchAppBar
import com.conexentools.presentation.components.common.cleanCubanMobileNumber
import com.conexentools.presentation.components.common.enums.ScreenSurfaceContentWrapper
import contacts.core.Contacts
import contacts.core.entities.Contact
import contacts.core.entities.ContactEntity
import contacts.core.util.phoneList
import kotlinx.coroutines.launch
import my.nanihadesuka.compose.LazyColumnScrollbar

@Composable
fun ContactPickerScreen(
  onNavigateBack: () -> Unit, onSelectionDone: (List<Contact>) -> Unit,
  multiContactSelectionOnly: Boolean = false
) {
  var areContactsSelectable by remember { mutableStateOf(multiContactSelectionOnly) }
  var isLoadingClients by remember { mutableStateOf(true) }

  val context = LocalContext.current
//  val searchTerm by remember { mutableStateOf("") }
  val phoneContacts: SnapshotStateList<Contact> = remember { mutableStateListOf() }

  var searchTerm = remember { mutableStateOf("") }
  val coroutineScope = rememberCoroutineScope()



  fun filterContacts(query: String) {
    isLoadingClients = true
    phoneContacts.clear()
    coroutineScope.launch {
      phoneContacts.addAll(Contacts(context).broadQuery().wherePartiallyMatches(query).find().sortedBy { contact -> contact.displayNamePrimary ?: contact.displayNameAlt ?: "" })
      isLoadingClients = false
    }
  }



  LaunchedEffect(searchTerm.value) {
    log("Querying all contacts")
    filterContacts(searchTerm.value)
//    phoneContacts.addAll(Contacts(context).query().find().toList())
  }

  var isSearchingContacts by remember { mutableStateOf(false) }
  val selectedContacts = remember { mutableStateListOf<Contact>() }
  var showDoneButton by remember { mutableStateOf(false) }

//  var isSelectAllContactsButtonIcon by remember { mutableStateOf(false) }
//  var switchSelectionState by remember { mutableStateOf(false) }
  var selectAll by remember { mutableStateOf(false) }
  var deselectAll by remember { mutableStateOf(false) }

  val customTopAppBar: @Composable () -> Unit = {
    SearchAppBar(
      text = searchTerm,
      onNavigateBack = { isSearchingContacts = false },
    )
  }

  ScreenSurface(
    title = "Contactos",
    titleTextAlign = TextAlign.Left,
    onNavigateBack = onNavigateBack,
    customTopAppBar = if (isSearchingContacts) customTopAppBar else null,
    surfaceModifier = Modifier.widthIn(0.dp, 350.dp),
    defaultTopAppBarActions = {
      // Search Button
      PrimaryIconButton(Icons.Default.Search) {
        isSearchingContacts = true
      }

      // Select|Deselect All Button
      AnimatedVisibility(visible = areContactsSelectable) {
        phoneContacts.let {
          Box {
            if (deselectAll || selectedContacts.isNotEmpty() && selectedContacts.count() < it.count())
              PrimaryIconButton(Icons.Default.SelectAll) {
                selectAll = true
                if (deselectAll)
                  deselectAll = false
              }

            if (selectAll || selectedContacts.count() == it.count())
              PrimaryIconButton(Icons.Default.Deselect) {
                deselectAll = true
                if (selectAll)
                  selectAll = false
              }
          }
        }
      }

      // Submit Changes Button
      AnimatedVisibility(visible = showDoneButton) {
        PrimaryIconButton(Icons.Default.Done) {
          onSelectionDone(selectedContacts.toList())
        }
      }
    },
    screenSurfaceContentWrapper = ScreenSurfaceContentWrapper.Surface
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
          items(phoneContacts) { contact ->
            var isSelected by remember { mutableStateOf(false) }

            val isContactSelected = remember {
              derivedStateOf {
                when (areContactsSelectable) {
                  false -> null
                  else -> isSelected
                }
              }
            }

            if (isSelected && !selectedContacts.contains(contact))
              selectedContacts.add(contact)
            else if (!isSelected && selectedContacts.contains(contact))
              selectedContacts.remove(contact)

//            val selectedContactsCount = selectedContacts.count()
//            if (selectedContactsCount == phoneContacts.value!!.count() && isSelectAllContactsButtonIcon)
//              isSelectAllContactsButtonIcon = false
//            else if (selectedContactsCount > 0 && !isSelectAllContactsButtonIcon)
//              isSelectAllContactsButtonIcon = true

            showDoneButton = selectedContacts.isNotEmpty()

            if (selectAll && !isSelected)
              isSelected = true
            else if (deselectAll && isSelected)
              isSelected = false

//            if (switchSelectionState) {
//              isSelected = !isSelected
//              if ((isSelected && selectedContactsCount == phoneContacts.value!!.count()) ||
//                (!isSelected && selectedContactsCount == 0))
//                switchSelectionState = false
//            }

//            key(isSelected, switchSelectionState) {
//
//              val selectedContactsCount = selectedContacts.count()
//              if (selectedContactsCount == phoneContacts.value!!.count())
//                isSelectAllContactsButtonIcon = false
//              else if (selectedContactsCount > 0)
//                isSelectAllContactsButtonIcon = true
//
//
////              else if (areContactsSelectable && !isSelected)
////                isSelected = true
//              if (isSelected && !selectedContacts.contains(contact)) selectedContacts.add(contact)
//              else if (!isSelected && selectedContacts.contains(contact)) selectedContacts.remove(
//                contact
//              )
//              showDoneButton = selectedContacts.isNotEmpty()
//
//              if (switchSelectionState) {
//                isSelected = !isSelected
//                if ((isSelected && selectedContactsCount == phoneContacts.value!!.count()) ||
//                  (!isSelected && selectedContactsCount == 0)
//                )
//                  switchSelectionState = false
//              }
////              if (!showDoneButton && !isSelectAllContactsButtonIcon){
////                isSelectAllContactsButtonIcon = true
////              } else
//            }

            Contact(
              name = contact.displayNamePrimary ?: "?",
              imageUriString = (contact.photoThumbnailUri ?: contact.photoUri)?.toString(),
              showDivider = false,
              subtitle = contact.phoneList()
                .firstOrNull()?.normalizedNumber?.cleanCubanMobileNumber(),
              onSelected = {
                if (!areContactsSelectable) {
                  isSelected = true
                  areContactsSelectable = true
                }
              },
              onSubtitleLongClick = null,
              onClick = {
                if (areContactsSelectable) {
                  isSelected = !isSelected
                  if (!isSelected && selectAll)
                    selectAll = false
                  if (isSelected && deselectAll)
                    deselectAll = false
                }
              },
              extraContent = null,
              isSelected = isContactSelected.value,
            )
          }
        }
      }
    }
  }
}

