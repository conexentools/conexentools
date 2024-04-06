package com.conexentools.presentation.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import com.conexentools.core.app.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenSurface(
  title: String,
  titleTextAlign: TextAlign = TextAlign.Center,
  surfaceModifier: Modifier = Modifier,
  lazyColumnModifier: Modifier = Modifier,
  horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
  verticalArrangement: Arrangement.HorizontalOrVertical = Arrangement.Center,
  bottomContent: @Composable () -> Unit = {},
  onNavigateBack: () -> Unit,
  content: @Composable (LazyItemScope.() -> Unit)? = null,
) {
  Scaffold(
    topBar = {
      @Composable
      fun title() {
        Text(
          text = title,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          style = MaterialTheme.typography.headlineSmall
        )
      }

      @Composable
      fun navIcon() =
        PrimaryIconButton(imageVector = Icons.AutoMirrored.Default.ArrowBack) { onNavigateBack() }

      if (titleTextAlign == TextAlign.Center) {
        CenterAlignedTopAppBar(
          title = { title() },
          navigationIcon = { navIcon() }
        )
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
        .then(surfaceModifier)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
//        verticalAlignment = verticalAlignment,

      ) {
        LazyColumn(
          horizontalAlignment = horizontalAlignment,
          verticalArrangement = verticalArrangement,
          modifier = Modifier
//            .fillMaxHeight()
//            .fillMaxWidth()
            .then(lazyColumnModifier)
        ) {
          content?.let {
            item { it() }
          }
        }
      }
    }
  }

}