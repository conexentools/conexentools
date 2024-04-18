package com.conexentools.presentation.components.screens.contact_picker

import androidx.compose.runtime.mutableStateOf
import com.conexentools.presentation.components.common.cleanCubanMobileNumber
import contacts.core.entities.Contact
import contacts.core.util.phoneList

data class ContactPickerContact(val contact: Contact) {
  val name = contact.displayNamePrimary ?: contact.displayNameAlt ?: ""
  val phone = contact.phoneList().firstOrNull()?.normalizedNumber?.cleanCubanMobileNumber()
  var isSelected = mutableStateOf(false)
  var visible = mutableStateOf(false)
  var imageUriString = (contact.photoThumbnailUri ?: contact.photoUri)?.toString()
}