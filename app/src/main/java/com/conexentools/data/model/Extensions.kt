package com.conexentools.data.model

import java.time.Instant

fun String?.toInstant(): Instant? {
  return if (this == null)
    null
  else
    Instant.parse(this)
}