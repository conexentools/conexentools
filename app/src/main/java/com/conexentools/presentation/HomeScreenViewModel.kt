package com.conexentools.presentation

import android.net.Uri
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.random.Random


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

  var firstClientNumber = mutableStateOf<String?>("")
  var firstClientRecharge = mutableStateOf<String?>("")
  var secondClientNumber = mutableStateOf<String?>(null)
  var secondClientRecharge = mutableStateOf<String?>(null)

  var fetchDataFromWA = mutableStateOf(false)
  var cardLast4Digits = mutableStateOf<String>("")

  //  var userRedirectedToSpecialPermissionsToAllowDisplayPopUpWhileRunningInTheBackground by mutableStateOf(false)
  var rechargesAvailabilityDateISOString = mutableStateOf<String?>(null)
  var waContactImageUri = mutableStateOf<Uri?>(null)

  var initialHomeScreenPage = mutableStateOf(HomeScreenPage.INSTRUMENTED_TEST)
  var appTheme = mutableStateOf(AppTheme.MODE_AUTO)
  var alwaysWaMessageByIntent = mutableStateOf(false)

  // Client List Screen
//  private val _searchText = MutableStateFlow("")
//  val searchText = _searchText.asStateFlow()

  //  val _clients: SnapshotStateList<Client> = mutableStateListOf()
  private val _clients: MutableStateFlow<PagingData<Client>> =
    MutableStateFlow(value = PagingData.empty())
  val clients: MutableStateFlow<PagingData<Client>> get() = _clients

//  val clients = searchText
//    .combine(clientsState) { text, _ ->//combine searchText with _contriesList
//      if (text.isBlank()) { //return the entire list of countries if not is typed
//        clientsState
//      }
//      clientsState.filter { client ->// filter and return a list of countries based on the text the user typed
//        val term = text.trim()
//        client.filter {  }
//        client.name.contains(term, ignoreCase = true) ||
//            client.cardNumber != null && client.cardNumber!!.contains(term, ignoreCase = true) ||
//            client.phoneNumber != null && client.phoneNumber!!.contains(term, ignoreCase = true)
//      }
//    }.stateIn(//basically convert the Flow returned from combine operator to StateFlow
//      scope = viewModelScope,
//      started = SharingStarted.WhileSubscribed(5000),//it will allow the StateFlow survive 5 seconds before it been canceled
//      initialValue = _clients as List<Client>
//    )

  init {
    initialize()
  }

  private fun initialize() {

    viewModelScope.launch {
      val loadUserPreferencesDeferredJob = async {
        loadUserPreferences()
      }
      val lodClientsDeferredJob = async {
        getAllClients()
      }
      log("Awaiting user preferences Job")
      loadUserPreferencesDeferredJob.await()
      log("Awaiting clients job")
      lodClientsDeferredJob.await()
      // Giving time for initial recomposition to finish
      delay(Random.nextLong(from = 800, until=1300))
    }.invokeOnCompletion {
      if (it != null) {
        log("Fail at initialization. Cause ${it.localizedMessage}")
        _state.value = HomeScreenState(HomeScreenLoadingState.Error(it.localizedMessage))
      } else {
        log("all done")
        _state.value = HomeScreenState(HomeScreenLoadingState.Success)
        log("Data Loaded")
      }
    }
  }

  private fun loadUserPreferences() = viewModelScope.launch {
    try {
      bank.value = up.bank.first() ?: "Metropolitano"
      waContact.value = up.waContact.first() ?: ""
      waContactImageUri.value = up.waContactImageUriString.first()?.toUri()
      fetchDataFromWA.value = up.fetchDataFromWA.first() ?: false
      cardLast4Digits.value = up.cardLast4Digits.first() ?: ""
      firstClientRecharge.value = up.firstClientRecharge.first() ?: ""
      secondClientRecharge.value = up.secondClientRecharge.first()
      rechargesAvailabilityDateISOString.value = up.rechargeAvailabilityDate.first()
      isManager.value = up.isManager.first() ?: false
      initialHomeScreenPage.value = HomeScreenPage.fromOrdinal(up.initialHomeScreenPage.first())
      appTheme.value = AppTheme.fromOrdinal(up.appTheme.first())
      alwaysWaMessageByIntent.value = up.alwaysWaMessageByIntent.first() ?: false

      log("Preferences successfully loaded")
    } catch (exc: Exception) {
      au.toast("Error al cargar las preferencias de usuario")
      au.toast(exc.message)
      throw exc
//      _state.value = HomeScreenState(HomeScreenLoadingState.Error(exc.localizedMessage))
    }
  }

  fun saveUserPreferences() = viewModelScope.launch {
    up.saveBank(bank.value)
    up.saveWaContact(waContact.value)
    up.saveWaContactImageUriString(waContactImageUri.value?.toString())
    up.saveFetchDataFromWA(fetchDataFromWA.value)
    up.saveCardLast4Digits(cardLast4Digits.value)
    up.saveFirstClientRecharge(firstClientRecharge.value)
    up.saveSecondClientRecharge(secondClientRecharge.value)
    up.saveRechargeAvailabilityDateISOString(rechargesAvailabilityDateISOString.value)
    up.saveIsManager(isManager.value)
    up.saveInitialHomeScreenPage(initialHomeScreenPage.value.ordinal)
    up.saveAppTheme(appTheme.value.ordinal)
    up.saveAlwaysWaMessageByIntent(alwaysWaMessageByIntent.value)
  }

  private suspend fun getAllClients() =
    viewModelScope.launch(context = coroutinesDispatchers.unconfined) {
      getAllClientsUseCase()
        .distinctUntilChanged()
        .cachedIn(viewModelScope)
        .collect {
          _clients.value = it
          log("Clients collected")
        }
      log("Clients successfully loaded")
    }

  fun updateClient(client: Client) = viewModelScope.launch {
    updateClientUseCase(client)
    getAllClients()
  }

  fun deleteClient(clientId: Long) = viewModelScope.launch {
    deleteClientUseCase(clientId)
    getAllClients()
  }

  fun insertClient(client: Client) = viewModelScope.launch {
    insertClientUseCase(client)
    getAllClients()
  }

//  fun onClientsSearchBarValueChange(text: String) {
//
//  }

  fun rechargeClient(client: Client) {
    client.latestRechargeDateISOString = Instant.now().plus(15, ChronoUnit.MINUTES).toString()
    client.rechargesMade = (client.rechargesMade ?: 0) + 1
    updateClient(client)
    // TODO recharge instrumented test
  }

  fun sendWAMessage(number: String, message: String?){
    log("Sending message to number: $number, Message: $message")
    log("alwaysWaMessageByIntent: ${alwaysWaMessageByIntent.value}")
    if (alwaysWaMessageByIntent.value || !RootUtil.isDeviceRooted){
      au.sendWaMessage(number, message)
    } else {
      // send message through instrumented test
    }
  }

  suspend fun countClientMatches(client: Client) = countClientMatchesUseCase(client)


//  companion object {
//    //    @SuppressLint("SimpleDateFormat")
////    val DATE_FORMATTER = SimpleDateFormat("d MMM, yyyy, hh:mm:ss a")
////    SimpleDateFormat.getDateInstance()
////    val s = getDateInstance().
//
//  }

//  fun fetchContacts() {
//    viewModelScope.launch {
//      val contactsListAsync = async { getPhoneContacts(appContext) }
////      val contactNumbersAsync = async { getContactNumbers() }
////      val contactEmailAsync = async { getContactEmails() }
//
//      val contacts = contactsListAsync.await()
////      val contactNumbers = contactNumbersAsync.await()
////      val contactEmails = contactEmailAsync.await()
//
////      contacts.forEach {
////        contactNumbers[it.id]?.let { numbers ->
////          it.numbers = numbers
////        }
////        contactEmails[it.id]?.let { emails ->
////          it.emails = emails
////        }
////      }
//      _contactsLiveData.postValue(contacts)
//    }
//  }

//  fun <T> LoadData(key: Preferences.Key<T>, default: T?): Flow<T?> =
//    appContext.dataStore.data.map { preferences ->
//      Log.i("<<CONEXEN>>", "Loading data for key: ${key.name}")
//      val data = preferences[key] ?: default
//      Log.i("<<CONEXEN>>", "Data Loaded for key: ${key.name}, value: $data")
//      data
//    }


//  fun canAddRecharge() = !fetchDataFromWA.value && secondClientNumber.value == null

  fun getAdbInstrumentationRunCommand(): String {

    var command =
      "adb shell am instrument -w -e class ${BuildConfig.APPLICATION_ID}.InstrumentedTest#MakeMobileRecharge"
    if (!fetchDataFromWA.value) {
      command += " -e recargas ${firstClientNumber.value},${firstClientRecharge.value}"
      if (secondClientNumber.value != null)
        command += "@${secondClientNumber.value},${secondClientRecharge.value}"
    } else
      command += " -e contactoWA \"${waContact.value}\""
    if (cardLast4Digits.value.isNotEmpty())
      command += " -e digitosTarjeta ${cardLast4Digits.value}"
    if (bank.value == "Metropolitano")
      bank.value = "metro"
    command += " -e pin ${pin.value} -e banco ${bank.value.lowercase()} ${BuildConfig.APPLICATION_ID}.test/${BuildConfig.RUNNER} --no-window-animation --no-hidden-api-checks"
    return command
  }

//  @RequiresApi(Build.VERSION_CODES.O)
//  fun GetRechargeAvailablityFormattedString(): String {
//    var rechargeAvailabilityDate = DATE_FORMATTER.parse(this.rechargeAvailabilityDate)!!.toInstant()
//    if (rechargeAvailabilityDate.compareTo(Instant.now()) < 1)
//      return ""
//
//    var date = Date.from(rechargeAvailabilityDate)
//    var formatter = DateFormat.getDateTimeInstance()
//    return formatter.format(date)
////    var duration = Duration.parse(date.toString())
////    var remaining = nextRechargeAvailabilityDate.minus(Instant.now().toEpochMilli(), ChronoUnit.MILLIS)
////    return Date.from(remaining)
//  }

  fun updateRechargeAvailability() {
    rechargesMade += if (secondClientNumber.value != null) 2 else 1
    rechargesAvailabilityDateISOString.value = if (rechargesMade < 2) {
      null
    } else {
      Instant.now().plus(1, ChronoUnit.DAYS).toString()
    }
//    areRechargesAvailable.value = false
  }

  fun runInstrumentationTest() {
    au.executeCommand(getAdbInstrumentationRunCommand().removePrefix("adb shell "), true)
  }

  override fun onPause(owner: LifecycleOwner) {
    super.onPause(owner)
    if (state.value == HomeScreenState(HomeScreenLoadingState.Success))
      saveUserPreferences()
  }
}

