package com.conexentools.presentation.components.screens.contact_picker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.conexentools.presentation.components.common.Contact
import com.conexentools.presentation.components.common.PrimaryIconButton
import com.conexentools.presentation.components.common.ScreenSurface
import com.conexentools.presentation.components.common.cleanCubanMobileNumber
import com.conexentools.presentation.components.screens.home.enums.HomeScreenPage
import com.conexentools.presentation.components.screens.home.pages.client_list.SearchAppBar
import contacts.core.Contacts
import contacts.core.entities.Contact
import contacts.core.util.phoneList
import kotlinx.coroutines.launch
import my.nanihadesuka.compose.LazyColumnScrollbar

@Composable
fun ContactPickerScreen(
  onNavigateBack: () -> Unit,
  onSelectionDone: (List<Contact>) -> Unit
) {
  var areContactsSelectable by remember { mutableStateOf(false) }

  val context = LocalContext.current
//  val searchTerm by remember { mutableStateOf("") }
  val contacts: MutableState<List<Contact>?> = remember { mutableStateOf(null) }
  val coroutineScope = rememberCoroutineScope()

  fun filterContacts(query: String) {
    contacts.value = null
    coroutineScope.launch {
      contacts.value = Contacts(context)
        .broadQuery()
        .wherePartiallyMatches(query)
        .find().toList()
    }
  }

  LaunchedEffect(true) {
    contacts.value = Contacts(context).query().find().toList()
  }

  var isSearchingContacts by remember { mutableStateOf(false) }

  val customTopAppBar: @Composable () -> Unit = {
    SearchAppBar(
      onTextChange = {
        filterContacts(it)
      },
      onNavigateBack = { isSearchingContacts = false },
    )
  }

  ScreenSurface(
    title = "Contactos",
    titleTextAlign = TextAlign.Left,
    onNavigateBack = onNavigateBack,
    customTopAppBar = if (isSearchingContacts) customTopAppBar else null,
    defaultTopAppBarActions = {
      PrimaryIconButton(Icons.Default.Search){
        isSearchingContacts = true
      }
    }
  ) {

    if (contacts.value == null) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
      ) {
        CircularProgressIndicator()
//        Text(
//          text = "Cargando contactos...",
//          style = MaterialTheme.typography.bodyMedium
//        )
      }
    } else {

      val listState = rememberLazyListState()

      LazyColumnScrollbar(
        listState = listState,
        modifier = Modifier.animateContentSize()
      ) {
        LazyColumn {
          items(contacts.value!!) {
            var isSelected by remember { mutableStateOf(false) }
            val isContactSelected = remember {
              derivedStateOf {
                when (areContactsSelectable) {
                  false -> null
                  else -> isSelected
                }
              }
            }

            Contact(
              name = it.displayNamePrimary ?: "?",
              imageUriString = (it.photoThumbnailUri ?: it.photoUri)?.toString(),
              showDivider = false,
              subtitle = if (it.phoneList().isNotEmpty()) it.phoneList()
                .first().normalizedNumber?.cleanCubanMobileNumber() else null,
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