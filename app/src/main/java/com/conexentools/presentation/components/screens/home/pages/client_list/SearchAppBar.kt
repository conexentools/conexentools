package com.conexentools.presentation.components.screens.home.pages.client_list

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ContentAlpha
import com.conexentools.core.util.PreviewComposable
import com.conexentools.presentation.components.common.PrimaryIconButton

@Composable
fun SearchAppBar(
  onTextChange: (String) -> Unit,
  onNavigateBack: () -> Unit,
) {

  val focusRequester = remember { FocusRequester() }
  var value by remember {
    mutableStateOf("")
  }

  val trailingIcon = @Composable {
    PrimaryIconButton(Icons.Default.Close){
      onTextChange("")
      value = ""
    }
  }

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .padding(5.dp)
      .height(56.dp),
    color = Color.Transparent
  ) {
    TextField(
      value = value,
      onValueChange = {
        onTextChange(it)
        value = it
      },
      modifier = Modifier
//        .fillMaxWidth()
//        .height(15.dp)
        .focusRequester(focusRequester),
      placeholder = {
        Text(
          text = "Buscar...",
//          color = Color.White,
          modifier = Modifier.alpha(ContentAlpha.medium)
        )
      },
      singleLine = true,
      leadingIcon = {
        PrimaryIconButton(Icons.AutoMirrored.Default.ArrowBack){
          onNavigateBack()
        }
      },
      trailingIcon = if (value.isEmpty()) null else trailingIcon,
//      keyboardOptions = KeyboardOptions(
//        imeAction = ImeAction.Done
//      ),
//      keyboardActions = KeyboardActions(
//        onSearch = {
//          onSearchClicked(value)
//        }
//      ),
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
fun PreviewSearchAppBar() {
  PreviewComposable(fillMaxSize = false) {
    Box(
      modifier = Modifier
        .height(100.dp)
        .background(MaterialTheme.colorScheme.background),
      ) {
      SearchAppBar(
        onTextChange = { },
        onNavigateBack = { },
      )
    }
  }
}