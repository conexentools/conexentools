package com.conexentools.presentation.components.common

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.conexentools.core.util.PreviewComposable
import com.conexentools.core.util.RemainingTimeTextRepresentation

@Composable
fun RadialProgressTimeIndicator(
  modifier: Modifier = Modifier,
  value: Float,
  maxValue: Float,
  timeNumericTextRepresentation: String,
  timeUnit: String,
  canvasSize: Dp? = null,
  strokeColor: Color = Color.Green,
  strokeColorOnCompleted: Color = Color.Red,
  strokeWidthRatio: Float = 0.4f,
  numberTextStyle: TextStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
  unitTextStyle: TextStyle = MaterialTheme.typography.titleSmall.copy(fontStyle = FontStyle.Italic),
) {

  Surface(modifier = modifier) {
    RadialProgressBar(
      value = value,
      maxValue = maxValue,
      canvasSize = canvasSize,
      strokeColor = strokeColor,
      strokeColorOnCompleted = strokeColorOnCompleted,
      strokeWidthRatio = strokeWidthRatio
    ) {

      Row(verticalAlignment = Alignment.Bottom) {
        Text(
          timeNumericTextRepresentation,
          style = numberTextStyle
        )
        Text(
          timeUnit,
          style = unitTextStyle
        )
      }
    }
  }
}


@Preview(showBackground = true, apiLevel = 33)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, apiLevel = 33)
@Composable
fun PreviewClientCard() {
  PreviewComposable(fillMaxSize = false) {
    val r = RemainingTimeTextRepresentation(10L, "25","h")
    RadialProgressTimeIndicator(
      value = 500f,
      maxValue = 60 * 60 * 24f, //24h
      timeNumericTextRepresentation = r.numericRepresentationPart,
      timeUnit = r.unit,
      canvasSize = 155.dp,
      strokeWidthRatio = 0.3f,
      modifier = Modifier
//        .defaultMinSize(minWidth = 45.dp)
//            .padding(Dimens.Small)
//              onLongClickLabel = "Timer long click"
        )

//      numberTextStyle =  MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
//      unitTextStyle =  MaterialTheme.typography.titleSmall.copy(fontStyle = FontStyle.Italic),

  }
}