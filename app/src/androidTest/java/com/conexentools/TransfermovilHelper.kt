package com.conexentools

import android.widget.ImageButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.conexentools.DeviceManager.Companion.LONG_TIMEOUT
import com.conexentools.DeviceManager.Companion.getPatternForResourceID
import com.conexentools.InstrumentedTest.Companion.SECONDS_TO_WAIT_FOR_CONFIRMATION_MESSAGE
import com.conexentools.Utils.Companion.log

class TransfermovilHelper(device: UiDevice) {

  val installedVersion = Utils.getPackageVersion(BuildConfig.TRANSFERMOVIL_PACKAGE_NAME)
  val testedVersion = Pair(BuildConfig.TESTED_TM_VERSION_CODE.toLong(), BuildConfig.TESTED_TM_VERSION_NAME)

  val dm: DeviceManager = DeviceManager(device)

  fun accept(){
    dm.click("btn_aceptar")
//    device.pressEnter()
    dm.click(By.res("android:id/button1"), timeout = LONG_TIMEOUT)
    Thread.sleep(2000)
    dm.click(By.res("android:id/button1"), timeout = LONG_TIMEOUT * 3)
  }

  fun selectBankTab(tabName: String){
    val tab = dm.waitForObject(By.desc(tabName))!!
    if (!tab.isSelected)
      tab.click()
  }

  fun selectBank(bank: String){
    val designNavigationView = dm.waitForObject("design_navigation_view")!!
    val s = By.res(getPatternForResourceID(bank)).clazz(LinearLayoutCompat::class.java)
    designNavigationView.scrollUntil(Direction.DOWN, Until.findObject(s))
    dm.click(s)
  }

  fun selectBankOperation(operationName: String){
    val viewPager = dm.findObject("viewpager")
    val selector = By.text(operationName)
    viewPager.scrollUntil(Direction.DOWN, Until.findObject(selector))
    dm.click(selector)
  }

  fun openLateralPanel(){
    dm.click(By.clazz(ImageButton::class.java).descContains("Transfermóvil"), LONG_TIMEOUT)
  }

  fun clickWelcomeMessageIfPresent() {
    val isWelcomeMessagePresent =
      dm.waitForObject(null, "Bienvenido a Transfermóvil") != null
    if (isWelcomeMessagePresent) {
      dm.click("button1", null, DeviceManager.MEDIUM_TIMEOUT)
      log("Welcome message clicked")
    } else {
      log("Welcome message not present", true)
    }
  }

  fun launch(clearOutPreviousInstances: Boolean = true) = dm.launchPackage(BuildConfig.TRANSFERMOVIL_PACKAGE_NAME, clearOutPreviousInstances = clearOutPreviousInstances)

  fun authenticate(pin: String): Boolean {
    selectBankTab("Sesión")
    dm.click(null, "Autenticarse")
    val input = dm.findObject("input_clave")
    input.text = pin
    accept()

    // Waiting for confirmation message
    var count = 0
    val lastMessage: Map.Entry<Int, String> = getMessages().entries.last()
    var currentMessage: Map.Entry<Int, String>
    Utils.toast("Esperando por mensaje de confirmación")
    do {
      Thread.sleep(1000)
      currentMessage = getMessages().entries.last()
    } while (currentMessage == lastMessage && ++count < SECONDS_TO_WAIT_FOR_CONFIRMATION_MESSAGE)

    return count < SECONDS_TO_WAIT_FOR_CONFIRMATION_MESSAGE && currentMessage.value.contains("Usted se ha autenticado en la plataforma de pagos moviles")
  }

  fun getMessages(): Map<Int, String>{
    return Utils.getMessages("PAGOxMOVIL")
  }
}