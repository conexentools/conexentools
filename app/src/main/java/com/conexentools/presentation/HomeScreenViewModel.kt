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
import com.conexentools.core.util.CoroutinesDispatchers
import com.conexentools.core.util.RootUtil
import com.conexentools.core.util.log
import com.conexentools.core.util.logError
import com.conexentools.data.local.model.Client
import com.conexentools.domain.repository.AndroidUtils
import com.conexentools.domain.repository.UserPreferences
import com.conexentools.domain.use_cases.room.CountClientMatchesUseCase
import com.conexentools.domain.use_cases.room.DeleteClientUseCase
import com.conexentools.domain.use_cases.room.GetAllClientsUseCase
import com.conexentools.domain.use_cases.room.InsertClientUseCase
import com.conexentools.domain.use_cases.room.UpdateClientUseCase
import com.conexentools.presentation.components.common.enums.AppTheme
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
import javax.xml.transform.TransformerFactory

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
  private val coroutinesDispatchers: CoroutinesDispatchers,
  private val up: UserPreferences,
  private val getAllClientsUseCase: GetAllClientsUseCase,
  private val deleteClientUseCase: DeleteClientUseCase,
  private val insertClientUseCase: InsertClientUseCase,
  private val updateClientUseCase: UpdateClientUseCase,
  private val countClientMatchesUseCase: CountClientMatchesUseCase,
  private val au: AndroidUtils
) : ViewModel(), DefaultLifecycleObserver {

  private val _state = MutableStateFlow(HomeScreenState(HomeScreenLoadingState.ScreenLoading))
  val state: StateFlow<HomeScreenState> = _state.asStateFlow()

  private var rechargesMade = 0

  // States
  var bank = mutableStateOf("Metropolitano")
  var pin = mutableStateOf("")
  var waContact = mutableStateOf<String>("")
  var isManager = mutableStateOf(false)

  var firstClientNumber = mutableStateOf("")
  var firstClientRecharge = mutableStateOf("")
  var secondClientNumber = mutableStateOf<String?>(null)
  var secondClientRecharge = mutableStateOf("")

  var fetchDataFromWA = mutableStateOf(false)
  var cardLast4Digits = mutableStateOf("")

  var rechargesAvailabilityDateISOString = mutableStateOf<String?>(null)
  var waContactImageUri = mutableStateOf<Uri?>(null)

  var initialHomeScreenPage = mutableStateOf<HomeScreenPage?>(null)
  var appTheme = mutableStateOf(AppTheme.MODE_AUTO)
  var alwaysWaMessageByIntent = mutableStateOf(!RootUtil.isDeviceRooted)
  var appLaunchCount = mutableIntStateOf(0)
  var clientListPageHelpDialogsShowed = mutableStateOf(false)
  var savePin = mutableStateOf(false)

  private val whatsAppInstalledVersion = au.getPackageVersion(BuildConfig.WHATSAPP_PACKAGE_NAME)
  private val instrumentationAppVersion = au.getPackageVersion(BuildConfig.TEST_NAMESPACE)
  private val transfermovilInstalledVersion =
    au.getPackageVersion(BuildConfig.TRANSFERMOVIL_PACKAGE_NAME)

  private val _clients: MutableStateFlow<PagingData<Client>> =
    MutableStateFlow(value = PagingData.empty())
  val clients: MutableStateFlow<PagingData<Client>> get() = _clients

  init {
    log("Home View Model initialization")
    initialize()
  }

  private fun initialize() {

    viewModelScope.launch {
      val loadUserPreferencesDeferredJob = async { loadUserPreferences() }
      loadUserPreferencesDeferredJob.await()
    }.invokeOnCompletion {
      if (it != null) {
        logError("Fail at initialization. Cause ${it.localizedMessage}")
        _state.value = HomeScreenState(HomeScreenLoadingState.Error(it.localizedMessage))
      } else {
        _state.value = HomeScreenState(HomeScreenLoadingState.Success)
        log("Preferences Loaded")
      }
    }
  }

  fun initialClientsLoad() = viewModelScope.launch(coroutinesDispatchers.unconfined) {
    log("Running first client load")
    _state.value = HomeScreenState(HomeScreenLoadingState.ScreenLoading)
    val lodClientsDeferredJob = async { getClients() }
    lodClientsDeferredJob.await()
  }.invokeOnCompletion {
    if (it != null) {
      logError("Fail at initialization. Cause ${it.localizedMessage}")
      _state.value = HomeScreenState(HomeScreenLoadingState.Error(it.localizedMessage))
    } else {
      _state.value = HomeScreenState(HomeScreenLoadingState.Success)
      log("Initial clients loaded")
    }
  }

  private fun loadUserPreferences() = viewModelScope.launch(coroutinesDispatchers.unconfined) {
    try {
      bank.value = up.bank.first() ?: "Metropolitano"
      waContact.value = up.waContact.first() ?: ""
      waContactImageUri.value = up.waContactImageUriString.first()?.toUri()
      fetchDataFromWA.value = up.fetchDataFromWA.first() ?: false
      cardLast4Digits.value = up.cardLast4Digits.first() ?: ""
      firstClientRecharge.value = up.firstClientRecharge.first() ?: ""
      secondClientRecharge.value = up.secondClientRecharge.first() ?: ""
      rechargesAvailabilityDateISOString.value = up.rechargeAvailabilityDate.first()
      isManager.value = up.isManager.first() ?: false
      appTheme.value = AppTheme.fromOrdinal(up.appTheme.first())
      alwaysWaMessageByIntent.value = up.alwaysWaMessageByIntent.first() ?: false
      appLaunchCount.intValue = (up.appLaunchCount.first() ?: 0) + 1
      clientListPageHelpDialogsShowed.value = up.clientListPageHelpDialogsShowed.first() ?: false
      initialHomeScreenPage.value = HomeScreenPage.fromOrdinal(up.initialHomeScreenPage.first())
      savePin.value = up.savePin.first() ?: false
      if (savePin.value)
        pin.value = up.pin.first() ?: ""
      log("Preferences loaded")
    } catch (exc: Exception) {
      au.toast("Error al cargar las preferencias de usuario")
      au.toast(exc.message)
      throw exc
    }
  }

  private fun saveUserPreferences() = viewModelScope.launch(coroutinesDispatchers.unconfined) {
    up.saveBank(bank.value)
    up.saveWaContact(waContact.value)
    up.saveWaContactImageUriString(waContactImageUri.value?.toString())
    up.saveFetchDataFromWA(fetchDataFromWA.value)
    up.saveCardLast4Digits(cardLast4Digits.value)
    up.saveFirstClientRecharge(firstClientRecharge.value)
    up.saveSecondClientRecharge(secondClientRecharge.value)
    up.saveRechargeAvailabilityDateISOString(rechargesAvailabilityDateISOString.value)
    up.saveIsManager(isManager.value)
    up.saveInitialHomeScreenPage(initialHomeScreenPage.value?.ordinal)
    up.saveAppTheme(appTheme.value.ordinal)
    up.saveAlwaysWaMessageByIntent(alwaysWaMessageByIntent.value)
    up.saveAppLaunchCount(appLaunchCount.intValue)
    up.saveClientListPageHelpDialogsShowed(clientListPageHelpDialogsShowed.value)
    up.saveSavePin(savePin.value)
    up.savePin(if (savePin.value) pin.value else null)
  }

  private suspend fun getClients() =
    viewModelScope.launch(context = coroutinesDispatchers.unconfined) {
      getAllClientsUseCase()
        .distinctUntilChanged()
        .cachedIn(viewModelScope)
        .collect {
          _clients.value = it
        }
    }

  fun updateClient(client: Client) = viewModelScope.launch(coroutinesDispatchers.unconfined) {
    updateClientUseCase(client)
    getClients()
  }

  fun deleteClient(clientId: Long) = viewModelScope.launch(coroutinesDispatchers.unconfined) {
    deleteClientUseCase(clientId)
    getClients()
  }

  fun insertClient(client: Client) = viewModelScope.launch(coroutinesDispatchers.unconfined) {
    insertClientUseCase(client)
    getClients()
  }

  fun rechargeClient(client: Client) {
    val areMainConditionsToRunInstrumentedTestMet = areMainConditionsToRunInstrumentedTestMet()

    if (transfermovilInstalledVersion == null)
      return

    client.latestRechargeDateISOString = Instant.now().plus(15, ChronoUnit.MINUTES).toString()
    client.rechargesMade = (client.rechargesMade ?: 0) + 1
    updateClient(client)

    if (areMainConditionsToRunInstrumentedTestMet) {
      // TODO Launch recharge instrumented test
    } else {
      if (!au.launchPackage(transfermovilInstalledVersion.second))
        au.toast("Transfermóvil no pudo ser iniciado :(", vibrate = true)
    }
  }

  fun sendWAMessage(number: String, message: String?) {
    log("Sending message to number: $number, Message: $message")
    log("alwaysWaMessageByIntent: ${alwaysWaMessageByIntent.value}")
    if (whatsAppInstalledVersion == null) {
      au.toast(
        "WhatsApp (${BuildConfig.WHATSAPP_PACKAGE_NAME}) parece no estar instalado",
        vibrate = true
      )
      return
    }

    if (alwaysWaMessageByIntent.value || !RootUtil.isDeviceRooted) {
      au.sendWaMessage(number, message)
    } else {
      if (!RootUtil.isDeviceRooted) {
        au.toast(
          "Acceso root requerido para poder enviar el mensaje a través de WhatsApp",
          vibrate = true
        )
        au.toast("Active la opción en configuraciones 'Siempre API WA Message' para escribir el mensaje")
        au.toast("en la entrada de texto del chat. Esta opción no requiere acceso root")
        au.toast("pero el mensaje deberá ser enviado manualmente y es necesario tener una conexión de Internet activa")
      }

      // TODO send message through instrumented test
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

  fun getAdbInstrumentationRunCommand(): String {

    var command =
      "adb shell am instrument -w -e class ${BuildConfig.APPLICATION_ID}.InstrumentedTest#MakeMobileRecharge"
    if (!fetchDataFromWA.value) {
      command += " -e recargas ${firstClientNumber.value},${firstClientRecharge.value}"
      if (secondClientNumber.value != null)
        command += "@${secondClientNumber.value},${secondClientRecharge.value}"
    } else
      command += " -e contactoWA '${waContact.value}'"
    if (cardLast4Digits.value.isNotEmpty())
      command += " -e digitosTarjeta ${cardLast4Digits.value}"
    var bank_ = bank.value
    if (bank_ == "Metropolitano")
      bank_ = "metro"
    command += " -e pin ${pin.value} -e banco ${bank_.lowercase()} ${BuildConfig.TEST_NAMESPACE}/${BuildConfig.TEST_INSTRUMENTATION_RUNNER} --no-window-animation --no-hidden-api-checks"
    return command
  }

  private fun updateRechargeAvailability() {
    rechargesMade += if (secondClientNumber.value != null) 2 else 1
    rechargesAvailabilityDateISOString.value = if (rechargesMade < 2) {
      null
    } else {
      Instant.now().plus(1, ChronoUnit.DAYS).plusSeconds(60 * 15).toString()
    }
  }

  fun runInstrumentedTest() {

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
        "El monto de la recarga debe estar entre $25 y $1205"
      else ""
    }

    if (errorMessage.isEmpty()) {
      log("Running instrumented test")
      au.executeCommand(getAdbInstrumentationRunCommand().removePrefix("adb shell "), true)
      updateRechargeAvailability()
    } else
      au.toast(errorMessage, vibrate = true)
  }

  private fun areMainConditionsToRunInstrumentedTestMet(): Boolean {
    val maxPinLength = if (bank.value == "Metropolitano") 4 else 5

    val errorMessage = if (!RootUtil.isDeviceRooted)
      "Acceso root es requerido para ejecutar el test automatizado"
    else if (instrumentationAppVersion == null)
      "Conexen Tool - Instrumentation App, parece no estar instalada"
    else if (transfermovilInstalledVersion == null)
      "Transfermóvil parece no estar instalado"
    else if (cardLast4Digits.value.length in 1..3)
      "Especifique los cuatro dígitos de la tarjeta o deje el campo vacío"
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

  override fun onPause(owner: LifecycleOwner) {
    super.onPause(owner)
    if (state.value == HomeScreenState(HomeScreenLoadingState.Success))
      saveUserPreferences()
  }
}
