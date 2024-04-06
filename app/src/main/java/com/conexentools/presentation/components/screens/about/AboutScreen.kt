package com.conexentools.presentation.components.screens.about

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.conexentools.BuildConfig
import com.conexentools.R
import com.conexentools.core.app.Constants
import com.conexentools.core.util.PreviewComposable
import com.conexentools.domain.repository.AndroidUtils
import com.conexentools.presentation.components.common.ScreenSurface

@Composable
fun AboutScreen(
  onNavigateBack: () -> Unit,
  au: AndroidUtils
) {

  ScreenSurface(
    title = "Acerca de",
    onNavigateBack = onNavigateBack,
    horizontalAlignment = Alignment.CenterHorizontally,
    lazyColumnModifier = Modifier
      .fillMaxHeight()
//      .heightIn(0.dp, 232.dp) //mention max height here
//      .widthIn(0.dp, 350.dp)
//      .fillMaxWidth()
//    modifier = Modifier,
//    horizontalAlignment = ,
//    verticalArrangement = Arrangement.SpaceAround

  ) {

    Text(
      text = "${stringResource(id = R.string.app_name)} ",
      style =  MaterialTheme.typography.displayMedium
    )

    Spacer(modifier = Modifier.height(Constants.Dimens.Medium))

    Image(
      painter = painterResource(id = R.drawable.conexentools_app_icon),
      contentDescription = null,
      modifier = Modifier
        .size(250.dp)
        .clickable {
          au.openBrowser(Constants.GITHUB_REPO_URL)
        },
    )

    Spacer(modifier = Modifier.height(Constants.Dimens.Medium))

    Text(
      text = "v${BuildConfig.VERSION_NAME} Build ${BuildConfig.VERSION_CODE}\n" +
          "2024",
      textAlign = TextAlign.Center
    )
    Text(
      text = "by Odell",
      modifier = Modifier.clickable {
        au.openBrowser("https://instagram.com/odell0111")
      }
    )

    Spacer(modifier = Modifier.height(Constants.Dimens.ExtraLarge))

    Image(
      painter = painterResource(id = R.drawable.github),
      contentDescription = null,
      modifier = Modifier
        .size(100.dp)
        .clip(CircleShape)
        .clickable {
          au.openBrowser(Constants.GITHUB_REPO_URL)
        },
    )

    Text(
      text = "Código disponible en GitHub",
      modifier = Modifier.clickable {
        au.setClipboard(Constants.GITHUB_REPO_URL)
        au.toast("Link del repositorio copiado al portapapeles")
      }
    )
  }


}

@Preview(apiLevel = 33)
@Preview(apiLevel = 33, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewAboutScreen() {
  PreviewComposable {
    AboutScreen(
      onNavigateBack = {},
      au = AndroidUtils.create(),
    )
  }
}