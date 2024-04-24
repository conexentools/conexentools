package com.conexentools.presentation.components.screens.home.pages.client_list

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.conexentools.core.app.Constants
import com.conexentools.core.util.PreviewComposable
import com.conexentools.core.util.log
import com.conexentools.data.local.model.Client
import com.conexentools.data.repository.AndroidUtilsImpl
import com.conexentools.domain.repository.AndroidUtils
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import my.nanihadesuka.compose.LazyColumnScrollbar
import kotlin.random.Random

@OptIn(FlowPreview::class)
@Composable
fun ClientsListPage(
  clients: LazyPagingItems<Client>,
  searchBarText: String = "",
  onClientEdit: (Client) -> Unit,
  onClientRecharge: (Client, () -> Unit) -> Unit,
  onClientSendMessage: (String, String?) -> Unit,
  onClientDelete: (Client) -> Unit,
  onClientCardCounterReset: (Client) -> Unit,
  scrollPosition: MutableIntState,
  au: AndroidUtils
) {

  val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = scrollPosition.intValue)

  LaunchedEffect(lazyListState) {
    snapshotFlow { lazyListState.firstVisibleItemIndex }
      .debounce(500L)
      .collectLatest { pos ->
        scrollPosition.intValue = pos
        log("saving scroll position: $pos")
      }
  }

//  key(clients) {
  LazyColumnScrollbar(
    listState = lazyListState,
  ) {
    LazyColumn(
      state = lazyListState,
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.fillMaxSize()
    ) {
      items(
        count = clients.itemCount,
        key = { clients[it]?.hashCode() ?: Random.nextDouble() }
      ) { index ->
        val client = clients[index]
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

          if (isVisibleByFilters) {
            ClientCard(
              client = client,
              onEdit = onClientEdit,
              onRecharge = onClientRecharge,
              onDelete = { onClientDelete(it) },
              showDivider = index < clients.itemCount - 1,
              onSendMessage = onClientSendMessage,
              onClientCardCounterReset = onClientCardCounterReset,
              au = au
            )
          }
        }
      }

      item {
        clients.run {
          when {
            loadState.refresh is LoadState.Loading -> {
              PageLoader(
                modifier = Modifier.height(Constants.Dimens.HorizontalCardHeight)
              )
            }

            loadState.refresh is LoadState.Error -> {
              val error = clients.loadState.refresh as LoadState.Error
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
              val error = clients.loadState.append as LoadState.Error
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
      clients = MutableStateFlow(value = PagingData.from(clientsForTesting)).collectAsLazyPagingItems(),
      searchBarText = "",
      onClientEdit = {},
      onClientDelete = { },
//      paddingValues = PaddingValues(all = 0.dp),
      onClientSendMessage = { _, _ -> },
//      onAddClient = {},
      au = AndroidUtilsImpl(LocalContext.current),
      onClientRecharge = { _, _ -> },
      scrollPosition = remember { mutableIntStateOf(0) },
//      lazyListState = rememberLazyListState(),
      onClientCardCounterReset = {},
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