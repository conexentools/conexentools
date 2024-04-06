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
import java.util.regex.Pattern

class DeviceManager(private val device: UiDevice) {

  fun findObject(resourceID: String?, text: String? = null): UiObject2 {
    val selector: BySelector = getSelector(resourceID, text)
    return device.findObject(selector)
  }

  fun findObjects(resourceID: String?, text: String? = null): MutableList<UiObject2> {
    val selector: BySelector = getSelector(resourceID, text)
    return device.findObjects(selector)
  }

  fun click(selector: BySelector, timeout: Int = MEDIUM_TIMEOUT) {
    val element: UiObject2? = waitForObject(selector, timeout)
    element!!.click()
  }

  fun click(resourceID: String?, text: String? = null, timeout: Int = MEDIUM_TIMEOUT) {
    val selector: BySelector = getSelector(resourceID, text)
    click(selector, timeout)
  }

  fun waitForObject(selector: BySelector, timeout: Int = MEDIUM_TIMEOUT): UiObject2? {
    return device.wait(Until.findObject(selector), timeout.toLong())
  }

  fun selectDropDownMenuChoice(resourceID: String?, text: String? = null, timeout: Int = MEDIUM_TIMEOUT, choice: Int, isMenuBelow: Boolean = true){
    val selector: BySelector = getSelector(resourceID, text)
    selectDropDownMenuChoice(
      selector = selector,
      timeout = timeout,
      choice =  choice,
      isMenuBelow = isMenuBelow)
  }

  fun selectDropDownMenuChoice(selector: BySelector, choice: Int, timeout: Int = MEDIUM_TIMEOUT, isMenuBelow: Boolean = true){
    assert(choice >= 1)
    val element = waitForObject(selector, timeout)!!
    element.click()
//    click(selector, timeout)
    var heightDistanceToChoiceCenter = element.visibleBounds.height() * choice
    if (!isMenuBelow)
      heightDistanceToChoiceCenter *= -1
    val point = Point(element.visibleCenter.x, element.visibleCenter.y + heightDistanceToChoiceCenter)
    Thread.sleep(700)
    device.click(point.x, point.y)
  }


  fun waitForObject(resourceID: String?, text: String? = null, timeout: Int = MEDIUM_TIMEOUT): UiObject2? {
    val selector: BySelector = getSelector(resourceID, text)
    return waitForObject(selector, timeout)
  }

  fun launchPackage(packageName: String, timeout: Int = LONG_TIMEOUT, clearOutPreviousInstances: Boolean = true) {
    val context: Context = getApplicationContext()
    val intent = context.packageManager.getLaunchIntentForPackage(packageName)!!

    // Clear out any previous instances
    if (clearOutPreviousInstances)
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
    context.startActivity(intent)
    device.wait(Until.hasObject(By.pkg(packageName).depth(0)), timeout.toLong())
  }

  private fun getSelector(resourceID: String?, text: String?): BySelector {
    var selector: BySelector? = null
    if (resourceID != null) {
      selector = By.res(getPatternForResourceID(resourceID))
    }
    if (text != null) {
      selector = if (selector == null) { By.text(text)
      } else {
        selector.text(text)
      }
    }
    assert(selector != null)
    return selector!!
  }

  companion object {
    fun getPatternForResourceID(resourceName: String): Pattern {
      return Pattern.compile(".*:id/$resourceName")
    }

    const val SHORT_TIMEOUT = 2000
    const val MEDIUM_TIMEOUT = 5000
    const val LONG_TIMEOUT = 10000

  }
}
