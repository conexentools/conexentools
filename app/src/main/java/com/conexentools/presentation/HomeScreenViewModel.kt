package com.conexentools.presentation

import android.net.Uri
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.conexentools.BuildConfig
import com.conexentools.core.app.Constants
import com.conexentools.core.util.CoroutinesDispatchers
import com.conexentools.core.util.RootUtil
import com.conexentools.core.util.log
import com.conexentools.core.util.logError
import com.conexentools.data.local.model.Client
import com.conexentools.domain.repository.AndroidUtils
import com.conexentools.domain.repository.UserPreferences
import com.conexentools.domain.use_cases.room.CleanDatabaseUseCase
import com.conexentools.domain.use_cases.room.CountClientMatchesUseCase
import com.conexentools.domain.use_cases.room.DeleteClientUseCase
import com.conexentools.domain.use_cases.room.GetAllClientsUseCase
import com.conexentools.domain.use_cases.room.InsertClientUseCase
import com.conexentools.domain.use_cases.room.UpdateClientUseCase
import com.conexentools.presentation.components.common.enums.AppTheme
import com.conexentools.presentation.components.common.enums.InstrumentedTest
import com.conexentools.presentation.components.screens.home.enums.HomeScreenPage
import com.conexentools.presentation.components.screens.home.state.HomeScreenLoadingState
import com.conexentools.presentation.components.screens.home.state.HomeScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
  private val coroutinesDispatchers: CoroutinesDispatchers,
  private val up: UserPreferences,
  private val getAllClientsUseCase: GetAllClientsUseCase,
  private val deleteClientUseCase: DeleteClientUseCase,
  private val insertClientUseCase: InsertClientUseCase,
  private val updateClientUseCase: UpdateClientUseCase,
  private val countClientMatchesUseCase: CountClientMatchesUseCase,
  private val cleanDatabaseUseCase: CleanDatabaseUseCase,
  private val au: AndroidUtils
) : ViewModel(), DefaultLifecycleObserver {

  private val _state = MutableStateFlow(HomeScreenState(HomeScreenLoadingState.ScreenLoading))
  val state: StateFlow<HomeScreenState> = _state.asStateFlow()

  private var rechargesMade = 0

  // States
  var bank = mutableStateOf("Metropolitano")
  var pin = mutableStateOf("")
  var waContact = mutableStateOf<String>("")

  var firstClientNumber = mutableStateOf("")
  var firstClientRecharge = mutableStateOf("")
  var secondClientNumber = mutableStateOf<String?>(null)
  var secondClientRecharge = mutableStateOf("")

  var fetchDataFromWA = mutableStateOf(false)
  var cardToUseAlias = mutableStateOf("")
  var defaultMobileToSendCashTransferConfirmation = mutableStateOf("")

  var rechargesAvailabilityDateISOString = mutableStateOf<String?>(null)
  var waContactImageUri = mutableStateOf<Uri?>(null)

  var initialHomeScreenPage = mutableStateOf<HomeScreenPage?>(null)
  var appTheme = mutableStateOf(AppTheme.MODE_AUTO)
  var alwaysWaMessageByIntent = mutableStateOf(!RootUtil.isDeviceRooted)
  var appLaunchCount = mutableIntStateOf(0)
  var clientListPageHelpDialogsShowed = mutableStateOf(false)
  var savePin = mutableStateOf(false)
  var joinMessages = mutableStateOf(true)
  val homeScreenClientListScrollPosition = mutableIntStateOf(0)
  val cashToTransfer = mutableStateOf("")
  val sendWhatsAppMessageOnTransferCashTestCompleted = mutableStateOf(false)
  val whatsAppMessageToSendOnTransferCashTestCompleted = mutableStateOf("")
  val recipientReceiveMyMobileNumberAfterCashTransfer = mutableStateOf(false)
  val whatsAppInstalledVersion = au.getPackageVersion(BuildConfig.WHATSAPP_PACKAGE_NAME)
  val transfermovilInstalledVersion =
    au.getPackageVersion(BuildConfig.TRANSFERMOVIL_PACKAGE_NAME)
  val instrumentationAppInstalledVersion = au.getPackageVersion(BuildConfig.TEST_NAMESPACE)

  private val _clients = MutableStateFlow(value = PagingData.empty<Client>())
  val clients: StateFlow<PagingData<Client>> get() = _clients

  init {
    log("Home View Model initialization")
    initialize()
  }

  private fun initialize() {

    viewModelScope.launch {
      cleanDatabaseUseCase()
      val loadUserPreferencesDeferredJob = async { loadUserPreferences() }
      loadUserPreferencesDeferredJob.await()
    }.invokeOnCompletion {
      if (it != null) {
        logError("Fail at initialization. Cause ${it.localizedMessage}")
        _state.value = HomeScreenState(HomeScreenLoadingState.Error(it.localizedMessage))
      } else {
        _state.value = HomeScreenState(HomeScreenLoadingState.Success)
      }
    }
  }

  fun initialClientsLoad() = viewModelScope.launch(coroutinesDispatchers.unconfined) {
    log("Running first client load")
    _state.value = HomeScreenState(HomeScreenLoadingState.ScreenLoading)
    getClients()
  }.invokeOnCompletion {
    if (it != null) {
      logError("Fail at initial clients load. Cause ${it.localizedMessage}")
      _state.value = HomeScreenState(HomeScreenLoadingState.Error(it.localizedMessage))
    } else {
      _state.value = HomeScreenState(HomeScreenLoadingState.Success)
      log("Initial clients loaded")
    }
  }

  private fun loadUserPreferences() = viewModelScope.launch(coroutinesDispatchers.unconfined) {
    try {
      waContact.value = up.waContact.first() ?: ""
      bank.value = up.bank.first() ?: "Metropolitano"
      joinMessages.value = up.joinMessages.first() ?: true
      cashToTransfer.value = up.cashToTransfer.first() ?: ""
      appTheme.value = AppTheme.fromOrdinal(up.appTheme.first())
      fetchDataFromWA.value = up.fetchDataFromWA.first() ?: false
      appLaunchCount.intValue = (up.appLaunchCount.first() ?: 0) + 1
      firstClientRecharge.value = up.firstClientRecharge.first() ?: ""
      secondClientRecharge.value = up.secondClientRecharge.first() ?: ""
      waContactImageUri.value = up.waContactImageUriString.first()?.toUri()
      alwaysWaMessageByIntent.value = up.alwaysWaMessageByIntent.first() ?: false
      rechargesAvailabilityDateISOString.value = up.rechargeAvailabilityDate.first()
      cardToUseAlias.value = up.cardToUseAlias.first() ?: ""
      clientListPageHelpDialogsShowed.value = up.clientListPageHelpDialogsShowed.first() ?: false
      homeScreenClientListScrollPosition.intValue =
        up.homeScreenClientListScrollPosition.first() ?: 0
      defaultMobileToSendCashTransferConfirmation.value =
        up.defaultMobileToSendCashTransferConfirmation.first() ?: ""
      sendWhatsAppMessageOnTransferCashTestCompleted.value =
        up.sendWhatsAppMessageOnTransferCashTestCompleted.first() ?: false
      recipientReceiveMyMobileNumberAfterCashTransfer.value =
        up.recipientReceiveMyMobileNumberAfterCashTransfer.first() ?: true
      whatsAppMessageToSendOnTransferCashTestCompleted.value =
        up.whatsAppMessageToSendOnTransferCashTestCompleted.first()
          ?: Constants.Messages.CLIENT_MESSAGE_TO_SEND_ON_TRANSFER_CASH_TEST_COMPLETED
      savePin.value = up.savePin.first() ?: false
      if (savePin.value)
        pin.value = up.pin.first() ?: ""

      // Keep initialHomeScreenPage as latest preference to load
      initialHomeScreenPage.value = HomeScreenPage.fromOrdinal(up.initialHomeScreenPage.first())
      log("Preferences loaded")
    } catch (exc: Exception) {
      au.toast("Error al cargar las preferencias de usuario")
      au.toast(exc.message)
      throw exc
    }
  }

  private fun saveUserPreferences() = viewModelScope.launch(coroutinesDispatchers.unconfined) {
    up.saveBank(bank.value)
    up.saveSavePin(savePin.value)
    up.saveWaContact(waContact.value)
    up.saveAppTheme(appTheme.value.ordinal)
    up.saveJoinMessages(joinMessages.value)
    up.saveCashToTransfer(cashToTransfer.value)
    up.saveFetchDataFromWA(fetchDataFromWA.value)
    up.saveAppLaunchCount(appLaunchCount.intValue)
    up.savePin(if (savePin.value) pin.value else null)
    up.saveFirstClientRecharge(firstClientRecharge.value)
    up.saveSecondClientRecharge(secondClientRecharge.value)
    up.saveAlwaysWaMessageByIntent(alwaysWaMessageByIntent.value)
    up.saveInitialHomeScreenPage(initialHomeScreenPage.value?.ordinal)
    up.saveWaContactImageUriString(waContactImageUri.value?.toString())
    up.saveCardToUseAlias(cardToUseAlias.value)
    up.saveClientListPageHelpDialogsShowed(clientListPageHelpDialogsShowed.value)
    up.saveRechargeAvailabilityDateISOString(rechargesAvailabilityDateISOString.value)
    up.saveHomeScreenClientListScrollPosition(homeScreenClientListScrollPosition.intValue)
    up.saveDefaultMobileToSendCashTransferConfirmation(defaultMobileToSendCashTransferConfirmation.value)
    up.saveSendWhatsAppMessageOnTransferCashTestCompleted(
      sendWhatsAppMessageOnTransferCashTestCompleted.value
    )
    up.saveRecipientReceiveMyMobileNumberAfterCashTransfer(
      recipientReceiveMyMobileNumberAfterCashTransfer.value
    )
    up.saveWhatsAppMessageToSendOnTransferCashTestCompleted(
      whatsAppMessageToSendOnTransferCashTestCompleted.value.ifEmpty { null }) // If empty save null to delete the preference key and load default value next time
  }

  private suspend fun getClients() =
    viewModelScope.launch(context = coroutinesDispatchers.unconfined) {
      log("getting clients")
      getAllClientsUseCase()
        .distinctUntilChanged()
        .cachedIn(viewModelScope)
        .collect {
          _clients.value = it
          log("Clients updated")
        }
    }

  fun updateClient(client: Client) = viewModelScope.launch(coroutinesDispatchers.unconfined) {
    log("Updating client: $client")
    updateClientUseCase(client)
  }

  fun deleteClient(clientId: Long) = viewModelScope.launch(coroutinesDispatchers.unconfined) {
    deleteClientUseCase(clientId)
  }

  fun insertClient(client: Client) = viewModelScope.launch(coroutinesDispatchers.unconfined) {
    insertClientUseCase(client)
  }

  fun transferCash(
    client: Client,
    mobileToConfirm: String,
    numberToSendWhatsAppMessage: String?
  ) {

    if (client.cardNumber == null)
      throw Exception("Client card must not be null at this point")

    val areMainConditionsToRunInstrumentedTestMet = areMainConditionsToRunInstrumentedTestMet()

    if (areMainConditionsToRunInstrumentedTestMet) {
      client.latestRechargeDateISOString = Instant.now().plus(15, ChronoUnit.MINUTES).toString()
      client.rechargesMade = (client.rechargesMade ?: 0) + 1
      updateClient(client)

      runTransferCashInstrumentedTest(
        recipientCard = client.cardNumber!!,
        mobileToConfirm = mobileToConfirm,
        numberToSendWhatsAppMessage = numberToSendWhatsAppMessage
      )
    } else if (transfermovilInstalledVersion != null) {
      // Launching Transfermovil in case instrumented test couldn't be executed, to at least do something
      au.launchPackage(BuildConfig.TRANSFERMOVIL_PACKAGE_NAME)
    }
  }

  fun sendWhatsAppMessage(number: String, message: String?) {
    log("Sending WhatsApp message, alwaysWaMessageByIntent: ${alwaysWaMessageByIntent.value}")
    if (whatsAppInstalledVersion == null) {
      au.toast(
        "WhatsApp (${BuildConfig.WHATSAPP_PACKAGE_NAME}) parece no estar instalado",
        vibrate = true
      )
      return
    }

    if (alwaysWaMessageByIntent.value || !RootUtil.isDeviceRooted) {
      var num = number
      if (number.length == 8)
        num = "53$number"
      au.sendWaMessage(num, message)
    } else if (instrumentationAppInstalledVersion == null) {
      au.toast("Conexen Tool - Instrumentation App, parece no estar instalada", vibrate = true)
      return
    } else {
      val command = getShellCommandToRunSendWhatsAppMessageInstrumentedTest(number, message)
      runInstrumentedTest(command)
    }
  }

  fun checkIfClientIsPresentInDatabase(client: Client, onClientNotPresent: () -> Unit = {}) {
    var clientExists = false
    viewModelScope.launch(context = coroutinesDispatchers.main) {
      val count = countClientMatchesUseCase(client)
      log("count: $count")
      clientExists = count > 0
    }.invokeOnCompletion { exception ->
      if (exception == null) {
        if (clientExists) {
          au.toast("El cliente '${client.name}' ya existe", vibrate = true, shortToast = true)
        } else {
          onClientNotPresent()
        }
      } else {
        au.toast(
          "Un error ocurrió mientras se contaban los clientes en la base de datos",
          vibrate = true
        )
        au.toast(exception.localizedMessage)
      }
    }
  }

  private fun getInstrumentedTestShellCommand(
    test: InstrumentedTest,
    args: String
  ): String {
    return StringBuilder().apply {
      append("am instrument -w -e class ${BuildConfig.APPLICATION_ID}.InstrumentedTest#$test")
      append(" ${args.trim()} ${BuildConfig.TEST_NAMESPACE}/${BuildConfig.TEST_INSTRUMENTATION_RUNNER} --no-window-animation")
    }.toString()
  }

  private fun getShellCommandToRunSendWhatsAppMessageInstrumentedTest(
    number: String,
    message: String?
  ): String {
    val args = StringBuilder().apply {
      // Contact
      append("-e waContact \"\"\"$number\"\"\"")

      // Message
      if (message != null) {
        append(" -e message \"\"\"$message\"\"\"")
      }
    }.toString()

    return getInstrumentedTestShellCommand(
      test = InstrumentedTest.SendWhatsAppMessage,
      args = args
    )
  }

  fun getShellCommandToRunRechargeMobileInstrumentedTest(): String {

    val args = StringBuilder().apply {
      // Recharges
      if (!fetchDataFromWA.value) {
        append("-e recharges ${firstClientNumber.value},${firstClientRecharge.value}")
        if (secondClientNumber.value != null)
          append("@${secondClientNumber.value},${secondClientRecharge.value}")
      } else
      // WA Contact
        append(" -e waContact \"\"\"${waContact.value}\"\"\"")

      if (!joinMessages.value)
        append(" -e joinMessages false")

      // Card to use DropDownMenu position
      if (cardToUseAlias.value.isNotEmpty())
        append(" -e cardToUseAlias ${cardToUseAlias.value}")

      // Bank
      var bank_ = bank.value
      if (bank_ == "Metropolitano")
        bank_ = "metro"
      append(" -e pin ${pin.value} -e bank ${bank_.lowercase()}")
    }.toString()

    return getInstrumentedTestShellCommand(
      test = InstrumentedTest.RechargeMobile,
      args = args
    )
  }

  fun getShellCommandToRunTransferCashInstrumentedTest(
    recipientCard: String,
    mobileToConfirm: String,
    waContact: String?
  ): String {
    val args = StringBuilder().apply {

      // Card to use DropDownMenu position
      if (cardToUseAlias.value.isNotEmpty())
        append(" -e cardToUseAlias ${cardToUseAlias.value}")

      // Recipient receive my number
      if (recipientReceiveMyMobileNumberAfterCashTransfer.value)
        append(" -e recipientReceiveMyNumber ${recipientReceiveMyMobileNumberAfterCashTransfer.value}")

      // WhatsApp contact
      if (waContact != null) {
        append(" -e waContact \"\"\"$waContact\"\"\"")

        // Message
        if (whatsAppMessageToSendOnTransferCashTestCompleted.value.isNotEmpty()) {
          append(" -e message \"\"\"${whatsAppMessageToSendOnTransferCashTestCompleted.value}\"\"\"")
        }
      }

      // Bank
      var bank_ = bank.value
      if (bank_ == "Metropolitano")
        bank_ = "metro"

      append(
        " -e pin ${pin.value} -e bank ${bank_.lowercase()} -e recipientCard $recipientCard -e cash ${cashToTransfer.value} -e mobileToConfirm ${
          mobileToConfirm.replace(
            " ",
            "\\ "
          )
        }"
      )
    }.toString()

    return getInstrumentedTestShellCommand(
      test = InstrumentedTest.TransferCash,
      args = args
    )
  }

  private fun updateRechargeAvailability() {
    rechargesMade += if (secondClientNumber.value != null) 2 else 1
    rechargesAvailabilityDateISOString.value = if (rechargesMade < 2) {
      null
    } else {
      Instant.now().plus(1, ChronoUnit.DAYS).plusSeconds(60 * 15).toString()
    }
  }

  private fun runTransferCashInstrumentedTest(
    recipientCard: String,
    mobileToConfirm: String,
    numberToSendWhatsAppMessage: String?,
  ) {
    val command = getShellCommandToRunTransferCashInstrumentedTest(
      recipientCard,
      mobileToConfirm,
      numberToSendWhatsAppMessage
    )
    runInstrumentedTest(command)
  }

  fun runRechargeMobileInstrumentedTest() {

    if (!areMainConditionsToRunInstrumentedTestMet())
      return

    val errorMessage = if (fetchDataFromWA.value) {
      if (whatsAppInstalledVersion == null)
        "WhatsApp (${BuildConfig.WHATSAPP_PACKAGE_NAME}) parece no estar instalado"
      else if (waContact.value.isEmpty())
        "Especifique el nombre|número del contacto de WhatsApp que le envió los números a recargar"
      else ""
    } else {
      if (firstClientNumber.value.length != 8 || (secondClientNumber.value != null && secondClientNumber.value!!.length != 8))
        "El número del cliente a recargar debe tener 8 dígitos"
      else if ((firstClientRecharge.value.isEmpty() || firstClientRecharge.value.toInt() !in 25..1250) ||
        (secondClientNumber.value != null && (secondClientRecharge.value.isEmpty() || secondClientRecharge.value.toInt() !in 25..1250))
      )
        "El monto de la recarga debe estar entre $25 y $1250"
      else ""
    }

    if (errorMessage.isEmpty()) {
      log("Running RechargeMobileInstrumentedTest instrumented test")
      updateRechargeAvailability()
      val command = getShellCommandToRunRechargeMobileInstrumentedTest()
      runInstrumentedTest(command)
    } else
      au.toast(errorMessage, vibrate = true)
  }

  private fun areMainConditionsToRunInstrumentedTestMet(): Boolean {
    val maxPinLength = if (bank.value == "Metropolitano") 4 else 5

    val errorMessage = if (!RootUtil.isDeviceRooted)
      "Es requerido acceso root para ejecutar el test automatizado"
    else if (instrumentationAppInstalledVersion == null)
      "Conexen Tool - Instrumentation App, parece no estar instalada"
    else if (transfermovilInstalledVersion == null)
      "Transfermóvil parece no estar instalado"
    else if (pin.value.length != maxPinLength)
      "El PIN debe tener $maxPinLength dígitos"
    else ""

    return if (errorMessage.isEmpty()) {
      true
    } else {
      au.toast(errorMessage, vibrate = true)
      false
    }
  }

  private fun runInstrumentedTest(command: String) {
    saveUserPreferences().invokeOnCompletion {
      if (it == null){
        au.executeCommand(command, su = true)
      } else {
        au.toast("La prueba automatizada no pudo ser iniciada, ocurrió un error al guardar las preferencias de usuario")
        au.toast(it.localizedMessage)
      }
    }
  }

  override fun onPause(owner: LifecycleOwner) {
    super.onPause(owner)
//    log("onPause")
    if (state.value == HomeScreenState(HomeScreenLoadingState.Success))
      saveUserPreferences()
  }

  override fun onDestroy(owner: LifecycleOwner) {
    super.onDestroy(owner)
//    log("onDestroy")
    if (state.value == HomeScreenState(HomeScreenLoadingState.Success))
      saveUserPreferences()
  }
}
