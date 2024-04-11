package com.conexentools.presentation.navigation

sealed class Screen(val route: String) {
  data object Home : Screen(route = "home_screen")
  data object AddEditClient : Screen(route = "add_edit_client_screen")
  data object Settings : Screen(route = "settings_screen")
  data object ContactPicker : Screen(route = "contact_picker_screen")
  data object About : Screen(route = "about_screen")
  data object Help : Screen(route = "help")
}
