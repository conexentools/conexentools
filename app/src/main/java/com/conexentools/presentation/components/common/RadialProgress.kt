package com.conexentools.presentation.components.common

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.conexentools.core.util.PreviewComposable

@Composable
fun RadialProgressBar(
  modifier: Modifier = Modifier,
  value: Float,
  maxValue: Float = 100f,
  canvasSize: Dp? = 200.dp,
  strokeColor: Color = MaterialTheme.colorScheme.primary,
  backgroundStrokeColor: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f),
  lerpStrokeColor: Boolean = true,
  strokeColorOnCompleted: Color? = null,
  strokeWidthRatio: Float = 0.3f,
  strokeCap: StrokeCap = StrokeCap.Round,
  content: @Composable ColumnScope.() -> Unit,
) {
  val normalizedValue by animateFloatAsState(
    targetValue = value.coerceAtMost(maxValue) / maxValue,
    label = ""
  )
  val sk =
    if (lerpStrokeColor) strokeColorOnCompleted?.let { lerp(strokeColor, it, normalizedValue) }
      ?: strokeColor else strokeColor

  Column(
    modifier = modifier
      .drawBehind {
        val componentSize = if (canvasSize != null)
          Size(width = canvasSize.value, height = canvasSize.value)
        else null

        // Background Stroke
        indicator(
          componentSize = componentSize,
          strokeColor = backgroundStrokeColor,
          strokeWidthRatio = strokeWidthRatio,
          strokeCap = strokeCap,
        )
        // Stroke
        indicator(
          componentSize = componentSize,
          strokeColor = sk,
          strokeWidthRatio = strokeWidthRatio,
          sweepAngle = normalizedValue * 360f,
          strokeCap = strokeCap,
        )
      },
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    content()
  }
}

fun DrawScope.indicator(
  componentSize: Size?,
  strokeColor: Color,
  strokeWidthRatio: Float,
  sweepAngle: Float = 360f,
  strokeCap: StrokeCap = StrokeCap.Round
) {
  val maxSize = componentSize ?: size
  val strokeWidth = maxSize.width / 2 * strokeWidthRatio
  val s = Size(
    width = maxSize.width - strokeWidth,
    height = maxSize.width - strokeWidth
  )

  drawArc(
    size = s,
    color = strokeColor,
    startAngle = 270f,
    sweepAngle = sweepAngle,
    useCenter = false,
    style = Stroke(
      width = strokeWidth,
      cap = strokeCap
    ),
    topLeft = Offset(
      x = (size.width - s.width) / 2f,
      y = (size.height - s.height) / 2f,
    )
  )
}

@Preview(showBackground = true, apiLevel = 33)
@Preview(showBackground = true, apiLevel = 33, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewRadialProgressBar() {
  PreviewComposable(fillMaxSize = false) {
    Row(
      modifier = Modifier
        .size(200.dp)
        .background(MaterialTheme.colorScheme.background),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    )
    {
      Surface(
        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),//  MaterialTheme.colorScheme.background,
        modifier = Modifier.size(100.dp)
      ) {
        RadialProgressBar(
          value = 5f,
          maxValue = 10f,
          canvasSize = null,
          strokeColor = MaterialTheme.colorScheme.primary,
          backgroundStrokeColor = MaterialTheme.colorScheme.onPrimary,//.copy(alpha = 0.09f),
          lerpStrokeColor = false,
          strokeColorOnCompleted = Color.Green,
          strokeWidthRatio = 0.5f
        ) {
          Text(
            text = "123",
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.background)
          )
        }
      }
    }
  }
}
