package com.conexentools.presentation.components.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.conexentools.core.app.Constants
import com.conexentools.presentation.components.common.enums.ScreenSurfaceContentContainer
import my.nanihadesuka.compose.LazyColumnScrollbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenSurface(
  title: String,
  titleTextAlign: TextAlign = TextAlign.Center,
  surfaceModifier: Modifier = Modifier,
  lazyColumnModifier: Modifier = Modifier,
  lazyColumnHorizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
  lazyColumnVerticalArrangement: Arrangement.HorizontalOrVertical = Arrangement.Center,
  showTopAppBarHorizontalDivider: Boolean = false,
  bottomContent: @Composable () -> Unit = {},
  onNavigateBack: (() -> Unit)? = null,
  padding: PaddingValues = PaddingValues(horizontal = Constants.Dimens.Medium),
  defaultTopAppBarActions: @Composable (RowScope.() -> Unit) = {},
  defaultTopAppBarColors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
    containerColor = Color.Transparent,
    titleContentColor = MaterialTheme.colorScheme.onBackground,
    actionIconContentColor = MaterialTheme.colorScheme.primary
  ),
  customTopAppBar: @Composable (() -> Unit)? = null,
  defaultTopAppBarEnterTransition: EnterTransition = slideInHorizontally(
    initialOffsetX = { it },
    animationSpec = tween(durationMillis = 300)
  ),
  defaultTopAppBarExitTransition: ExitTransition = ExitTransition.None,
  customTopAppBarEnterTransition: EnterTransition = slideInHorizontally(
    initialOffsetX = { it },
    animationSpec = tween(durationMillis = 300)
  ),
  customTopAppBarExitTransition: ExitTransition = slideOutHorizontally(
    targetOffsetX = { -it },
    animationSpec = tween(durationMillis = 300)
  ),
  snackbarHost: @Composable () -> Unit = {},
  floatingActionButton: @Composable () -> Unit = {},
  scrollBehavior: TopAppBarScrollBehavior? = TopAppBarDefaults.enterAlwaysScrollBehavior(),
  contentContainer: ScreenSurfaceContentContainer = ScreenSurfaceContentContainer.LazyColumn,
  content: @Composable (() -> Unit)? = null,
) {

  Scaffold(
    topBar = {
      Column (modifier = Modifier.height(65.dp)) {
        AnimatedVisibility(
          visible = customTopAppBar == null,
          enter = defaultTopAppBarEnterTransition,
          exit = defaultTopAppBarExitTransition
        ) {

          @Composable
          fun title(textAlign: TextAlign = TextAlign.Center) {
            Text(
              text = title,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              style = MaterialTheme.typography.headlineSmall,
              textAlign = textAlign
            )
          }

          @Composable
          fun navIcon() =
            if (onNavigateBack != null)
              PrimaryIconButton(imageVector = Icons.AutoMirrored.Default.ArrowBack) { onNavigateBack() }
            else {
            }

          if (titleTextAlign == TextAlign.Center) {
            CenterAlignedTopAppBar(
              title = { title() },
              navigationIcon = { navIcon() },
              scrollBehavior = scrollBehavior,
              colors = defaultTopAppBarColors,
              actions = defaultTopAppBarActions
            )
          } else {
            TopAppBar(
              title = { title(titleTextAlign) },
              navigationIcon = { navIcon() },
              colors = defaultTopAppBarColors,
              scrollBehavior = scrollBehavior,
              actions = defaultTopAppBarActions
            )
          }
        }

        AnimatedVisibility(
          visible = customTopAppBar != null,
          enter = customTopAppBarEnterTransition,
          exit = customTopAppBarExitTransition
        ) {
          customTopAppBar?.invoke()
        }

        if (showTopAppBarHorizontalDivider)
          HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f))
      }
    },
    bottomBar = bottomContent,
    snackbarHost = snackbarHost,
    floatingActionButton = floatingActionButton
  ) { paddingValues ->

    Row(
      modifier = Modifier.fillMaxSize(),
      horizontalArrangement = Arrangement.Center
    ) {

      Surface(
        modifier = Modifier
          .background(MaterialTheme.colorScheme.background)
          .padding(
            PaddingValues(
              start = paddingValues.calculateStartPadding(LayoutDirection.Ltr) + padding.calculateStartPadding(LayoutDirection.Ltr),
              top = paddingValues.calculateTopPadding() + padding.calculateTopPadding(),
              end = paddingValues.calculateEndPadding(LayoutDirection.Ltr) + padding.calculateEndPadding(LayoutDirection.Ltr),
              bottom = paddingValues.calculateBottomPadding() + padding.calculateBottomPadding()
            )
          )
          .then(surfaceModifier.apply { if (scrollBehavior != null) nestedScroll(scrollBehavior.nestedScrollConnection) })
      ) {

        @Composable
        fun drawLazyColumn(state: LazyListState) {
          LazyColumn(
            state = state,
            horizontalAlignment = lazyColumnHorizontalAlignment,
            verticalArrangement = lazyColumnVerticalArrangement,
            modifier = Modifier
              .fillMaxSize()
              .then(lazyColumnModifier)
          ) {
            item { content!!() }
          }
        }

        content?.let {
          when (contentContainer) {
            ScreenSurfaceContentContainer.LazyColumn -> {
              val listState = rememberLazyListState()
              drawLazyColumn(listState)
            }

            ScreenSurfaceContentContainer.LazyColumnScrollable -> {
              val listState = rememberLazyListState()
              LazyColumnScrollbar(listState = listState) {
                drawLazyColumn(state = listState)
              }
            }

            ScreenSurfaceContentContainer.Surface -> {
              content()
            }
          }
        }
      }
    }
  }
}
