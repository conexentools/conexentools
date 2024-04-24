package com.conexentools.core.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object Constants {
  const val DEVELOPMENT_DATE = "2024"
  const val CONEXEN_GITHUB_REPO_URL = "https://github.com/conexentools/conexentools"
  const val CONEXEN_GOOGLE_MAIL = "conexentools@gmail.com"
  const val FAB_ANIMATION_DURATION = 400
  const val MAX_QUICK_MESSAGE_LENGTH = 1024
  const val APP_SETTINGS = "conexentools_settings"
  const val DATABASE_NAME = "clients"
  const val ADB_DOWNLOAD_PAGE_URL = "https://android-sdk-platform-tools.en.uptodown.com/windows"
  const val LOCAL_ADB_ARTICLE_URL = "https://www.makeuseof.com/run-adb-android-without-computer"
  const val ITEMS_PER_PAGE = 15

  object Dimens {
    val HorizontalCardHeight = 65.dp
    val HomeScreenFabContainer = 120.dp

    val MegaSmall = 2.dp
    val ExtraSmall = 3.dp
    val Small = 5.dp
    val Medium = 10.dp
    val Large = 15.dp
    val ExtraLarge = 20.dp
  }

  object Colors {
    val WARNING = Color(0xFFBE9117)
    val ERROR = Color(0xFF9E2927)
  }

  object PreferencesKey {
    val JOIN_MESSAGES = booleanPreferencesKey("join_messages")
    val PIN = stringPreferencesKey("pin")
    val SAVE_PIN = booleanPreferencesKey("save_pin")
    val APP_THEME = intPreferencesKey("app_theme")
    val BANK = stringPreferencesKey("bank")
    val WA_CONTACT = stringPreferencesKey("wa_contact")
    val FIRST_CLIENT_RECHARGE = stringPreferencesKey("first_client_cash")
    val SECOND_CLIENT_RECHARGE = stringPreferencesKey("second_client_cash")
    val FETCH_DATA_FROM_WA = booleanPreferencesKey("fetch_data_from_wa")
    val CLIENT_LIST_PAGE_HELP_DIALOGS_SHOWED = booleanPreferencesKey("client_list_help_dialog_showed")
    val CARD_LAST_4_DIGITS = stringPreferencesKey("card_last_4_digits")
    val RECHARGES_AVAILABILITY_DATE = stringPreferencesKey("recharges_availability_date")
    val IS_MANAGER = booleanPreferencesKey("is_manager")
    val INITIAL_HOME_SCREEN_PAGE = intPreferencesKey("initial_home_screen_page")
    val APP_LAUNCH_COUNT = intPreferencesKey("app_launch_count")
    val HOME_SCREEN_CLIENT_LIST_SCROLL_POSITION = intPreferencesKey("home_screen_client_list_scroll_position")
    val WA_CONTACT_IMAGE_URI_STRING = stringPreferencesKey("wa_contact_image_uri_string")
    val ALWAYS_WA_MESSAGE_BY_INTENT = booleanPreferencesKey("always_wa_message_by_intent")
  }
}
