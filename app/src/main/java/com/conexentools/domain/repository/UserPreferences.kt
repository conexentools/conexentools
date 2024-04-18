package com.conexentools.domain.repository

import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow

interface UserPreferences {
  suspend fun <T> save(key: Preferences.Key<T>, data: T?)
  fun <T> load(key: Preferences.Key<T>): Flow<T?>

  suspend fun saveBank(value: String?)
  suspend fun saveWaContact(value: String?)
  suspend fun saveWaContactImageUriString(value: String?)
  suspend fun saveFetchDataFromWA(value: Boolean?)
  suspend fun saveCardLast4Digits(value: String?)
  suspend fun saveFirstClientRecharge(value: String?)
  suspend fun saveSecondClientRecharge(value: String?)
  suspend fun saveRechargeAvailabilityDateISOString(value: String?)
  suspend fun saveIsManager(value: Boolean?)
  suspend fun saveInitialHomeScreenPage(value: Int?)
  suspend fun saveAppTheme(value: Int?)
  suspend fun saveAlwaysWaMessageByIntent(value: Boolean?)
  suspend fun saveAppLaunchCount(value: Int?)
  suspend fun saveClientListPageHelpDialogsShowed(value: Boolean?)

  val bank: Flow<String?>
  val waContact: Flow<String?>
  val waContactImageUriString: Flow<String?>
  val fetchDataFromWA: Flow<Boolean?>
  val cardLast4Digits: Flow<String?>
  val firstClientRecharge: Flow<String?>
  val secondClientRecharge: Flow<String?>
  val rechargeAvailabilityDate: Flow<String?>
  val isManager: Flow<Boolean?>
  val initialHomeScreenPage: Flow<Int?>
  val appTheme: Flow<Int?>
  val alwaysWaMessageByIntent: Flow<Boolean?>
  val appLaunchCount: Flow<Int?>
  val clientListPageHelpDialogsShowed: Flow<Boolean?>
}