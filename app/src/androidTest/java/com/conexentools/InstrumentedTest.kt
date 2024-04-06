package com.conexentools

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.conexentools.Utils.Companion.setClipboard
import com.conexentools.Utils.Companion.toast
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 18) //@androidx.annotation.UiThread


class InstrumentedTest {

  //  private val WA_PN = "com.whatsapp"
//  private val WABusiness_PN = "com.whatsapp.w4b"
//  private val WAPlus_PN = "com.aero"
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

    // TODO handle errors informing the user with toasts
    // TODO test with my number
    // TODO val chats = wh.getLatestChatsInConversation(getYourChats = true) to false
    // select SESION only if it is not already selected
    // Tipo de cuenta a operar is not workin, text inseertion is not workin, how to handle Drop Down menus in UiAutomator2
    // Opearciones tab is getting executed before message arrived, or that seems, implement toast system when message arrived
    // TODO Error Monto fuera de rango. Rl monto debe estar entre 25 y 1250

    device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
//    device.pressHome();
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
  fun MakeMobileRecharge() {

    try {

      if (th.installedVersion == null){
        toast("Por favor instale Transfermóvil", vibrate = true, waitForToastToHide = true)
        assert(false)
      }
//?.map { Pair(it.split(',')[0], it.split(',')[1]) }
//          ?.toMutableList()
      toast("Iniciando el proceso de automatización", Toast.LENGTH_SHORT)

      var recharges = cliArguments.getString("recargas")?.split("@")?.map { Pair(it.split(',')[0], it.split(',')[1]) }?.toMutableList()
      val waContact = cliArguments.getString("contactoWA")
      var bank = cliArguments.getString("banco")!!.lowercase()
      if (bank == "metropolitano")
        bank = "metro"
      val pin = cliArguments.getString("pin")!!
      val cardLast4Digits = cliArguments.getString("digitosTarjeta")

      var dataFetchedFromWA = false
      // Grab data from WA if transfers isNullOrEmpty

      if (recharges.isNullOrEmpty()) {

        if (wh.installedVersion == null){
          toast("Por favor instale WhatsApp", vibrate = true, waitForToastToHide = true)
          assert(false)
        }

        if (waContact.isNullOrEmpty()){
          toast("Cuando ninguna recarga es especificada un nombre de contacto de WA tiene que ser especificado", vibrate = true, waitForToastToHide = true)
          assert(false)
        }
        toast("Obteniendo datos desde WhatsApp", Toast.LENGTH_SHORT)
        wh.launch()
        wh.startConversation(waContact!!)
        val chats = wh.getLatestChatsInConversation(getYourChats = true)
        if (chats.isEmpty()){
          toast("Los últimos chats en la conversación no parecen ser los del contacto: $waContact, en cambio los tuyos", vibrate = true, waitForToastToHide = true)
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
        if (recharges.isEmpty()){
          toast("Las recargas no pudieron ser obtenidas desde WhatsApp o no hay recargas en el los últimos chats", vibrate = true, waitForToastToHide = true)
          assert(false)
        }
        dataFetchedFromWA = true
        toast("Recargas obtenidas desde WhatsApp satisfactoriamente", Toast.LENGTH_SHORT)
      }

      recharges.forEach {
        if (it.second.toInt() !in 25..1250){
          toast("Recarga de $$it es inválida, cada recarga debe estar entre $25 y $1250", vibrate = true, waitForToastToHide = true)
          assert(false)
        }
      }

      toast("Abriendo Transfermóvil", Toast.LENGTH_SHORT)
      // Launch Transfermóvil
      th.launch()

      // Confirm Welcome Message if present
      th.clickWelcomeMessageIfPresent()

      // Open lateral panel
      th.openLateralPanel()

      // Select bank
      th.selectBank(bank)

      // Authenticate
      if (!th.authenticate(pin)) {
        toast("Autenticación fallida", vibrate = true, waitForToastToHide = true)
        assert(false)
      }

      toast("Autenticado satisfactoriamente")

      val confirmationMessages = mutableListOf<String>()
      var latestClientNumberRecharged = ""
      for (index in 0..<recharges.count()) {
        if (index > 1)
          break

        th.selectBankTab("Operaciones")
        th.selectBankOperation("Recarga Saldo Móvil")

        val pair = recharges[index]
        latestClientNumberRecharged = pair.first
//      dm.findObject("spTipoRecarga").text = "Recarga Móvil con tarjeta CUP"
        dm.selectDropDownMenuChoice(resourceID = "spTipoRecarga", choice = 1)
        dm.findObject("txCuenta").text = pair.first
        dm.findObject("txMonto").text = pair.second

//      val spinnerTipoMonedaChoice = 1//dm.findObject("spinnerTipoMoneda")
        if (!cardLast4Digits.isNullOrEmpty()) {
//        spinnerTipoMoneda.text = "-MIS CUENTAS-"
          dm.selectDropDownMenuChoice(resourceID = "spinnerTipoMoneda", choice = 3)

          var text = "BANCO "
          text += if (bank == "metro")
            "METROPOLITANO"
          else
            bank.uppercase()
          text += " - ${cardLast4Digits.drop(cardLast4Digits.length - 4)}"
          dm.waitForObject("spinnerCuentas")!!.text = text
        } else
          dm.selectDropDownMenuChoice(resourceID = "spinnerTipoMoneda", choice = 1)
//        spinnerTipoMoneda.text = "CUP"

        val lastMessage = th.getMessages().entries.last()
        th.accept()
        var count = 0
        var currentMessage: Map.Entry<Int, String>
        toast("Esperando por mensaje de confirmación", Toast.LENGTH_SHORT)
        do {
          Thread.sleep(1000)
          currentMessage = th.getMessages().entries.last()
        } while (currentMessage == lastMessage && ++count < SECONDS_TO_WAIT_FOR_CONFIRMATION_MESSAGE)

        if (count >= SECONDS_TO_WAIT_FOR_CONFIRMATION_MESSAGE){
          toast("El mensaje de confirmación no fue recibido en $SECONDS_TO_WAIT_FOR_CONFIRMATION_MESSAGE segundos :(", vibrate = true, waitForToastToHide = true)
          assert(false)
        }
        var message = currentMessage.value
        if (!message.contains("La recarga se realizo con exito")){
          toast("Algo salió mal, la recarga parece no haberse realizado con éxito :(", vibrate = true, waitForToastToHide = true)
          assert(false)
        }
        message = message.replace(CASH_REPLACEMENT_RE, "CR 0.00")

        confirmationMessages.add(message)
        toast("Mensaje de confirmación capturado")

        // If recharging to same number wait between recharges
        if (index + 1 < recharges.count() && latestClientNumberRecharged == recharges[index + 1].first) {
          // Waiting 1 min and 5 seconds
          toast("Esperando 1 minuto y 5 segundos para hacer la próxima recarga... Stay tuned!")
          toast("No salga de la app ni toque nada, pórtese bien", Toast.LENGTH_SHORT)
          Thread.sleep(1000 * 60 + 1000 * 5)
        }
      }

      // Copying message to clipboard
      var clipboardContent = ""
      confirmationMessages.forEach { clipboardContent += (it + "\n") }
      clipboardContent = clipboardContent.trim()
      setClipboard(clipboardContent)
      toast("Mensajes de confirmación copiados al portapapeles")

      if (!waContact.isNullOrEmpty()) {
        wh.launch(clearOutPreviousInstances = !dataFetchedFromWA)
        if (!dataFetchedFromWA)
          wh.startConversation(waContact)
        wh.sendMessage(clipboardContent)
      }

      toast("Operaciones satisfactoriamente completadas... fino", waitForToastToHide = true)
    } catch (assertionExc: AssertionError){
      checkInstalledVersionsOfTMAndShowMessageIfNecessary()
      throw assertionExc
    }
    catch (ex: Exception){
      toast("Un error inesperado ocurrió", Toast.LENGTH_SHORT, vibrate = true)
      toast(ex.message.toString(), waitForToastToHide = true)
      checkInstalledVersionsOfTMAndShowMessageIfNecessary()
      throw ex
    }
  }

  fun checkInstalledVersionsOfTMAndShowMessageIfNecessary(){
    if (th.installedVersion != null && th.installedVersion != th.testedVersion){
      toast("Usted esta usando una versión de Transfermóvil diferente")
      toast( "a la usada para ejecutar la prueba de automatización", waitForToastToHide = true)
    }
  }

  @Test
  fun Test() {

  }

  @Test
  fun PrintInstalledTransfermovilVersion(){
    if (th.installedVersion == null)
      toast("Transfermóvil is not installed", vibrate = true, waitForToastToHide = true)
    else
      toast("Installed Transfermóvil:\n\t" +
          "Version Code: ${th.installedVersion!!.first}\n\t" +
          "Version Name: ${th.installedVersion!!.second}", waitForToastToHide = true)
  }

  @Test
  fun PrintTestedTransfermovilVersion(){
    toast("Tested Transfermóvil:\n\t" +
          "Version Code: ${th.testedVersion.first}\n\t" +
          "Version Name: ${th.testedVersion.second}", waitForToastToHide = true)
  }

  @Test
  fun sendWAMessage() {
    val number: String? = cliArguments.getString("numero")
    val message: String? = cliArguments.getString("mensaje")

    assert(number != null)

    wh.launch(clearOutPreviousInstances = true)
    wh.startConversation(number!!)
    if (message != null)
      wh.sendMessage(message)
  }
}
