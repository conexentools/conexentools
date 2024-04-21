package com.conexentools.core.util

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.conexentools.presentation.theme.LocalTheme
import kotlin.math.min

private fun getMaskStyle(darkTheme: Boolean): SpanStyle =
  SpanStyle(color = Color.LightGray.copy(alpha = if (darkTheme) 0.4f else 1f))

fun textFilter(
  text: AnnotatedString,
  mask: String,
  separator: Char = ' ',
  darkTheme: Boolean,
  transformedToOriginalOffsetTranslator: (Int) -> Int = { min(it, text.length) },
  originalToTransformedOffsetTranslator: (Int) -> Int = { it },
): TransformedText {

  val annotatedString = AnnotatedString.Builder().run {
    var separatorCount = 0
    mask.forEachIndexed { index, char ->
      if (char == separator) {
        append(separator)
        separatorCount++
      } else if (index - separatorCount < text.length) {
        append(text[index - separatorCount])
      } else {
        withStyle(getMaskStyle(darkTheme)) {
          append(char)
        }
      }
    }
    toAnnotatedString()
  }

  val offsetTranslator = object : OffsetMapping {
    override fun transformedToOriginal(offset: Int) = transformedToOriginalOffsetTranslator(offset)
    override fun originalToTransformed(offset: Int) = originalToTransformedOffsetTranslator(offset)
  }

  return TransformedText(annotatedString, offsetTranslator)
}

fun cubanMobileNumberFilter(
  text: AnnotatedString,
  darkTheme: Boolean,
): TransformedText {
  return textFilter(
    text = text,
    mask = "_ _______",
    separator = ' ',
    darkTheme = darkTheme,
    transformedToOriginalOffsetTranslator = {
      val offset = when (it) {
        in 0..1 -> it
        in 2..9 -> it - 1
        else -> 8
      }
      min(offset, text.text.length)
    },
    originalToTransformedOffsetTranslator = {
      when (it) {
        in 0..1 -> it
        in 2..8 -> it + 1
        else -> 9
      }
    }
  )
}

fun cubanCardNumberFilter(
  text: AnnotatedString,
  mask: String = "XXXX XXXX XXXX XXXX",
  separator: Char = ' ',
  darkTheme: Boolean,
): TransformedText {
  return textFilter(
    text = text,
    mask = mask,
    separator = separator,
    darkTheme = darkTheme,
    transformedToOriginalOffsetTranslator = {
      val offset = when (it) {
        in 0..4 -> it
        in 5..8 -> it - 1
        in 9..12 -> it - 2
        else -> it - 3
      }
      min(offset, text.text.length)
    },
    originalToTransformedOffsetTranslator = {
      when (it) {
        in 0..4 -> it
        in 5..8 -> it + 1
        in 9..12 -> it + 2
        else -> it + 3
      }
    }
  )
}

@Preview(showBackground = true, apiLevel = 33)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, apiLevel = 33)
@Composable
private fun PreviewTextField() {
  PreviewComposable(fillMaxSize = true) {
    var text by remember { mutableStateOf("123") }
    val isDark = LocalTheme.current.isDark
    Column {
      val mask = "________________0111"
      val separator = ' '
      TextField(
        value = text,
        onValueChange = { text = it },
        visualTransformation = {
          textFilter(
            text = it,
            mask = mask,
            separator = separator,
            darkTheme = isDark,
          )
        }
      )
    }
  }

}