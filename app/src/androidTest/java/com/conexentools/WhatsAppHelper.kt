package com.conexentools

import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
//import me.ibrahimsn.lib.PhoneNumberKit
//import me.ibrahimsn.lib.api.Phone


class WhatsAppHelper(val device: UiDevice) {

  companion object {
    const val PACKAGE_NAME = "com.whatsapp"
  }

  val installedVersion = Utils.getPackageVersion(PACKAGE_NAME)

  val dm: DeviceManager = DeviceManager(device)

  fun launch(clearOutPreviousInstances: Boolean = true) =
    dm.launchPackage(PACKAGE_NAME, clearOutPreviousInstances = clearOutPreviousInstances)

  fun startConversation(contactNameOrNumber: String) {
      // Assuming WA is already open at main screen
      dm.click("fab")
      dm.click("menuitem_search")
      dm.findObject("search_src_text").text = contactNameOrNumber.filter { it.code < 256 } // Filtering put unicode characters
      dm.click("contactpicker_text_container")
  }

  fun sendMessage(message: String) {
    // Assuming user is in WA chat window
    dm.waitForObject("entry")!!.text = message
    dm.click("send")
  }

//  fun getChats(){
//    val chats = dm.findObjects("message_text").map { it.text }.reversed()
//    return chats
//  }

  fun getLatestChatsInConversation(getYourChats: Boolean): List<String> {
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
      if (!wasOwnerOfLatestChatsIdentified && (isToLeft && !getYourChats || isToRight && getYourChats)) {
        chats.clear()
        wasOwnerOfLatestChatsIdentified = true
      } else if (isToLeft || isToRight)
        break

      chats.add(0, chat)
    }
    return chats.mapNotNull { it.findObject(By.res(DeviceManager.getPatternForResourceID("message_text")))?.text }
  }
}
