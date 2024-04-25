package com.conexentools

import android.content.Context
import android.content.Intent
import android.graphics.Point
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

  fun findObject(resourceID: String?, text: String? = null): UiObject2 {
    val selector: BySelector = makeSelector(resourceID, text)
    return device.findObject(selector)
  }

  fun findObjects(resourceID: String?, text: String? = null): MutableList<UiObject2> {
    val selector: BySelector = makeSelector(resourceID, text)
    return device.findObjects(selector)
  }

  fun click(selector: BySelector, timeout: Int = MEDIUM_TIMEOUT) {
    val element: UiObject2? = waitForObject(selector, timeout)
    element!!.click()
  }

  fun click(resourceID: String?, text: String? = null, timeout: Int = MEDIUM_TIMEOUT) {
    val selector: BySelector = makeSelector(resourceID, text)
    click(selector, timeout)
  }

  fun waitForObject(selector: BySelector, timeout: Int = MEDIUM_TIMEOUT): UiObject2? {
    return device.wait(Until.findObject(selector), timeout.toLong())
  }

  fun selectDropDownMenuItem(
    resourceID: String?,
    text: String? = null,
    choice: Int,
    choicesCount: Int,
    timeout: Int = MEDIUM_TIMEOUT,
    isItemBelowTextInput: Boolean = true,
    expectedTextAfterSelection: String?,
  ) {
    val selector: BySelector = makeSelector(resourceID, text)
    selectDropDownMenuItem(
      selector = selector,
      timeout = timeout,
      choice = choice,
      choicesCount = choicesCount,
      isItemBelowTextInput = isItemBelowTextInput,
      expectedTextAfterSelection = expectedTextAfterSelection
    )
  }

  fun selectDropDownMenuItem(
    selector: BySelector,
    choice: Int,
    choicesCount: Int,
    timeout: Int = MEDIUM_TIMEOUT,
    isItemBelowTextInput: Boolean = true,
    expectedTextAfterSelection: String?,
  ) {
    assert(choicesCount > 0 && choice in 1..choicesCount)
    val textInput = waitForObject(selector, timeout)!!

    fun clickDropDownItem(isBelow: Boolean): Boolean {
      val offset = if (isBelow) {
        choice
      } else {
        (choicesCount - choice + 1) * -1
      }
      val distanceToChoiceCenter = textInput.visibleBounds.height() * offset
      val point =
        Point(textInput.visibleCenter.x, textInput.visibleCenter.y + distanceToChoiceCenter)
      textInput.click()
      Thread.sleep(700)
      device.click(point.x, point.y)
      return expectedTextAfterSelection == null || textInput.text == expectedTextAfterSelection
    }

    if (!clickDropDownItem(isBelow = isItemBelowTextInput))
      if (!clickDropDownItem(isBelow = !isItemBelowTextInput))
        throw Exception("Expected DropDownMenuItem '$expectedTextAfterSelection' couldn't be selected, instead we got '${textInput.text}'")
  }

  fun waitForObject(
    resourceID: String?,
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
      selector = selector?.text(text) ?: By.text(text)
    }
    assert(selector != null)
    return selector!!
  }

  fun write(
    resourceID: String?,
    text: String,
    elementText: String? = null,
    timeout: Int = MEDIUM_TIMEOUT,
  ): UiObject2? {
    val selector = makeSelector(resourceID, elementText)
    return write(selector, text, timeout)
  }

  fun write(
    selector: BySelector,
    text: String,
    timeout: Int = MEDIUM_TIMEOUT,
  ): UiObject2? {
    val element = waitForObject(selector, timeout)
    if (element != null)
      element.text = text
    return element
  }
}
