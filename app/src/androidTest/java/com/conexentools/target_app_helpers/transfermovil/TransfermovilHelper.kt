@file:Suppress("MemberVisibilityCanBePrivate")

package com.conexentools.target_app_helpers.transfermovil

import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.conexentools.BuildConfig
import com.conexentools.Constants
import com.conexentools.Constants.LONG_TIMEOUT
import com.conexentools.Constants.SHORT_TIMEOUT
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

  fun accept(type1ToAccept: Boolean){
    dm.click("btn_aceptar")
    // Confirmación
    dm.click("button1", timeout = LONG_TIMEOUT)
    // Marque 1 para confimar, it may not appear, which doesn't matter
    if (type1ToAccept && dm.write("input_field", "1", timeout = LONG_TIMEOUT) != null){
      dm.click("button1", timeout = SHORT_TIMEOUT)
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
    isAuthenticatedInLatestSelectedBank = device.hasObject(By
      .res(getPatternForResourceID("design_menu_item_text"))
      .hasParent(By.res(getPatternForResourceID(bank)))
      .textContains("actual"))
    log("Is authenticated for bank $bank: $isAuthenticatedInLatestSelectedBank")
    dm.click(s)
  }

  fun selectBankOperation(operation: BankOperation) {
    val viewPager = dm.findObject("viewpager")!!
    val selector = By.textContains(operation.description)
    viewPager.scrollUntil(Direction.DOWN, Until.findObject(selector))
    dm.click(selector)
  }

  fun openLateralPanel(){
    dm.click(By.desc(name).hasAncestor(By.res(getPatternForResourceID("toolbar")), 1))
  }

  fun bypassStartUpDialogs() {
    log("Waiting for rate dialog")
    val isRateDialogPresent =
      dm.waitForObject(text = "ENVIAR") != null
    if (isRateDialogPresent){
      device.pressBack()
      log("Rate dialog dismissed")
    }
    else
      log("Rate dialog not present")

   val welcomeToTransfermovil = dm.waitForObject(null, "Bienvenido a Transfermóvil", timeout = Constants.SHORT_TIMEOUT)
    if (welcomeToTransfermovil != null) {
      dm.click("button1")
      log("Welcome dialog clicked")
    } else
      log("Welcome dialog not present")

    val welcomeToSystem = dm.waitForObject(text = "Bienvenido al sistema", timeout = Constants.SHORT_TIMEOUT)
    if (welcomeToSystem != null) {
      dm.click("button1")
      log("Welcome to system dialog clicked")
    } else
      log("Welcome to system dialog not present")
  }

  fun authenticate(pin: String): Boolean {
    selectBankTab(BankTab.Sesion)
    dm.click(null, "Autenticarse")
    val input = dm.findObject("input_clave")!!
    input.text = pin

    // Waiting for confirmation message
    val latestMessage: Map.Entry<Int, String> = getMessages().entries.last()

    accept(type1ToAccept = false)

    return waitForConfirmationMessage(latestMessage, "Usted se ha autenticado").isNotEmpty()
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

  fun selectAccountTypeToOperateForBandec(cardAlias: String) {
    dm.click("swCuentasBanco")
    dm.click(text = "Listar mis cuentas")
    selectCard(cardAlias)
  }

  fun selectAccountTypeToOperateForMetroAndBPA(
    cardAlias: String?,
  ) {
    if (cardAlias == null) {
      dm.selectDropDownMenuItem(
        resourceID = "spinnerTipoMoneda",
        itemText = "CUP",
      )
    } else {
      dm.selectDropDownMenuItem(
        resourceID = "spinnerTipoMoneda",
        itemText = "-MIS CUENTAS-",
      )

      if(device.findObject(By.textContains("No tiene cuentas registradas")) != null) {
        Utils.toast("No posee ninguna cuenta para este banco, por favor agregue una")
        assert(false)
      }

      selectCard(cardAlias)
    }
  }

  fun selectCard(cardAlias: String) {
    dm.selectDropDownMenuItem(
      resourceID = "spinnerCuentas",
      itemText = cardAlias,
    )
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