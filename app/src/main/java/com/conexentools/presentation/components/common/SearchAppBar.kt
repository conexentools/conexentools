package com.conexentools.presentation.components.common

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ContentAlpha
import com.conexentools.core.app.Constants
import com.conexentools.core.util.PreviewComposable

@Composable
fun SearchAppBar(
  text: MutableState<String>,
  onTextChange: (String) -> Unit = { },
  onNavigateBack: () -> Unit,
) {

  val focusRequester = remember { FocusRequester() }

  val trailingIcon = @Composable {
    PrimaryIconButton(Icons.Default.Close){
      onTextChange("")
      text.value = ""
    }
  }

  Surface(
    modifier = Modifier
      .fillMaxSize()
      .padding(Constants.Dimens.Small),
    color = Color.Transparent
  ) {
    TextField(
      value = text.value,
      onValueChange = {
        onTextChange(it)
        text.value = it
      },
      modifier = Modifier
        .focusRequester(focusRequester),
      placeholder = {
        Text(
          text = "Buscar...",
          modifier = Modifier.alpha(ContentAlpha.medium),
          textAlign = TextAlign.Center
        )
      },
      singleLine = true,
      leadingIcon = {
        PrimaryIconButton(Icons.AutoMirrored.Default.ArrowBack){
          onTextChange("")
          text.value = ""
          onNavigateBack()
        }
      },
      trailingIcon = if (text.value.isEmpty()) null else trailingIcon,
      colors = TextFieldDefaults.colors(
        unfocusedContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),//Color.Transparent,
        focusedContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),//Color.Transparent,
        disabledContainerColor = Color.Transparent,
        errorContainerColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
      ),
      shape = RoundedCornerShape(size = 30.dp)
    )

    LaunchedEffect(key1 = Unit) { focusRequester.requestFocus() }
  }
}

@Preview(showBackground = true, apiLevel = 33)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, apiLevel = 33)
@Composable
private fun PreviewSearchAppBar() {
  PreviewComposable(fillMaxSize = false) {
    Box(
      modifier = Modifier
        .height(65.dp)
        .background(MaterialTheme.colorScheme.background),
      ) {
      SearchAppBar(
        text = remember { mutableStateOf("") },
        onTextChange = { },
        onNavigateBack = { },
      )
    }
  }
}