package com.conexentools.presentation.components.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import com.conexentools.core.app.Constants
import com.conexentools.presentation.components.common.enums.ScreenSurfaceContentWrapper
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
  bottomContent: @Composable () -> Unit = {},
  onNavigateBack: () -> Unit,
  defaultTopAppBarActions: @Composable (RowScope.() -> Unit) = {},
  customTopAppBar: @Composable (() -> Unit)? = null,
  customTopAppBarExitTransition: ExitTransition = slideOutHorizontally(
    targetOffsetX = { -it },
    animationSpec = tween(durationMillis = 300)
  ) + shrinkVertically(
    animationSpec = tween(delayMillis = 300)
  ),
  screenSurfaceContentWrapper: ScreenSurfaceContentWrapper = ScreenSurfaceContentWrapper.LazyColumn,
  content: @Composable (() -> Unit)? = null,
) {
  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

  Scaffold(
    topBar = {

      Column {
        AnimatedVisibility(customTopAppBar == null) {

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
            PrimaryIconButton(imageVector = Icons.AutoMirrored.Default.ArrowBack) { onNavigateBack() }

          if (titleTextAlign == TextAlign.Center) {
            CenterAlignedTopAppBar(
              title = { title() },
              navigationIcon = { navIcon() },
              scrollBehavior = scrollBehavior,
              actions = defaultTopAppBarActions
            )
          } else {
            TopAppBar(
              title = { title(titleTextAlign) },
              navigationIcon = { navIcon() },
              scrollBehavior = scrollBehavior,
              actions = defaultTopAppBarActions
            )
          }
        }

        AnimatedVisibility(
          visible = customTopAppBar != null,
          exit = customTopAppBarExitTransition
        ) {
          customTopAppBar?.invoke()
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f))
      }
    },
    bottomBar = bottomContent
  ) { paddingValues ->
    Surface(
      modifier = Modifier
        .background(MaterialTheme.colorScheme.background)
        .padding(
          PaddingValues(
            start = paddingValues.calculateStartPadding(LayoutDirection.Ltr) + Constants.Dimens.Medium,
            top = paddingValues.calculateTopPadding(),
            end = paddingValues.calculateEndPadding(LayoutDirection.Ltr) + Constants.Dimens.Medium,
            bottom = paddingValues.calculateBottomPadding()
          )
        )
        .fillMaxSize()
        .nestedScroll(scrollBehavior.nestedScrollConnection)
        .then(surfaceModifier)
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
        when (screenSurfaceContentWrapper) {
          ScreenSurfaceContentWrapper.LazyColumn -> {
            val listState = rememberLazyListState()
            drawLazyColumn(listState)
          }

          ScreenSurfaceContentWrapper.LazyColumnScrollable -> {
            val listState = rememberLazyListState()
            LazyColumnScrollbar(listState = listState) {
              drawLazyColumn(state = listState)
            }
          }

          ScreenSurfaceContentWrapper.Surface -> {
            content()
          }
        }
      }

//        LazyColumn(
//          horizontalAlignment = horizontalAlignment,
//          verticalArrangement = verticalArrangement,
//          modifier = Modifier
//            .fillMaxSize()
//            .then(lazyColumnModifier)
//        ) {
//          content?.let {
//            item { it() }
//          }
//      }
    }
  }
}
