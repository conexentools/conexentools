package com.conexentools

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import com.conexentools.Constants.LONG_TIMEOUT
import com.conexentools.Constants.MEDIUM_TIMEOUT
import com.conexentools.Utils.Companion.getPatternForResourceID

class DeviceManager(private val device: UiDevice) {

  fun findObject(resourceID: String? = null, text: String? = null): UiObject2? {
    val selector: BySelector = makeSelector(resourceID, text)
    return device.findObject(selector)
  }

  fun findObjects(resourceID: String? = null, text: String? = null): MutableList<UiObject2> {
    val selector: BySelector = makeSelector(resourceID, text)
    return device.findObjects(selector)
  }

  fun click(selector: BySelector, timeout: Int = MEDIUM_TIMEOUT) {
    val element: UiObject2? = waitForObject(selector, timeout)
    element!!.click()
  }

  fun click(resourceID: String? = null, text: String? = null, timeout: Int = MEDIUM_TIMEOUT) {
    val selector: BySelector = makeSelector(resourceID, text)
    click(selector, timeout)
  }

  fun waitForObject(selector: BySelector, timeout: Int = MEDIUM_TIMEOUT): UiObject2? {
    return device.wait(Until.findObject(selector), timeout.toLong())
  }

  fun selectDropDownMenuItem(
    resourceID: String? = null,
    text: String? = null,
    itemPosition: Int? = null,
    itemText: String? = null,
    timeout: Int = MEDIUM_TIMEOUT,
  ) {
    val selector: BySelector = makeSelector(resourceID, text)
    selectDropDownMenuItem(
      selector = selector,
      timeout = timeout,
      itemPosition = itemPosition,
      itemText = itemText
    )
  }

  fun selectDropDownMenuItem(
    selector: BySelector,
    itemPosition: Int? = null,
    itemText: String? = null,
    timeout: Int = MEDIUM_TIMEOUT,
  ) {

    if (itemPosition == null && itemText == null)
      throw Exception("You must specify either itemPosition or itemText")

    val textInput = waitForObject(selector, timeout)!!
    val regularDropDownMenuItemSelector = By.res("android:id/text1").clazz("android.widget.TextView")
    var count = 0
    var items: List<UiObject2>
    do {
      textInput.click()
      Thread.sleep(700)
      items = device.findObjects(regularDropDownMenuItemSelector)
    }
    while (items.isEmpty() && count++ < 3)

    if (count == 3)
      throw Exception("DropDownMenu couldn't be show")

    for (index in items.indices) {
      val item = items[index]
      val matchPosition = itemPosition == null || index == itemPosition - 1
      val matchText = itemText == null || item.text.contains(itemText)
      if (matchPosition && matchText) {
        item.click()
        return
      }
    }

    throw Exception("Item couldn't be found")
  }

  fun waitForObject(
    resourceID: String? = null,
    text: String? = null,
    timeout: Int = MEDIUM_TIMEOUT
  ): UiObject2? {
    val selector: BySelector = makeSelector(resourceID, text)
    return waitForObject(selector, timeout)
  }

  fun launchPackage(
    packageName: String,
    timeout: Int = LONG_TIMEOUT,
    clearOutPreviousInstances: Boolean = true
  ) {
    val context: Context = getApplicationContext()
    val intent = context.packageManager.getLaunchIntentForPackage(packageName)!!

    // Clear out any previous instances
    if (clearOutPreviousInstances)
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
    context.startActivity(intent)
    device.waitForIdle()
    device.wait(Until.hasObject(By.pkg(packageName).depth(0)), timeout.toLong())
  }

  private fun makeSelector(resourceID: String?, text: String?): BySelector {
    var selector: BySelector? = null
    if (resourceID != null) {
      selector = By.res(getPatternForResourceID(resourceID))
    }
    if (text != null) {
      selector = selector?.textContains(text) ?: By.textContains(text)
    }
    assert(selector != null)
    return selector!!
  }

  fun write(
    resourceID: String? = null,
    textToWrite: String,
    elementText: String? = null,
    timeout: Int = MEDIUM_TIMEOUT,
  ): UiObject2? {
    val selector = makeSelector(resourceID, elementText)
    return write(selector, textToWrite, timeout)
  }

  fun write(
    selector: BySelector,
    textToWrite: String,
    timeout: Int = MEDIUM_TIMEOUT,
  ): UiObject2? {
    val element = waitForObject(selector, timeout)
    if (element != null)
      element.text = textToWrite
    return element
  }
}
