package com.conexentools.presentation.components.screens.home.pages.client_list

import android.content.res.Configuration
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.conexentools.R
import com.conexentools.core.util.PreviewComposable
import com.conexentools.domain.repository.AndroidUtils

@Composable
fun PageLoader(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = stringResource(id = R.string.fetching_data),
      color = MaterialTheme.colorScheme.primary,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
    CircularProgressIndicator(Modifier.padding(top = 10.dp))
  }
}

@Composable
fun LoadingNextPageItem(modifier: Modifier) {
  CircularProgressIndicator(
    modifier = modifier
      .fillMaxWidth()
      .padding(10.dp)
      .wrapContentWidth(Alignment.CenterHorizontally)
  )
}

@Composable
fun ErrorMessage(
  message: String,
  modifier: Modifier = Modifier,
  onClickRetry: () -> Unit,
) {
  Row(
    modifier = modifier.padding(10.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    LazyColumn(
      modifier = Modifier
        .height(60.dp)
        .weight(0.8f)
    ) {
      item {
        Text(
          text = message,
          color = MaterialTheme.colorScheme.error
        )
      }
    }

    OutlinedButton(
      onClick = onClickRetry,
//      modifier = Modifier.weight(1f)
    ) {
      Text(text = stringResource(id = R.string.retry))
    }
  }
}

@Preview(apiLevel = 33)
@Preview(apiLevel = 33, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewErrorMessage() {
  PreviewComposable(fillMaxSize = false) {
    ErrorMessage(
      message = "Mensa de superbus brodium, tractare ventus!Sources, winds, and eternal individuals will always protect them. ",
//          modifier = modifier,
      onClickRetry = {}

    )
  }
}