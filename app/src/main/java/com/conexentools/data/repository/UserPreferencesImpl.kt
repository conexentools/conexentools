package com.conexentools.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.conexentools.domain.repository.UserPreferences
import com.conexentools.core.util.CoroutinesDispatchers
import com.conexentools.core.util.logError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import com.conexentools.core.app.Constants.PreferencesKey as PK

class UserPreferencesImpl @Inject constructor(
  private val dataStore: DataStore<Preferences>,
  private val coroutinesDispatchers: CoroutinesDispatchers,
) : UserPreferences {

  override suspend fun <T> save(key: Preferences.Key<T>, data: T?) {
    dataStore.edit { settings ->
      if (data != null)
        settings[key] = data
      else if (settings.contains(key))
        settings.remove(key)
    }
  }

  override fun <T> load(key: Preferences.Key<T>): Flow<T?> = dataStore.data
    .catch { exception ->
      exception.localizedMessage?.let { logError(it) }
      emit(emptyPreferences())
    }.map { preferences ->
      preferences[key]
    }.flowOn(context = coroutinesDispatchers.main)

  override suspend fun savePin(value: String?) = save(PK.PIN, value)
  override suspend fun saveBank(value: String?) = save(PK.BANK, value)
  override suspend fun saveAppTheme(value: Int?) = save(PK.APP_THEME, value)
  override suspend fun saveSavePin(value: Boolean?) = save(PK.SAVE_PIN, value)
  override suspend fun saveWaContact(value: String?) = save(PK.WA_CONTACT, value)
  override suspend fun saveIsManager(value: Boolean?) = save(PK.IS_MANAGER, value)
  override suspend fun saveJoinMessages(value: Boolean?) = save(PK.JOIN_MESSAGES, value)
  override suspend fun saveAppLaunchCount(value: Int?) = save(PK.APP_LAUNCH_COUNT, value)
  override suspend fun saveCashToTransfer(value: String?) = save(PK.CASH_TO_TRANSFER, value)
  override suspend fun saveCardToUseAlias(value: String?) = save(PK.CARD_TO_USE_ALIAS, value)
  override suspend fun saveFetchDataFromWA(value: Boolean?) = save(PK.FETCH_DATA_FROM_WA, value)
  override suspend fun saveFirstClientRecharge(value: String?) = save(PK.FIRST_CLIENT_RECHARGE, value)
  override suspend fun saveSecondClientRecharge(value: String?) = save(PK.SECOND_CLIENT_RECHARGE, value)
  override suspend fun saveInitialHomeScreenPage(value: Int?) = save(PK.INITIAL_HOME_SCREEN_PAGE, value)
  override suspend fun saveWaContactImageUriString(value: String?) = save(PK.WA_CONTACT_IMAGE_URI_STRING, value)
  override suspend fun saveAlwaysWaMessageByIntent(value: Boolean?) = save(PK.ALWAYS_WA_MESSAGE_BY_INTENT, value)
  override suspend fun saveRechargeAvailabilityDateISOString(value: String?) = save(PK.RECHARGES_AVAILABILITY_DATE, value)
  override suspend fun saveClientListPageHelpDialogsShowed(value: Boolean?) = save(PK.CLIENT_LIST_PAGE_HELP_DIALOGS_SHOWED, value)
  override suspend fun saveHomeScreenClientListScrollPosition(value: Int?) = save(PK.HOME_SCREEN_CLIENT_LIST_SCROLL_POSITION, value)
  override suspend fun saveDefaultMobileToSendCashTransferConfirmation(value: String?) = save(PK.DEFAULT_MOBILE_TO_SEND_CASH_TRANSFER_CONFIRMATION, value)
  override suspend fun saveSendWhatsAppMessageOnTransferCashTestCompleted(value: Boolean?) = save(PK.SEND_WHATS_APP_MESSAGE_ON_TRANSFER_CASH_TEST_COMPLETED, value)
  override suspend fun saveRecipientReceiveMyMobileNumberAfterCashTransfer(value: Boolean?) = save(PK.RECIPIENT_RECEIVE_MY_MOBILE_NUMBER_AFTER_CASH_TRANSFER, value)
  override suspend fun saveWhatsAppMessageToSendOnTransferCashTestCompleted(value: String?) = save(PK.WHATS_APP_MESSAGE_TO_SEND_ON_TRANSFER_CASH_TEST_COMPLETED, value)

  override val pin: Flow<String?> get() = load(PK.PIN)
  override val bank: Flow<String?> get() = load(PK.BANK)
  override val appTheme: Flow<Int?> get() = load(PK.APP_THEME)
  override val savePin: Flow<Boolean?> get() = load(PK.SAVE_PIN)
  override val waContact: Flow<String?> get() = load(PK.WA_CONTACT)
  override val isManager: Flow<Boolean?> get() = load(PK.IS_MANAGER)
  override val joinMessages: Flow<Boolean?> get() = load(PK.JOIN_MESSAGES)
  override val appLaunchCount: Flow<Int?> get() = load(PK.APP_LAUNCH_COUNT)
  override val cashToTransfer: Flow<String?> get() = load(PK.CASH_TO_TRANSFER)
  override val fetchDataFromWA: Flow<Boolean?> get() = load(PK.FETCH_DATA_FROM_WA)
  override val firstClientRecharge: Flow<String?> get() = load(PK.FIRST_CLIENT_RECHARGE)
  override val secondClientRecharge: Flow<String?> get() = load(PK.SECOND_CLIENT_RECHARGE)
  override val initialHomeScreenPage: Flow<Int?> get() = load(PK.INITIAL_HOME_SCREEN_PAGE)
  override val waContactImageUriString: Flow<String?> get() = load(PK.WA_CONTACT_IMAGE_URI_STRING)
  override val rechargeAvailabilityDate: Flow<String?> get() = load(PK.RECHARGES_AVAILABILITY_DATE)
  override val alwaysWaMessageByIntent: Flow<Boolean?> get() = load(PK.ALWAYS_WA_MESSAGE_BY_INTENT)
  override val cardToUseAlias: Flow<String?> get() = load(PK.CARD_TO_USE_ALIAS)
  override val clientListPageHelpDialogsShowed: Flow<Boolean?> get() = load(PK.CLIENT_LIST_PAGE_HELP_DIALOGS_SHOWED)
  override val homeScreenClientListScrollPosition: Flow<Int?> get() = load(PK.HOME_SCREEN_CLIENT_LIST_SCROLL_POSITION)
  override val defaultMobileToSendCashTransferConfirmation: Flow<String?> get() = load(PK.DEFAULT_MOBILE_TO_SEND_CASH_TRANSFER_CONFIRMATION)
  override val sendWhatsAppMessageOnTransferCashTestCompleted: Flow<Boolean?> get() = load(PK.SEND_WHATS_APP_MESSAGE_ON_TRANSFER_CASH_TEST_COMPLETED)
  override val recipientReceiveMyMobileNumberAfterCashTransfer: Flow<Boolean?> get() = load(PK.RECIPIENT_RECEIVE_MY_MOBILE_NUMBER_AFTER_CASH_TRANSFER)
  override val whatsAppMessageToSendOnTransferCashTestCompleted: Flow<String?> get() = load(PK.WHATS_APP_MESSAGE_TO_SEND_ON_TRANSFER_CASH_TEST_COMPLETED)
}
