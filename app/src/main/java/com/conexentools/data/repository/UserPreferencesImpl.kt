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

  override suspend fun saveBank(value: String?) = save(PK.BANK, value)
  override suspend fun saveWaContact(value: String?) = save(PK.WA_CONTACT, value)
  override suspend fun saveWaContactImageUriString(value: String?) = save(PK.WA_CONTACT_IMAGE_URI_STRING, value)
  override suspend fun saveFetchDataFromWA(value: Boolean?) = save(PK.FETCH_DATA_FROM_WA, value)
  override suspend fun saveCardLast4Digits(value: String?) = save(PK.CARD_LAST_4_DIGITS, value)
  override suspend fun saveFirstClientRecharge(value: String?) = save(PK.FIRST_CLIENT_RECHARGE, value)
  override suspend fun saveSecondClientRecharge(value: String?) = save(PK.SECOND_CLIENT_RECHARGE, value)
  override suspend fun saveRechargeAvailabilityDateISOString(value: String?) = save(PK.RECHARGES_AVAILABILITY_DATE, value)
  override suspend fun saveIsManager(value: Boolean?) = save(PK.IS_MANAGER, value)
  override suspend fun saveInitialHomeScreenPage(value: Int?) = save(PK.INITIAL_HOME_SCREEN_PAGE, value)
  override suspend fun saveAppTheme(value: Int?) = save(PK.APP_THEME, value)
  override suspend fun saveAlwaysWaMessageByIntent(value: Boolean?) = save(PK.ALWAYS_WA_MESSAGE_BY_INTENT, value)
  override suspend fun saveAppLaunchCount(value: Int?) = save(PK.APP_LAUNCH_COUNT, value)
  override suspend fun saveClientListPageHelpDialogsShowed(value: Boolean?) = save(PK.CLIENT_LIST_PAGE_HELP_DIALOGS_SHOWED, value)
  override suspend fun saveSavePin(value: Boolean?) = save(PK.SAVE_PIN, value)
  override suspend fun savePin(value: String?) = save(PK.PIN, value)
  override suspend fun saveJoinMessages(value: Boolean?) = save(PK.JOIN_MESSAGES, value)

  override val bank: Flow<String?>
    get() = load(PK.BANK)
  override val waContact: Flow<String?>
    get() = load(PK.WA_CONTACT)
  override val waContactImageUriString: Flow<String?>
    get() = load(PK.WA_CONTACT_IMAGE_URI_STRING)
  override val fetchDataFromWA: Flow<Boolean?>
    get() = load(PK.FETCH_DATA_FROM_WA)
  override val cardLast4Digits: Flow<String?>
    get() = load(PK.CARD_LAST_4_DIGITS)
  override val firstClientRecharge: Flow<String?>
    get() = load(PK.FIRST_CLIENT_RECHARGE)
  override val secondClientRecharge: Flow<String?>
    get() = load(PK.SECOND_CLIENT_RECHARGE)
  override val rechargeAvailabilityDate: Flow<String?>
    get() = load(PK.RECHARGES_AVAILABILITY_DATE)
  override val isManager: Flow<Boolean?>
    get() = load(PK.IS_MANAGER)
  override val initialHomeScreenPage: Flow<Int?>
    get() = load(PK.INITIAL_HOME_SCREEN_PAGE)
  override val appTheme: Flow<Int?>
    get() = load(PK.APP_THEME)
  override val alwaysWaMessageByIntent: Flow<Boolean?>
    get() = load(PK.ALWAYS_WA_MESSAGE_BY_INTENT)
  override val appLaunchCount: Flow<Int?>
    get() = load(PK.APP_LAUNCH_COUNT)
  override val clientListPageHelpDialogsShowed: Flow<Boolean?>
    get() = load(PK.CLIENT_LIST_PAGE_HELP_DIALOGS_SHOWED)
  override val savePin: Flow<Boolean?>
    get() = load(PK.SAVE_PIN)
  override val pin: Flow<String?>
    get() = load(PK.PIN)
  override val joinMessages: Flow<Boolean?>
    get() = load(PK.JOIN_MESSAGES)
}
