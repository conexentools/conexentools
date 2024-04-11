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
import androidx.navigation.compose.composable
import com.conexentools.presentation.navigation.Screen
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

fun NavController.navigate(screen: Screen) {
  navigate(screen.route)
}
