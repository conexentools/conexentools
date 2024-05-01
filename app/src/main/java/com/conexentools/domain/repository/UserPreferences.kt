package com.conexentools.domain.repository

import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow

interface UserPreferences {
  suspend fun <T> save(key: Preferences.Key<T>, data: T?)
  fun <T> load(key: Preferences.Key<T>): Flow<T?>

  suspend fun savePin(value: String?)
  suspend fun saveBank(value: String?)
  suspend fun saveAppTheme(value: Int?)
  suspend fun saveSavePin(value: Boolean?)
  suspend fun saveWaContact(value: String?)
  suspend fun saveAppLaunchCount(value: Int?)
  suspend fun saveJoinMessages(value: Boolean?)
  suspend fun saveCardToUseAlias(value: String?)
  suspend fun saveCashToTransfer(value: String?)
  suspend fun saveFetchDataFromWA(value: Boolean?)
  suspend fun saveInitialHomeScreenPage(value: Int?)
  suspend fun saveFirstClientRecharge(value: String?)
  suspend fun saveSecondClientRecharge(value: String?)
  suspend fun saveWaContactImageUriString(value: String?)
  suspend fun saveAlwaysWaMessageByIntent(value: Boolean?)
  suspend fun saveHomeScreenClientListScrollPosition(value: Int?)
  suspend fun saveClientListPageHelpDialogsShowed(value: Boolean?)
  suspend fun saveRechargeAvailabilityDateISOString(value: String?)
  suspend fun saveDefaultMobileToSendCashTransferConfirmation(value: String?)
  suspend fun saveSendWhatsAppMessageOnTransferCashTestCompleted(value: Boolean?)
  suspend fun saveWhatsAppMessageToSendOnTransferCashTestCompleted(value: String?)
  suspend fun saveRecipientReceiveMyMobileNumberAfterCashTransfer(value: Boolean?)

  val pin: Flow<String?>
  val bank: Flow<String?>
  val appTheme: Flow<Int?>
  val savePin: Flow<Boolean?>
  val waContact: Flow<String?>
  val appLaunchCount: Flow<Int?>
  val joinMessages: Flow<Boolean?>
  val cardToUseAlias: Flow<String?>
  val cashToTransfer: Flow<String?>
  val fetchDataFromWA: Flow<Boolean?>
  val initialHomeScreenPage: Flow<Int?>
  val firstClientRecharge: Flow<String?>
  val secondClientRecharge: Flow<String?>
  val waContactImageUriString: Flow<String?>
  val rechargeAvailabilityDate: Flow<String?>
  val alwaysWaMessageByIntent: Flow<Boolean?>
  val homeScreenClientListScrollPosition: Flow<Int?>
  val clientListPageHelpDialogsShowed: Flow<Boolean?>
  val defaultMobileToSendCashTransferConfirmation: Flow<String?>
  val sendWhatsAppMessageOnTransferCashTestCompleted: Flow<Boolean?>
  val whatsAppMessageToSendOnTransferCashTestCompleted: Flow<String?>
  val recipientReceiveMyMobileNumberAfterCashTransfer: Flow<Boolean?>
}
