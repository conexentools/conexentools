package com.conexentools.presentation.components.screens.about

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.conexentools.BuildConfig
import com.conexentools.R
import com.conexentools.core.app.Constants
import com.conexentools.core.util.PreviewComposable
import com.conexentools.domain.repository.AndroidUtils
import com.conexentools.presentation.components.common.ScreenSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
  onNavigateBack: () -> Unit,
  au: AndroidUtils
) {

  ScreenSurface(
    title = "Acerca de",
    lazyColumnModifier = Modifier.fillMaxHeight(),
    bottomContent = {
      Row(
        modifier = Modifier
          .height(110.dp)
          .fillMaxWidth()
          .padding(vertical = Constants.Dimens.ExtraLarge),
        horizontalArrangement = Arrangement.Center
      ) {
        Image(
          painter = painterResource(id = R.drawable.github),
          contentDescription = null,
          modifier = Modifier.clickable {
            au.openBrowser(Constants.CONEXEN_GITHUB_REPO_URL)
          },
        )

        Spacer(modifier = Modifier.width(Constants.Dimens.ExtraLarge))

        Image(
          painter = painterResource(id = R.drawable.gmail),
          contentDescription = null,
          modifier = Modifier.clickable {
            au.composeEmail(Constants.CONEXEN_GOOGLE_MAIL)
          },
        )
      }
    },
    onNavigateBack = onNavigateBack,
  ) {

    Text(
      text = "${stringResource(id = R.string.app_name)} ",
      style = MaterialTheme.typography.displayMedium
    )

    Spacer(modifier = Modifier.height(Constants.Dimens.Medium))

    Image(
      painter = painterResource(id = R.drawable.conexentools_app_icon),
      contentDescription = null,
      modifier = Modifier
        .size(250.dp)
        .clickable {
          au.openBrowser(Constants.CONEXEN_GITHUB_REPO_URL)
        },
    )

    Spacer(modifier = Modifier.height(Constants.Dimens.Medium))

    Text(
      text = "v${BuildConfig.VERSION_NAME} Build ${BuildConfig.VERSION_CODE}\n" +
          Constants.DEVELOPMENT_DATE,
      textAlign = TextAlign.Center
    )
    Text(
      text = "by Odell",
      modifier = Modifier.clickable {
        au.openBrowser("https://instagram.com/odell0111")
      }
    )

    Spacer(modifier = Modifier.height(Constants.Dimens.ExtraLarge))
  }
}

@Preview(apiLevel = 33)
@Preview(apiLevel = 33, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewAboutScreen() {
  PreviewComposable {
    AboutScreen(
      onNavigateBack = {},
      au = AndroidUtils.create(),
    )
  }
}