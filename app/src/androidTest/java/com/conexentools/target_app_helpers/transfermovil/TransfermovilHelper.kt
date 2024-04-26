@file:Suppress("MemberVisibilityCanBePrivate")

package com.conexentools.target_app_helpers.transfermovil

import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.conexentools.BuildConfig
import com.conexentools.Constants.LONG_TIMEOUT
import com.conexentools.Constants.MEDIUM_TIMEOUT
import com.conexentools.InstrumentedTest.Companion.SECONDS_TO_WAIT_FOR_CONFIRMATION_MESSAGE
import com.conexentools.Utils
import com.conexentools.Utils.Companion.getPatternForResourceID
import com.conexentools.Utils.Companion.log
import com.conexentools.target_app_helpers.TargetAppHelper

class TransfermovilHelper(
  device: UiDevice
) : TargetAppHelper(
  device = device,
  name = "Transfermóvil",
  packageName = BuildConfig.TRANSFERMOVIL_PACKAGE_NAME,
  testedVersionCode = BuildConfig.TESTED_TM_VERSION_CODE,
  testedVersionName = BuildConfig.TESTED_TM_VERSION_NAME,
) {

  var isAuthenticatedInLatestSelectedBank = false

  fun accept(){
    dm.click("btn_aceptar")
//    device.pressEnter()
    // Confirmación
    dm.click("button1", timeout = LONG_TIMEOUT)
    // Marque 1 para confimar, it may not appear, which doesn't matter
    if (dm.write("input_field", "1", timeout = MEDIUM_TIMEOUT) != null){
      dm.click("button1", timeout = MEDIUM_TIMEOUT)
    }
    // Su solicitud está siendo procesada
    dm.click("button1", timeout = LONG_TIMEOUT * 3)
  }

  fun selectBankTab(tab: BankTab) {
    val t = dm.waitForObject(By.desc(tab.name))!!
    if (!t.isSelected)
      t.click()
  }

  fun selectBank(bank: String) {
    val designNavigationView = dm.waitForObject("design_navigation_view")!!
    val s = By.res(getPatternForResourceID(bank))
    designNavigationView.scrollUntil(Direction.DOWN, Until.findObject(s))
    isAuthenticatedInLatestSelectedBank = device.findObject(By.res(getPatternForResourceID("design_menu_item_text")).textContains("actual")) != null
    log("Is authenticated for bank $bank: $isAuthenticatedInLatestSelectedBank")
    dm.click(s)
  }

  fun selectBankOperation(operation: BankOperation) {
    val viewPager = dm.findObject("viewpager")
    val selector = By.text(operation.name)
    viewPager.scrollUntil(Direction.DOWN, Until.findObject(selector))
    dm.click(selector)
  }

  fun openLateralPanel(){
    dm.click(By.desc(name).hasAncestor(By.res(getPatternForResourceID("toolbar")), 1))
  }

  fun bypassStartUpDialogs() {
    log("Waiting for rate dialog")
    val isRateDialogPresent =
      dm.waitForObject(null, "ENVIAR", timeout = MEDIUM_TIMEOUT) != null
    if (isRateDialogPresent){
      device.pressBack()
      log("Rate dialog clicked")
    }
    else
      log("Rate dialog not present")

    val isWelcomeMessagePresent =
      dm.waitForObject(null, "Bienvenido a Transfermóvil") != null
    if (isWelcomeMessagePresent) {
      dm.click("button1", null, MEDIUM_TIMEOUT)
      log("Welcome message clicked")
    } else
      log("Welcome message not present")
  }

  fun authenticate(pin: String): Boolean {
    selectBankTab(BankTab.Sesion)
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

    return count < SECONDS_TO_WAIT_FOR_CONFIRMATION_MESSAGE && currentMessage.value.contains("Usted se ha autenticado")
  }

  fun getMessages(): Map<Int, String>{
    return Utils.getMessages("PAGOxMOVIL")
  }

  fun performFullProcessTillAuthentication(bank: String, pin: String) {
    Utils.toast("Abriendo Transfermóvil", isShortToast = true)

    launch()
    bypassStartUpDialogs()
    openLateralPanel()
    selectBank(bank)

    if (isAuthenticatedInLatestSelectedBank) {
      Utils.toast("Actualmente autenticado para el banco seleccionado", isShortToast = true)
    } else {
      // Authenticate
      if (!authenticate(pin)) {
        Utils.toast("Autenticación fallida", vibrate = true, waitForToastToHide = true)
        assert(false)
      }
      Utils.toast("Autenticado satisfactoriamente")
    }
  }

  fun selectAccountTypeToOperate(
    cardToUseDropDownMenuPosition: Int?,
    isFiveEntriesDropDownMenu: Boolean = false
  ) {
    if (cardToUseDropDownMenuPosition == null) {
      dm.selectDropDownMenuItem(
        resourceID = "spinnerTipoMoneda",
        choice = 1,
        choicesCount = 4,
        isItemBelowTextInput = true,
        expectedTextAfterSelection = "CUP"
      )
    } else {
      dm.selectDropDownMenuItem(
        resourceID = "spinnerTipoMoneda",
        choice = if (isFiveEntriesDropDownMenu) 4 else 3,
        choicesCount = if (isFiveEntriesDropDownMenu) 5 else 4,
        isItemBelowTextInput = true,
        expectedTextAfterSelection = "-MIS CUENTAS-"
      )

      //TODO() make it dynamic !!!!!!
      dm.selectDropDownMenuItem(
        resourceID = "spinnerCuentas",
        choice = cardToUseDropDownMenuPosition,
        choicesCount = 1, // No matter since as expectedTextAfterSelection is null the DropDownMenu items will only be searched below the text input
        isItemBelowTextInput = true,
        expectedTextAfterSelection = null
      )
      // This seems to crash the app
//      dm.write("spinnerCuentas", cardNickname)!!
    }
  }

  fun waitForConfirmationMessage(
    latestMessage: Map.Entry<Int, String>,
    vararg textToBePresent: String
  ): String {

    var count = 0
    var currentMessage: Map.Entry<Int, String>
    Utils.toast("Esperando por mensaje de confirmación", isShortToast = true)
    do {
      Thread.sleep(1000)
      currentMessage = getMessages().entries.last()
    } while (currentMessage == latestMessage && ++count < SECONDS_TO_WAIT_FOR_CONFIRMATION_MESSAGE)

    if (count >= SECONDS_TO_WAIT_FOR_CONFIRMATION_MESSAGE) {
      Utils.toast(
        "El mensaje de confirmación no fue recibido en $SECONDS_TO_WAIT_FOR_CONFIRMATION_MESSAGE segundos :(",
        vibrate = true,
        waitForToastToHide = true
      )
      assert(false)
    }

    var containsText = false
    for (pattern in textToBePresent){
      if (currentMessage.value.contains(pattern)){
        containsText = true
        break
      }
    }

    if (containsText)
      return currentMessage.value

    Utils.toast(
      "Algo parece haber salido mal, el mensaje de confirmación no fue el esperado :(",
      vibrate = true,
      waitForToastToHide = true
    )
    assert(false)
    return ""
  }
}