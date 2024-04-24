package com.conexentools.target_app_helpers

import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import com.conexentools.BuildConfig
import com.conexentools.DeviceManager
import java.util.regex.Pattern

class WhatsAppHelper(
  device: UiDevice
) : TargetAppHelper(
  device = device,
  name = "WhatsApp",
  packageName = BuildConfig.WHATSAPP_PACKAGE_NAME,
  testedVersionCode = BuildConfig.TESTED_WA_VERSION_CODE,
  testedVersionName = BuildConfig.TESTED_WA_VERSION_NAME,
) {

  fun startConversation(contactNameOrNumber: String) {
    // Assuming WA is already open at main screen
    dm.click(By.res(Pattern.compile(".*:id/.*?fab")))

    dm.click("menuitem_search")
    dm.findObject("search_src_text").text =
      contactNameOrNumber.filter { it.code <= 0xFF } // Taking only ASCII characters
    device.waitForIdle()
    Thread.sleep(100)
    dm.click("contactpicker_text_container")
  }

  fun sendMessage(message: String) {
    // Assuming user is in WA chat window
    dm.waitForObject("entry")!!.text = message
    dm.click("send")
  }

  fun getLatestChatsInConversation(getOwnerChats: Boolean): List<String> {
    val visibleChats = dm.findObjects("main_layout").reversed()
    var latestChatRect = visibleChats.first().visibleBounds
    val chats = mutableListOf(visibleChats.first())
    var wasOwnerOfLatestChatsIdentified = false
    for (chat in visibleChats.drop(1)) {
      val isToLeft =
        chat.visibleBounds.left < latestChatRect.left && chat.visibleBounds.right < latestChatRect.right
      val isToRight =
        chat.visibleBounds.left > latestChatRect.left && chat.visibleBounds.right > latestChatRect.right
      latestChatRect = chat.visibleBounds
      if (!wasOwnerOfLatestChatsIdentified && (isToLeft && !getOwnerChats || isToRight && getOwnerChats)) {
        chats.clear()
        wasOwnerOfLatestChatsIdentified = true
      } else if (isToLeft || isToRight)
        break

      chats.add(0, chat)
    }
    return chats.mapNotNull { it.findObject(By.res(DeviceManager.getPatternForResourceID("message_text")))?.text }
  }
}
