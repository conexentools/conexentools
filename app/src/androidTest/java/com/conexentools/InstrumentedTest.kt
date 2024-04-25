package com.conexentools

import android.Manifest
import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
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
  private val RECHARGE_RE = Regex("""(?<n>\d{8})\s*,\s*(?<r>\d{2,4})""")

  companion object {
    const val SECONDS_TO_WAIT_FOR_CONFIRMATION_MESSAGE = 120
  }

  @Before
  fun InitializeClass() {

    device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    dm = DeviceManager(device)
    th = TransfermovilHelper(device)
    wh = WhatsAppHelper(device)
    InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
      BuildConfig.APPLICATION_ID,
      Manifest.permission.READ_SMS
    )
    InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
      BuildConfig.APPLICATION_ID,
      Manifest.permission.READ_CONTACTS
    )

    cliArguments = InstrumentationRegistry.getArguments()
  }

  @Test
  fun RechargeMobile() = runTest(
    includeWhatsAppVersionCompatibilityWarningToastOnErrors = true,
    includeTransfermovilVersionCompatibilityWarningToastOnErrors = true
  ) {
    th.throwExceptionIfNotInstalled()

    toast("Iniciando el proceso de automatización", isShortToast = true)

    var recharges = cliArguments.getString("recharges")?.split("@")
      ?.map { Pair(it.split(',')[0], it.split(',')[1]) }?.toList()
    val waContact = cliArguments.getString("waContact")
    val joinMessages = cliArguments.getString("joinMessages")?.toBoolean() ?: true
    // Parameter for testing purposes
    // If visible chats are for both senders, then get only owner chats or not
    val getOwnerChats = cliArguments.getString("getOwnerChats").toBoolean()
    var bank = cliArguments.getString("bank")!!.lowercase()
    if (bank == "metropolitano")
      bank = "metro"
    val pin = cliArguments.getString("pin")!!
    val cardToUseDropDownMenuPosition =
      cliArguments.getString("cardToUseDropDownMenuPosition")?.toInt()

    var dataFetchedFromWA = false

    // Grab data from WA if transfers isNullOrEmpty
    if (recharges.isNullOrEmpty()) {

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
      log("Chats:")
      chats.forEach { log(it) }
      if (chats.isEmpty()) {
        toast(
          "Los últimos chats en la conversación no parecen ser los del contacto: $waContact, en cambio los tuyos",
          vibrate = true,
          waitForToastToHide = true
        )
        toast("También puede haber ocurrido algún error mientras se obtenían los chats")
        assert(false)
      }

      if (recharges == null)
        recharges = mutableListOf()
      chats.forEach { chat ->
        val matches = RECHARGE_RE.findAll(chat)
        val r = matches.map { Pair(it.groups["n"]!!.value, it.groups["r"]!!.value) }.toList()
        if (r.isNotEmpty())
          recharges += r
      }
      if (recharges.isEmpty()) {
        toast(
          "Las recargas no pudieron ser obtenidas desde WhatsApp o no hay recargas en el los últimos chats",
          vibrate = true,
          waitForToastToHide = true
        )
        assert(false)
      }
      dataFetchedFromWA = true
      toast("Recargas obtenidas desde WhatsApp satisfactoriamente", isShortToast = true)
    }

    recharges.forEach {
      if (it.second.toInt() !in 25..1250) {
        toast(
          "Recarga de $$it es inválida, cada recarga debe estar entre $25 y $1250",
          vibrate = true,
          waitForToastToHide = true
        )
        assert(false)
      }
    }

    th.performFullProcessTillAuthentication(bank, pin)

    val confirmationMessages = mutableListOf<String>()
    for (index in 0..<recharges.count()) {
      if (index > 1)
        break

      th.selectBankTab(BankTab.Operaciones)
      th.selectBankOperation(BankOperation.RechargeMobile)

      val pair = recharges[index]
      val latestClientNumberRecharged = pair.first
//      dm.findObject("spTipoRecarga").text = "Recarga Móvil con tarjeta CUP"
      dm.selectDropDownMenuItem(
        resourceID = "spTipoRecarga",
        choice = 1,
        choicesCount = 2,
        isItemBelowTextInput = true,
        expectedTextAfterSelection = "Recarga Móvil con tarjeta CUP"
      )
      dm.findObject("txCuenta").text = pair.first
      dm.findObject("txMonto").text = pair.second

      th.selectAccountTypeToOperate(cardToUseDropDownMenuPosition)

      val latestMessage = th.getMessages().entries.last()
      th.accept()

      // Waiting for confirmation message
      var message = th.waitForConfirmationMessage(
        latestMessage = latestMessage,
        textToBePresent = "La recarga se realizo con exito"
      )
      message = message.replace(CASH_REPLACEMENT_RE, "CR 0.00")

      confirmationMessages.add(message)
      toast("Mensaje de confirmación capturado")

      // If recharging to same number, wait between recharges
      if (index + 1 < recharges.count() && latestClientNumberRecharged == recharges[index + 1].first) {
        // Waiting 1 min and 5 seconds
        toast("Esperando 1 minuto y 5 segundos para hacer la próxima recarga... Stay tuned!")
        toast("No salga de la app ni toque nada, pórtese bien", isShortToast = true)
        Thread.sleep(1000 * 60 + 1000 * 5)
      }
    }

    // Copying message to clipboard
    val clipboardContent = confirmationMessages.joinToString { "\n" }.trim()
    setClipboard(clipboardContent)
    toast("Mensajes de confirmación copiados al portapapeles")

    // If data was obtained from WhatsApp, an instance of it should be open in the background on the chat screen
    if (!waContact.isNullOrEmpty()) {
      wh.launch(clearOutPreviousInstances = !dataFetchedFromWA)
      if (!dataFetchedFromWA)
        wh.startConversation(waContact)
      if (joinMessages)
        wh.sendMessage(clipboardContent)
      else {
        confirmationMessages.forEach {
          wh.sendMessage(it.trim())
        }
      }
    }

    toast("Operaciones satisfactoriamente completadas... fino", waitForToastToHide = true)
  }

  @Test
  fun SendWhatsAppMessage() = runTest(
    includeWhatsAppVersionCompatibilityWarningToastOnErrors = true
  ) {
    wh.throwExceptionIfNotInstalled()
    val waContact = cliArguments.getString("waContact")!!
    val message = cliArguments.getString("message")

    log("========SendWhatsAppMessage========")
    log("waContact: $waContact")
    log("message: $message")
    log("===================================")

    wh.launch()
    wh.startConversation(waContact)
    if (message != null)
      wh.sendMessage(message)
  }

  @Test
  fun TransferCash() = runTest(
    includeTransfermovilVersionCompatibilityWarningToastOnErrors = true
  ) {
    var bank = cliArguments.getString("bank")!!.lowercase()
    if (bank == "metropolitano")
      bank = "metro"
    val pin = cliArguments.getString("pin")!!
    val cardToUseDropDownMenuPosition =
      cliArguments.getString("cardToUseDropDownMenuPosition")?.toInt()
    val recipientCard = cliArguments.getString("recipientCard")!!
    val cash = cliArguments.getString("cash")!!
    val mobileToConfirm = cliArguments.getString("mobileToConfirm")!!
    val recipientReceiveMyNumber = cliArguments.getBoolean("recipientReceiveMyNumber")

    log("========TransferCash========")
    log("bank: $bank")
    log("pin: $pin")
    log("cardToUseDropDownMenuPosition: $cardToUseDropDownMenuPosition")
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
    // Moneda
    dm.selectDropDownMenuItem(
      resourceID = "spinnerTipoMonedaImporte",
      choice = 1,
      choicesCount = 3,
      isItemBelowTextInput = true,
      expectedTextAfterSelection = "CUP"
    )
    // Tipo de cuenta a operar
    th.selectAccountTypeToOperate(
      cardToUseDropDownMenuPosition = cardToUseDropDownMenuPosition,
      isFiveEntriesDropDownMenu = true
    )
    // Móvil a confirmar
    dm.write("input_phone_confirm", mobileToConfirm)!!
    // El destinatario recibe mi número de móvil
    if (recipientReceiveMyNumber)
      dm.click("checkBoxMyPhone")

    val latestMessage = th.getMessages().entries.last()
    // Confirm dialogs
    th.accept()

    // Wait for confirmation message
    th.waitForConfirmationMessage(
      latestMessage = latestMessage,
      textToBePresent = "ransferencia fue completada"
    )

    // adb -s KRYX796HVGF67XWO shell am instrument -w -e class com.conexentools.InstrumentedTest#TransferCash -e bank metro -e pin 1064 -e cardToUseDropDownMenuPosition 1 -e recipientReceiveMyNumber false -e recipientCard 9224069991767498 -e cash 5 -e mobileToConfirm 55797140 com.conexentools.test/androidx.test.runner.AndroidJUnitRunner --no-window-animation --no-hidden-api-checks
  }

  @Test
  fun TestDropDownMenuSelector() = runTest {
    val choice = cliArguments.getString("choice")!!.toInt()
    val choicesCount = cliArguments.getString("choicesCount")!!.toInt()
    val expectedText = cliArguments.getString("expectedText")!!
    val isItemBelow = cliArguments.getBoolean("isItemBelow")
    val resourceID = cliArguments.getString("resourceId")!!

    log("========TestDropDownMenuSelector======")
    log("choice: $choice")
    log("choicesCount: $choicesCount")
    log("expectedText: $expectedText")
    log("isItemBelow: $isItemBelow")
    log("resourceID: $resourceID")
    log("======================================")

    dm.selectDropDownMenuItem(
      resourceID = resourceID,
      choice = choice,
      choicesCount = choicesCount,
      isItemBelowTextInput = isItemBelow,
      expectedTextAfterSelection = expectedText,
    )

    // adb shell am instrument -w -e class com.conexentools.InstrumentedTest#TestDropDownMenuSelector -e choice 1 -e choicesCount 2 -e expectedText Recarga\ Móvil\ con\ tarjeta\ CUP -e isItemBelow true -e resourceId spTipoRecarga com.conexentools.test/androidx.test.runner.AndroidJUnitRunner --no-window-animation --no-hidden-api-checks
  }

  @Test
  fun Test() {
    th.accept()
  }

  @Test
  fun PrintTransfermovilVersion() = th.printVersionInfo()

  @Test
  fun PrintWhatsAppVersionVersion() = wh.printVersionInfo()

  @Test
  fun sendWAMessage() = runTest(
    includeWhatsAppVersionCompatibilityWarningToastOnErrors = true,
  ) {
    val number: String? = cliArguments.getString("numero")
    val message: String? = cliArguments.getString("mensaje")

    assert(number != null)

    wh.launch(clearOutPreviousInstances = true)
    wh.startConversation(number!!)
    if (message != null)
      wh.sendMessage(message)
  }

  private fun runTest(
    startAtHome: Boolean = false,
    includeWhatsAppVersionCompatibilityWarningToastOnErrors: Boolean = false,
    includeTransfermovilVersionCompatibilityWarningToastOnErrors: Boolean = false,
    test: () -> Unit
  ) {
    try {
      if (startAtHome)
        device.pressHome()
      test()
    } catch (assertionExc: AssertionError) {
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
      throw ex
    } finally {
      if (includeTransfermovilVersionCompatibilityWarningToastOnErrors)
        th.checkVersionCompatibility()
      if (includeWhatsAppVersionCompatibilityWarningToastOnErrors)
        wh.checkVersionCompatibility()
    }
  }
}
