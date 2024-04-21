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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.conexentools.MainActivity

import com.conexentools.core.app.Constants
import com.conexentools.core.util.PreviewComposable
import com.conexentools.core.util.log
import com.conexentools.data.local.model.Client
import com.conexentools.data.repository.AndroidUtilsImpl
import com.conexentools.domain.repository.AndroidUtils
import kotlinx.coroutines.flow.flowOf
import my.nanihadesuka.compose.LazyColumnScrollbar

@Composable
fun ClientsListPage(
  clientPagingItems: LazyPagingItems<Client>,
  searchBarText: String = "",
  onClientEdit: (Client) -> Unit,
  onClientRecharge: (Client) -> Unit,
  onClientSendMessage: (String, String?) -> Unit,
  onClientDelete: (Client) -> Unit,
  onClientRechargeCounterReset: (Client) -> Unit,
  au: AndroidUtils
) {

  val listState = rememberLazyListState()

  LazyColumnScrollbar(
    listState = listState,
  ) {
    LazyColumn(
      state = listState,
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.fillMaxSize()
    ) {
      items(
        count = clientPagingItems.itemCount,
      ) { index ->

        val client: Client? = clientPagingItems[index]
        var isVisibleByFilters by remember { mutableStateOf(true) }
        if (client != null) {
          LaunchedEffect(client, searchBarText) {
            isVisibleByFilters = searchBarText.isBlank() ||
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

          if (client.visible.value && isVisibleByFilters) {
            ClientCard(
              client = client,
              onEdit = onClientEdit,
              onRecharge = onClientRecharge,
              onDelete = {
                it.visible.value = false
                onClientDelete(it)
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
                modifier = Modifier.height(Constants.Dimens.HorizontalCardHeight)
              )
            }

            loadState.refresh is LoadState.Error -> {
              val error = clientPagingItems.loadState.refresh as LoadState.Error
              ErrorMessage(
                modifier = Modifier.height(Constants.Dimens.HorizontalCardHeight),
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
}

@Preview(showBackground = true, apiLevel = 33)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, apiLevel = 33)
@Composable
private fun PreviewClientsListPage() {
  PreviewComposable {
    ClientsListPage(
      clientPagingItems = flowOf(PagingData.from(clientsForTesting)).collectAsLazyPagingItems(),
      searchBarText = "",
      onClientEdit = {},
      onClientRecharge = {},
      onClientDelete = { },
//      paddingValues = PaddingValues(all = 0.dp),
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
    latestRechargeDateISOString = "2024-04-17T16:10:49.291993300Z",
    imageUriString = null,
    quickMessage = "popop",
    rechargesMade = 45
  ),
  Client(
    id = 1,
    name = "Maria",
    phoneNumber = "55467811",
    cardNumber = "4567-8941-1234-1234",
    latestRechargeDateISOString = "2024-04-17T14:10:49.297831700Z",
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