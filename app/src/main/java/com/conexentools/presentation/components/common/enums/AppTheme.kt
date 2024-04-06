package com.conexentools.presentation.components.common.enums

enum class AppTheme {
  MODE_DAY,
  MODE_NIGHT,
  MODE_AUTO;

  companion object {
    fun fromOrdinal(ordinal: Int?): AppTheme {
      return entries[ordinal?.coerceIn(0..entries.count()) ?: 1]
    }
  }
}