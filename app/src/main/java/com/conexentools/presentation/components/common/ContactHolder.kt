package com.conexentools.presentation.components.common

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.conexentools.R
import com.conexentools.core.app.Constants
import com.conexentools.core.util.PreviewComposable
import com.conexentools.core.util.toUnicodeString
import com.conexentools.presentation.components.screens.home.pages.client_list.clientsForTesting

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Contact(
  modifier: Modifier = Modifier,
  name: String,
  imageUriString: String?,
  showDivider: Boolean,
  isSelected: Boolean? = null,
  subtitle: String? = null,
  onSelected: (() -> Unit)? = null,
  onSubtitleLongClick: ((String) -> Unit)? = null,
  onClick: (() -> Unit)? = null,
  backgroundColor: Color = MaterialTheme.colorScheme.background,
  titleLetter: Color = backgroundColor,
  contactImageColor: Color = MaterialTheme.colorScheme.primary,
  extraContent: @Composable (RowScope.() -> Unit)? = null,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    var m = Modifier
      .fillMaxWidth()
      .height(Constants.Dimens.HorizontalCardHeight)
      .padding(PaddingValues(horizontal = Constants.Dimens.Medium))
      .background(backgroundColor)
      .then(modifier)
    if (onClick != null || onSelected != null)
      m = modifier.combinedClickable(
        onLongClick = onSelected,
        onClick = {
          onClick?.invoke()
        }
      )

    Row(
      modifier = m,
      verticalAlignment = Alignment.CenterVertically,
    ) {

      key(isSelected) {
        if (isSelected != null) {
          Checkbox(
            modifier = Modifier.width(35.dp),
            checked = isSelected,
            onCheckedChange = {
              onClick?.invoke()
            }
          )
        }
      }

      // Contact Image
      Box(
        modifier = Modifier.size(55.dp)
      ) {
        if (imageUriString.isNullOrEmpty()) {

          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(Constants.Dimens.MegaSmall)
              .clip(CircleShape)
              .background(contactImageColor)
          ) {
//            Icon(
//              imageVector = Icons.Filled.AccountCircle,
//              modifier = Modifier
//                .clip(CircleShape)
//                .border(
//                  width = Constants.Dimens.MegaSmall,
//                  shape = CircleShape,
//                  color = titleLetterColor//MaterialTheme.colorScheme.primary
//                ),
//              tint = backgroundColor,
//              contentDescription = null
//            )
            Text(
              text = if (name.isNotEmpty()) name.codePointAt(0).toUnicodeString().uppercase() else "",
              style = MaterialTheme.typography.headlineMedium,
              color = titleLetter,
              modifier = Modifier
                .fillMaxSize()
                .wrapContentHeight(align = Alignment.CenterVertically),
              textAlign = TextAlign.Center
            )

          }
        } else {
          AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(imageUriString)
              .crossfade(true)
              .build(),
            alignment = Alignment.Center,
            placeholder = painterResource(R.drawable.contact_image_placeholder_02),
            modifier = Modifier.clip(CircleShape),
            contentDescription = null
          )
        }
      }

      Spacer(modifier = Modifier.width(Constants.Dimens.Medium))

      Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = name,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        subtitle?.let {
          if (it.isNotEmpty()) {
            Text(
              text = it,
              style = MaterialTheme.typography.titleSmall,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = if (onSubtitleLongClick == null) Modifier else Modifier.combinedClickable(
                onLongClick = onSubtitleLongClick.let { { onSubtitleLongClick(subtitle) } },
                onClick = { onClick?.invoke() })
            )
          }
        }
      }

      extraContent?.let { it() }
    }
    if (showDivider) {
      HorizontalDivider(
        modifier = Modifier.padding(horizontal = Constants.Dimens.Large),
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.17f)
      )
    }
  }
}

@Preview(showBackground = true, apiLevel = 33)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, apiLevel = 33)
@Composable
private fun PreviewClientView() {
  PreviewComposable(fillMaxSize = false) {
    var areContactsSelectable by remember { mutableStateOf(true) }

    LazyColumn {
      items(clientsForTesting) {
        var isSelected by remember { mutableStateOf(false) }
        val isContactSelected = remember {
          derivedStateOf {
            when (areContactsSelectable) {
              false -> null
              else -> isSelected
            }
          }
        }
        Contact(
          name = it.name,
          imageUriString = it.imageUriString,
          showDivider = false,
          subtitle = it.phoneNumber,
          onSelected = {
            if (!areContactsSelectable) {
              isSelected = true
              areContactsSelectable = true
            }
          },
          onSubtitleLongClick = null,
          onClick = {
            if (areContactsSelectable) {
              isSelected = !isSelected
            }
          },
          extraContent = null,
          isSelected = isContactSelected.value,
        )
      }
    }
  }
}

