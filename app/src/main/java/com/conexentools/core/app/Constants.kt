package com.conexentools.core.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object Constants {
  const val ITEMS_PER_PAGE = 15
  const val DEVELOPMENT_DATE = "2024"
  const val DATABASE_NAME = "clients"
  const val FAB_ANIMATION_DURATION = 400
  const val MAX_QUICK_MESSAGE_LENGTH = 1024
  const val APP_SETTINGS = "conexentools_settings"
  const val CONEXEN_GOOGLE_MAIL = "conexentools@gmail.com"
  const val CONEXEN_GITHUB_REPO_URL = "https://github.com/conexentools/conexentools"
  const val ADB_DOWNLOAD_PAGE_URL = "https://android-sdk-platform-tools.en.uptodown.com/windows"
  const val LOCAL_ADB_ARTICLE_URL = "https://www.makeuseof.com/run-adb-android-without-computer"

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

  object Messages {
    val CLIENT_QUICK_MESSAGE = "💸💸💳📲💸💸"
    val CLIENT_MESSAGE_TO_SEND_ON_TRANSFER_CASH_TEST_COMPLETED = "💰➡️💳✅"
  }

  object PreferencesKey {
    val PIN = stringPreferencesKey("pin")
    val BANK = stringPreferencesKey("bank")
    val APP_THEME = intPreferencesKey("app_theme")
    val SAVE_PIN = booleanPreferencesKey("save_pin")
    val WA_CONTACT = stringPreferencesKey("wa_contact")
    val IS_MANAGER = booleanPreferencesKey("is_manager")
    val JOIN_MESSAGES = booleanPreferencesKey("join_messages")
    val APP_LAUNCH_COUNT = intPreferencesKey("app_launch_count")
    val CASH_TO_TRANSFER = stringPreferencesKey("cash_to_transfer")
    val FETCH_DATA_FROM_WA = booleanPreferencesKey("fetch_data_from_wa")
    val FIRST_CLIENT_RECHARGE = stringPreferencesKey("first_client_cash")
    val SECOND_CLIENT_RECHARGE = stringPreferencesKey("second_client_cash")
    val INITIAL_HOME_SCREEN_PAGE = intPreferencesKey("initial_home_screen_page")
    val CARD_TO_USE_DROP_DOWN_MENU_POSITION = stringPreferencesKey("card_last_4_digits")
    val RECHARGES_AVAILABILITY_DATE = stringPreferencesKey("recharges_availability_date")
    val WA_CONTACT_IMAGE_URI_STRING = stringPreferencesKey("wa_contact_image_uri_string")
    val ALWAYS_WA_MESSAGE_BY_INTENT = booleanPreferencesKey("always_wa_message_by_intent")
    val CLIENT_LIST_PAGE_HELP_DIALOGS_SHOWED = booleanPreferencesKey("client_list_help_dialog_showed")
    val DEFAULT_MOBILE_TO_SEND_CASH_TRANSFER_CONFIRMATION = stringPreferencesKey("default_mobile_to_confirm")
    val HOME_SCREEN_CLIENT_LIST_SCROLL_POSITION = intPreferencesKey("home_screen_client_list_scroll_position")
    val RECIPIENT_RECEIVE_MY_MOBILE_NUMBER_AFTER_CASH_TRANSFER = booleanPreferencesKey("recipient_receive_my_mobile_number_after_cash_transfer")
    val SEND_WHATS_APP_MESSAGE_ON_TRANSFER_CASH_TEST_COMPLETED = booleanPreferencesKey("send_whats_app_message_on_transfer_cash_test_completed")
    val WHATS_APP_MESSAGE_TO_SEND_ON_TRANSFER_CASH_TEST_COMPLETED = stringPreferencesKey("whats_app_message_to_send_on_transfer_cash_test_completed")
  }
}
