package com.conexentools.presentation.components.screens.home.pages.client_list

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems

import com.conexentools.core.app.Constants
import com.conexentools.core.util.PreviewComposable
import com.conexentools.data.local.model.Client
import com.conexentools.data.repository.AndroidUtilsImpl
import com.conexentools.domain.repository.AndroidUtils
import kotlinx.coroutines.flow.flowOf


@Composable
fun ClientsListPage(
  clientPagingItems: LazyPagingItems<Client>,
  searchBarText: String = "",
  onClientEdit: (Client) -> Unit,
  onClientRecharge: (Client) -> Unit,
  onClientSendMessage: (String, String?) -> Unit,
  onClientDelete: (Client, () -> Unit) -> Unit,
  onClientRechargeCounterReset: (Client) -> Unit,
  paddingValues: PaddingValues,
  au: AndroidUtils
) {

  val listState = rememberLazyListState()
//    LazyColumnScrollbar(listState) {
//
//  }
//  LazyColumnScrollbar()

  LazyColumn(
    state = listState,
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .fillMaxSize()
      .padding(paddingValues)
      .animateContentSize()
  ) {
    items(
      count = clientPagingItems.itemCount,
      key = { clientPagingItems[it]?.hashCode() ?: -1 }
//      key = { clientPagingItems.itemKey { it.hashCode() } } // -> throws java.lang.IllegalArgumentException: Type of the key Function1<java.lang.Integer, java.lang.Object> is not supported. On Android you can only use types which can be stored inside the Bundle.
    ) { index ->

      val client = clientPagingItems[index]
      var showClient by remember(client, searchBarText) {
        mutableStateOf(
          if (client == null)
            false
          else {
            searchBarText.isBlank() ||
                client.name.contains(searchBarText, ignoreCase = true) ||
                client.cardNumber != null && client.cardNumber!!.contains(
              searchBarText,
              ignoreCase = true
            ) ||
                client.phoneNumber != null && client.phoneNumber!!.contains(
              searchBarText,
              ignoreCase = true
            )
          }
        )
      }

      if (client != null) {
        AnimatedVisibility(
          visible = showClient,
          enter = fadeIn(animationSpec = tween(durationMillis = 200)) + expandVertically(
            animationSpec = tween(durationMillis = 200)
          ),
          exit = fadeOut(animationSpec = tween(durationMillis = 200)) + shrinkVertically(
            animationSpec = tween(durationMillis = 200)
          )
        ) {
          ClientCard(
            client = client,
            onEdit = onClientEdit,
            onRecharge = onClientRecharge,
            onDelete = {
              showClient = false
              onClientDelete(it) { showClient = true }
            },
            showDivider = index < clientPagingItems.itemCount - 1,
            onSendMessage = onClientSendMessage,
            onClientRechargeCounterReset = onClientRechargeCounterReset,
            au = au
          )
        }
      }
    }

    item {
      clientPagingItems.run {
        when {
          loadState.refresh is LoadState.Loading -> {
            PageLoader(
              modifier = Modifier.height(Constants.Dimens.ClientCardHeight)
            )
          }

          loadState.refresh is LoadState.Error -> {
            val error = clientPagingItems.loadState.refresh as LoadState.Error
            ErrorMessage(
              modifier = Modifier.height(Constants.Dimens.ClientCardHeight),//fillParentMaxSize(),
              message = error.error.localizedMessage!!,
              onClickRetry = { retry() }
            )
          }

          loadState.append is LoadState.Loading -> {
            LoadingNextPageItem(modifier = Modifier)
          }

          loadState.append is LoadState.Error -> {
            val error = clientPagingItems.loadState.append as LoadState.Error
            ErrorMessage(
              modifier = Modifier,
              message = error.error.localizedMessage!!,
              onClickRetry = { retry() }
            )
          }
        }
      }
    }
  }
}

@Preview(showBackground = true, apiLevel = 33)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, apiLevel = 33)
@Composable
fun PreviewClientsListPage() {
  PreviewComposable {
    ClientsListPage(
      clientPagingItems = flowOf(PagingData.from(clientsForTesting)).collectAsLazyPagingItems(),
      searchBarText = "",
      onClientEdit = {},
      onClientRecharge = {},
      onClientDelete = { _, _ -> },
      paddingValues = PaddingValues(all = 0.dp),
      onClientSendMessage = { _, _ -> },
      onClientRechargeCounterReset = {},
//      onAddClient = {},
      au = AndroidUtilsImpl(LocalContext.current),
    )
  }
}

val clientsForTesting = listOf(
  Client(
    id = 0,
    name = "Perica sdfsfsdfsdfsdfsdfsdfsdfsdfsdfffffffggggggsdf",
    phoneNumber = "55467811",
    cardNumber = "4567894112341234",
    latestRechargeDateISOString = "2024-04-02T06:52:40.145175100Z",//"2024-03-14T03:18:39.713498400Z",
    imageUriString = null,
    quickMessage = "popop",
    rechargesMade = 45
  ),
  Client(
    id = 1,
    name = "Maria",
    phoneNumber = "55467811",
    cardNumber = "4567-8941-1234-1234",
    latestRechargeDateISOString = "2024-03-26T06:53:46.836478800Z",
    imageUriString = null,
    quickMessage = "erer",
    rechargesMade = 33
  ),
  Client(
    id = 2,
    name = "Luisa",
    phoneNumber = "55467811",
    cardNumber = "4567-8941-1234-1234",
    latestRechargeDateISOString = "2024-03-25T16:53:46.836478800Z",
    imageUriString = null,
    quickMessage = "erer",
    rechargesMade = 45
  )
)