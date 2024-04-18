package com.conexentools.core.util

import android.icu.text.DateFormat
import android.view.KeyEvent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.PopUpToBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.conexentools.data.local.model.Client
import com.conexentools.presentation.components.common.cleanCubanMobileNumber
import com.conexentools.presentation.navigation.Screen
import contacts.core.entities.Contact
import contacts.core.util.phoneList
import java.time.Instant
import java.util.Date

@Composable
fun <viewModel : LifecycleObserver> viewModel.ObserveLifecycleEvents(lifecycle: Lifecycle) {
  DisposableEffect(lifecycle) {
    lifecycle.addObserver(this@ObserveLifecycleEvents)
    onDispose {
      lifecycle.removeObserver(this@ObserveLifecycleEvents)
    }
  }
}

fun String.truncate(maxLength: Int): String {
  return if (this.length > maxLength) {
    this.substring(0, maxLength - 3) + "..."
  } else {
    this
  }
}

fun String.toFormattedDate(): String {
  val date = Date.from(Instant.parse(this))
  return DateFormat.getDateTimeInstance().format(date)
}

fun String?.toInstant(): Instant? {
  return if (this == null)
    null
  else
    Instant.parse(this)
}

fun Modifier.moveFocusOnTabPressed(
  direction: FocusDirection,
  focusManager: FocusManager
): Modifier {
  return then(onPreviewKeyEvent {
    if (it.key == Key.Tab && it.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
      focusManager.moveFocus(direction)
      true
    } else {
      false
    }
  })
}

fun NavGraphBuilder.composable(
  screen: Screen,
  arguments: List<NamedNavArgument> = emptyList(),
  deepLinks: List<NavDeepLink> = emptyList(),
  enterTransition: (@JvmSuppressWildcards
  AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
  exitTransition: (@JvmSuppressWildcards
  AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
  popEnterTransition: (@JvmSuppressWildcards
  AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? =
    enterTransition,
  popExitTransition: (@JvmSuppressWildcards
  AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? =
    exitTransition,
  content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
  this.composable(
    route = screen.route,
    arguments = arguments,
    deepLinks = deepLinks,
    enterTransition = enterTransition,
    exitTransition = exitTransition,
    popEnterTransition = popEnterTransition,
    popExitTransition = popExitTransition,
    content = content
  )
}

//fun NavHostController.navigate(
//  screen: Screen,
//  builder: NavOptionsBuilder.() -> Unit,
//) {
//  navigate(
//    route = screen.route,
//    builder = builder
//  )
//}

fun NavController.navigate(
  screen: Screen,
) {
  navigate(route = screen.route)
}

fun NavController.navigate(screen: Screen, builder: NavOptionsBuilder.() -> Unit) {
  navigate(screen.route, navOptions(builder))
}

fun NavController.navigateAndPopDestinationFromTheBackStack(screen: Screen) {
  navigate(screen) {
    popUpTo(screen) {
      inclusive = true
    }
  }
}


fun NavOptionsBuilder.popUpTo(screen: Screen, popUpToBuilder: PopUpToBuilder.() -> Unit = {}) {
  popUpTo(
    route = screen.route,
    popUpToBuilder = popUpToBuilder
  )
}

fun Contact.toClient() = Client(
  name = this.displayNamePrimary ?: this.displayNameAlt ?: "",
  phoneNumber = this.phoneList()
    .firstOrNull()?.normalizedNumber?.cleanCubanMobileNumber(),
  cardNumber = null,
  latestRechargeDateISOString = null,
  imageUriString = (this.photoUri ?: this.photoThumbnailUri)?.toString(),
  quickMessage = null,
  rechargesMade = 0
)

fun Int.toUnicodeString() =  String(Character.toChars(this))
