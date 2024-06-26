package com.conexentools.data.local.model

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.tooling.preview.Preview
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.conexentools.core.util.getRemainingTimeUntilDate
import com.conexentools.core.util.RemainingTimeTextRepresentation
import com.conexentools.domain.repository.AndroidUtils
import java.time.Instant
import java.time.temporal.ChronoUnit

@Entity(tableName = "client")
data class Client(
  @PrimaryKey(autoGenerate = true)
  var id: Long = 0,
  var name: String = "",
  var phoneNumber: String? = null,
  var cardNumber: String? = null,
  var latestRechargeDateISOString: String? = null,
  var imageUriString: String? = null,
  var quickMessage: String? = null,
  var rechargesMade: Int? = 0,
  var submittedForDeletionFlag: Int = 0
) {

//  @Ignore
//  var visible = mutableStateOf(true)

  fun getRemainingTimeForNextRechargeToBeAvailable(): RemainingTimeTextRepresentation? {
    return latestRechargeDateISOString?.run {
      val latestRechargeDate = Instant.parse(this)
      val nextRechargeAvailabilityDate = latestRechargeDate.plus(1, ChronoUnit.DAYS)
      getRemainingTimeUntilDate(nextRechargeAvailabilityDate)
    }
  }

  fun call(au: AndroidUtils) {
    if (phoneNumber.isNullOrEmpty()) {
      au.toast("Este cliente no tiene número asociado", vibrate = true)
    } else {
      au.call(phoneNumber!!)
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    other as Client
    return other.hashCode() == this.hashCode()
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    listOf(
      phoneNumber,
      cardNumber,
      latestRechargeDateISOString,
      imageUriString,
      quickMessage,
      rechargesMade,
    ).forEach {
      result = 31 * result + it.hashCode()
    }
    return result
  }
}
