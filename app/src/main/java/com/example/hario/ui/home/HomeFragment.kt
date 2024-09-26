package com.example.hario.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.example.shared.model.Bookmarks
import com.example.shared.state.ActionHandler
import com.example.shared.state.ActionType
import com.example.shared.state.AppStore
import com.example.shared.state.AppAction
import com.example.shared.state.ItemType
import org.reduxkotlin.StoreSubscriber

class HomeFragment : Fragment() {

    private val store = AppStore.store
    private var unsubscribe: StoreSubscriber? = null

    val actionHandler = ActionHandler()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize ComposeView
        val composeView = ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    HomeScreen()
                }
            }
        }
        return composeView
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun HomeScreen() {
        // Observe the state from the store
        val appState = remember { mutableStateOf(store.state) }

        // Subscribe to store updates
        // Subscribe to store updates
        LaunchedEffect(Unit) {
            unsubscribe = store.subscribe {
                appState.value = store.state
            }
        }

        // Unsubscribe when the composable is disposed
        DisposableEffect(Unit) {
            onDispose {
                unsubscribe?.invoke()
            }
        }
        // UI Components
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Bookmarks") })
            },
            content = { paddingValues ->
                BookmarkList(
                    bookmarks = appState.value.bookmarks,
                    onAddBookmark = { addBookmark() },
                    onUpdateBookmark = {updateBookmark()},
                    paddingValues=paddingValues
                )
            }
        )
    }

    @Composable
    fun BookmarkList(
        bookmarks: List<Bookmarks>,
        onAddBookmark: () -> Unit,
        onUpdateBookmark:() -> Unit,
        paddingValues: PaddingValues
    ) {
        Column(
            modifier = Modifier.padding(paddingValues)
        ) {
            // Display the list of bookmarks
            bookmarks.forEach { bookmark ->
                Text(text = "${bookmark.title}")
                Text(text = "${bookmark.isFavorite}")
            }

            // Add Bookmark Button
            Button(onClick = { onAddBookmark() }) {
                Text(text = "Add Bookmark")
            }

            // Update Bookmark Button
            Button(onClick = { onUpdateBookmark() }) {
                Text(text = "Update Bookmark")
            }
        }
    }


    private fun addBookmark() {
        // Create a new bookmark (in a real app, you'd get this data from user input)
        val newBookmark = Bookmarks(
            _id = System.currentTimeMillis().toString(),
            title = "New Bookmark",
            url = "https://www.example.com",
            isFavorite = false
        )
        // Dispatch the action to add the bookmark
        store.dispatch(AppAction.AddBookmark(newBookmark))
        store.dispatch(AppAction.UpdateBookmark(title = "Updated Bookmark", index = 1))
    }

    private fun updateBookmark() {
        // Create a new bookmark (in a real app, you'd get this data from user input)
        //store.dispatch(AppAction.UpdateBookmark(title = "Updated Bookmark", index = 1))
        store.dispatch(AppAction.UpdateItem(index = 1, itemId = "1", itemType = ItemType.BOOKMARK,  data=mapOf("title" to "sgrmhdk") ))
        val payload = mapOf(
            "index" to 1,
            "_id" to "bookmark1",
            "itemType" to ItemType.BOOKMARK,
            "value" to true
        )

        actionHandler.handleAction(ActionType.setFavorite, payload)
    }
}
