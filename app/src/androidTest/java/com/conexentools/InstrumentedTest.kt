package com.conexentools

import android.Manifest
import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.conexentools.Utils.Companion.grantRuntimePermissions
import com.conexentools.Utils.Companion.setClipboard
import com.conexentools.Utils.Companion.toast
import com.conexentools.core.util.log
import com.conexentools.target_app_helpers.transfermovil.BankOperation
import com.conexentools.target_app_helpers.transfermovil.BankTab
import com.conexentools.target_app_helpers.transfermovil.TransfermovilHelper
import com.conexentools.target_app_helpers.whatsapp.WhatsAppHelper
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@Suppress("TestFunctionName")
@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 18) //@androidx.annotation.UiThread


class InstrumentedTest {

  private lateinit var device: UiDevice
  private lateinit var dm: DeviceManager
  private lateinit var th: TransfermovilHelper
  private lateinit var wh: WhatsAppHelper
  private lateinit var cliArguments: Bundle
  private val CASH_REPLACEMENT_RE = Regex("""(?<=Saldo Restante: ).*(?=\.)""")
  private val RECHARGE_RE = Regex("""(?<number>\d{8})\s*,\s*(?<recharge>\d{2,4})""")

  companion object {
    const val SECONDS_TO_WAIT_FOR_CONFIRMATION_MESSAGE = 120
  }

  @Before
  fun InitializeClass() {

    device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    dm = DeviceManager(device)
    th = TransfermovilHelper(device)
    wh = WhatsAppHelper(device)

    InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermissions(BuildConfig.APPLICATION_ID,
      Manifest.permission.READ_SMS,
      Manifest.permission.READ_CONTACTS,
    )

    if (!Utils.notificationManager.isNotificationPolicyAccessGranted) {
      toast("Acceso a interruptciones requerido", waitForToastToHide = true)
      Utils.openAccessToInterruptionsScreen()
      assert(false)
    }
    cliArguments = InstrumentationRegistry.getArguments()
  }

  @Test
  fun RechargeMobile() = runTest(
    startAtHome = true,
    includeWhatsAppVersionCompatibilityWarningToastOnErrors = true,
    includeTransfermovilVersionCompatibilityWarningToastOnErrors = true,
    turnOnDoNotDisturb = true
  ) {

    th.throwExceptionIfNotInstalled()

    toast("Iniciando el proceso de automatización", isShortToast = true)

    val rechargesRaw = cliArguments.getString("recharges")
    val recharges = rechargesRaw?.split("@")
      ?.map { Pair(it.split(',')[0], it.split(',')[1]) }?.toMutableList() ?: mutableListOf()
    val waContact = cliArguments.getString("waContact")
    val joinMessages = cliArguments.getString("joinMessages")?.toBoolean() ?: true
    // Parameter for testing purposes
    // If visible chats are for both senders, then get only owner chats or not
    val getOwnerChats = cliArguments.getString("getOwnerChats").toBoolean()
    var bank = cliArguments.getString("bank")!!.lowercase()
    if (bank == "metropolitano")
      bank = "metro"
    val pin = cliArguments.getString("pin")!!
    val cardToUseAlias =
      cliArguments.getString("cardToUseAlias")

    log("======== RechargeMobile ========")
    log("recharges: $rechargesRaw")
    log("waContact: $waContact")
    log("joinMessages: $joinMessages")
    log("getOwnerChats: $getOwnerChats")
    log("bank: $bank")
    log("cardToUseAlias: $cardToUseAlias")
    log("==============================")

    // Grab data from WA if transfers isEmpty
    if (recharges.isEmpty()) {

      wh.throwExceptionIfNotInstalled()

      if (waContact.isNullOrEmpty()) {
        toast(
          "Cuando ninguna recarga es especificada un nombre de contacto de WA tiene que ser especificado",
          vibrate = true,
          waitForToastToHide = true
        )
        assert(false)
      }
      toast("Obteniendo datos desde WhatsApp", isShortToast = true)
      wh.launch()
      wh.startConversation(waContact!!)
      val chats = wh.getLatestChatsInConversation(getOwnerChats = getOwnerChats)
      if (chats.isEmpty()) {
        toast(
          "Los últimos chats en la conversación no parecen ser los del contacto: $waContact, en cambio los tuyos",
          vibrate = true,
          waitForToastToHide = true
        )
        toast("También puede haber ocurrido algún error mientras se obtenían los chats")
        assert(false)
      }

      chats.forEach { chat ->
        RECHARGE_RE.findAll(chat).forEach { matchResult ->
          val recharge = Pair(matchResult.groups["number"]!!.value, matchResult.groups["recharge"]!!.value)
          recharges.add(recharge)
        }
      }

      if (recharges.isEmpty()) {
        toast(
          "Las recargas no pudieron ser obtenidas desde WhatsApp o no hay recargas en el los últimos chats",
          vibrate = true,
          waitForToastToHide = true
        )
        assert(false)
      }
      toast("Recargas obtenidas desde WhatsApp satisfactoriamente. Count: ${recharges.count()}", isShortToast = true)
    }

    recharges.forEach {
      if (it.second.toInt() !in 25..1250) {
        toast(
          "Recarga de $$it no es válida, cada recarga debe estar entre $25 y $1250",
          vibrate = true,
          waitForToastToHide = true
        )
        assert(false)
      }
    }

    th.performFullProcessTillAuthentication(bank, pin)

    val confirmationMessages = mutableListOf<String>()
    recharges.take(2).forEachIndexed { index, rechargePair ->

      // If user is not in Mobile Recharge screen, go to it
      if (dm.findObject(text = "Recarga Móvil") == null) {
        th.selectBankTab(BankTab.Operaciones)
        th.selectBankOperation(BankOperation.RechargeMobile)
      }

      val number = rechargePair.first
      val recharge = rechargePair.second

      dm.selectDropDownMenuItem(
        resourceID = "spTipoRecarga",
        itemText = "CUP", // Recarga Movil con tarjeta CUP
      )
      dm.findObject("txCuenta")!!.text = number
      dm.findObject("txMonto")!!.text = recharge

      if (bank == "bandec") {
        if (cardToUseAlias != null)
          th.selectAccountTypeToOperateForBandec(cardToUseAlias)
      } else {
        th.selectAccountTypeToOperateForMetroAndBPA(cardToUseAlias)
      }

      val latestMessage = th.getMessages().entries.last()

      th.accept(type1ToAccept = false)

      // Waiting for confirmation message
      var message = th.waitForConfirmationMessage(
        latestMessage = latestMessage,
        "La recarga se realizo con exito"
      )
      message = message.replace(CASH_REPLACEMENT_RE, "CR 0.00")

      confirmationMessages.add(message.trim())
      toast("Mensaje de confirmación capturado")

      // If recharging to same number, wait between recharges
      if (index == 0 && recharges.count() > 1 && recharges[1].first == number) {
        // Waiting 1 min and 5 seconds
        toast("Esperando 1 minuto y 5 segundos para hacer la próxima recarga... Stay tuned!")
        toast("No salga de la app ni toque nada, pórtese bien", isShortToast = true)
        Thread.sleep(1000 * 35)
        toast("30 segundos restantes")
        Thread.sleep(1000 * 30)
      }
    }

    // Copying message to clipboard
    val clipboardContent = confirmationMessages.joinToString("\n")
    setClipboard(clipboardContent)
    toast("Mensajes de confirmación copiados al portapapeles")

    // If data was obtained from WhatsApp, an instance of it should be open in the background on the chat screen
    if (!waContact.isNullOrEmpty()) {
      wh.launch(clearOutPreviousInstances = false)
      if (wh.isHomeScreen())
        wh.startConversation(waContact)
      if (joinMessages)
        wh.sendMessage(clipboardContent)
      else {
        confirmationMessages.forEach {
          wh.sendMessage(it)
          Thread.sleep(1000)
        }
      }
    }

    toast("Recargas satisfactoriamente realizadas... fino", waitForToastToHide = true)

    log("RechargeMobile Instrumented Test completed")
  }

  @Test
  fun SendWhatsAppMessage() = runTest(
    startAtHome = true,
    includeWhatsAppVersionCompatibilityWarningToastOnErrors = true,
    turnOnDoNotDisturb = true
  ) {
    wh.throwExceptionIfNotInstalled()
    val waContact = cliArguments.getString("waContact")!!
    val message = cliArguments.getString("message")

    wh.launchAndSendMessage(waContact, message)

    log("SendWhatsAppMessage Instrumented Test completed")
  }

  @Test
  fun TransferCash() = runTest(
    startAtHome = true,
    includeTransfermovilVersionCompatibilityWarningToastOnErrors = true,
    turnOnDoNotDisturb = true
  ) {

    var bank = cliArguments.getString("bank")!!.lowercase()
    if (bank == "metropolitano")
      bank = "metro"
    val pin = cliArguments.getString("pin")!!
    val cardToUseAlias =
      cliArguments.getString("cardToUseAlias")
    val recipientCard = cliArguments.getString("recipientCard")!!
    val cash = cliArguments.getString("cash")!!
    val mobileToConfirm = cliArguments.getString("mobileToConfirm")!!
    val recipientReceiveMyNumber = cliArguments.getString("recipientReceiveMyNumber").toBoolean()
    val waContact = cliArguments.getString("waContact")
    val message = cliArguments.getString("message")

    log("======== TransferCash ========")
    log("bank: $bank")
    log("cardToUseAlias: $cardToUseAlias")
    log("recipientCard: $recipientCard")
    log("cash: $cash")
    log("mobileToConfirm: $mobileToConfirm")
    log("recipientReceiveMyNumber: $recipientReceiveMyNumber")
    log("============================")

    th.throwExceptionIfNotInstalled()
    th.performFullProcessTillAuthentication(bank, pin)
    th.selectBankTab(BankTab.Operaciones)
    th.selectBankOperation(BankOperation.TransferCash)

    // Tarjeta o cuenta del destinatario
    dm.write("input_cuenta", recipientCard)!!
    // Monto
    dm.write("input_monto", cash)!!

    if (bank != "bandec") {
      // Moneda
      dm.selectDropDownMenuItem(
        resourceID = "spinnerTipoMonedaImporte",
        itemPosition = 1,
      )

      // Tipo de cuenta a operar
      th.selectAccountTypeToOperateForMetroAndBPA(cardToUseAlias)
    } else if (cardToUseAlias != null) {
      // Tipo de cuenta a operar
      th.selectAccountTypeToOperateForBandec(cardToUseAlias)
    }

    // Móvil a confirmar
    dm.write("input_phone_confirm", mobileToConfirm)!!
    // El destinatario recibe mi número de móvil
    if (recipientReceiveMyNumber)
      dm.click("checkBoxMyPhone")

    val latestMessage = th.getMessages().entries.last()
    // Confirm dialogs
    th.accept(type1ToAccept = true)

    // Wait for confirmation message
    th.waitForConfirmationMessage(
      latestMessage = latestMessage,
      "ransferencia fue completada", "le ha realizado una transferencia"
    )

    if (waContact != null){
      wh.launchAndSendMessage(waContact, message)
    }

    toast("Transferencia completada", waitForToastToHide = true)
    log("TransferCash Instrumented Test completed")
    // adb -s KRYX796HVGF67XWO shell am instrument -w -e class com.conexentools.InstrumentedTest#TransferCash -e bank metro -e pin 1064 -e cardToUseAlias CUP -e recipientReceiveMyNumber false -e recipientCard 9224069991767498 -e cash 5 -e mobileToConfirm 55797140 com.conexentools.test/androidx.test.runner.AndroidJUnitRunner --no-window-animation
  }

  @Test
  fun TestDropDownMenuSelector() = runTest (startAtHome = false) {
    val resourceID = cliArguments.getString("resourceId")!!
    val itemPosition = cliArguments.getString("itemPosition")?.toInt()
    val itemText = cliArguments.getString("itemText")

    log("======== TestDropDownMenuSelector ======")
    log("itemPosition: $itemPosition")
    log("itemText: $itemText")
    log("resourceID: $resourceID")
    log("======================================")

    dm.selectDropDownMenuItem(
      resourceID = resourceID,
      itemPosition = itemPosition,
      itemText = itemText
    )

    log("TestDropDownMenuSelector Instrumented Test completed")

    // adb shell am instrument -w -e class com.conexentools.InstrumentedTest#TestDropDownMenuSelector -e itemPosition 1 -e resourceId spTipoRecarga com.conexentools.test/androidx.test.runner.AndroidJUnitRunner
  }

  @Test
  fun Test() {

  }

  @Test
  fun PrintTransfermovilVersion() = th.printVersionInfo()

  @Test
  fun PrintWhatsAppVersionVersion() = wh.printVersionInfo()

  private fun runTest(
    startAtHome: Boolean,
    includeWhatsAppVersionCompatibilityWarningToastOnErrors: Boolean = false,
    includeTransfermovilVersionCompatibilityWarningToastOnErrors: Boolean = false,
    turnOnDoNotDisturb: Boolean = false,
    test: () -> Unit
  ) {

    fun printTargetAppsVersionInfoToasts() {
      if (includeTransfermovilVersionCompatibilityWarningToastOnErrors)
        th.checkVersionCompatibility()
      if (includeWhatsAppVersionCompatibilityWarningToastOnErrors)
        wh.checkVersionCompatibility()
    }

    try {
      if (turnOnDoNotDisturb)
        Utils.turnOnDoNotDisturbMode()
      if (startAtHome && !device.currentPackageName.endsWith("launcher"))
        device.pressHome()
      test()
    } catch (assertionExc: AssertionError) {
      printTargetAppsVersionInfoToasts()
      throw assertionExc
    } catch (ex: Exception) {
      toast(
        "Un error inesperado ocurrió",
        isShortToast = true,
        vibrate = true,
        waitForToastToHide = true
      )
      if (ex.localizedMessage != null)
        toast(ex.localizedMessage!!, waitForToastToHide = true)
      else {
        toast(ex.toString(), waitForToastToHide = true)
      }
      printTargetAppsVersionInfoToasts()
      throw ex
    } finally {
      if (turnOnDoNotDisturb)
        Utils.restoreInterruptionFilter()
    }
  }
}
