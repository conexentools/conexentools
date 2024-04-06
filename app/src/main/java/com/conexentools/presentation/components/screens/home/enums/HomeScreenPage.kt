package com.conexentools.presentation.components.screens.home.enums

enum class HomeScreenPage {
  INSTRUMENTED_TEST,
  CLIENT_LIST;

  fun isInstrumentedTestPage() = this.ordinal == INSTRUMENTED_TEST.ordinal

  companion object {
    fun fromOrdinal(ordinal: Int?): HomeScreenPage {
      return entries[ordinal?.coerceIn(0..entries.count()) ?: 0]
    }
  }
}