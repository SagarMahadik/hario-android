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
import androidx.lifecycle.lifecycleScope
import com.example.shared.db.AppDatabase
import com.example.shared.db.DbManager
import com.example.shared.db.MutationPayload
import com.example.shared.db.daos.PartialBookmarkEntity
import com.example.shared.db.repository.BookmarkRepository
import com.example.shared.model.Bookmarks
import com.example.shared.state.ActionHandler
import com.example.shared.state.AppStore
import com.example.shared.state.AppAction
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.reduxkotlin.StoreSubscriber

class HomeFragment : Fragment() {

    private val store = AppStore.store
    private var unsubscribe: StoreSubscriber? = null

    private lateinit var bookmarkRepository: BookmarkRepository
    private lateinit var actionHandler: ActionHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = AppDatabase.getInstance(requireContext())
        actionHandler = ActionHandler()

        val bookmarkDao = database.bookmarkDao()
        bookmarkRepository = BookmarkRepository(bookmarkDao)
    }


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
            observeBookmarks()
        }

        // Unsubscribe when the composable is disposed
        DisposableEffect(Unit) {
            onDispose {
                unsubscribe?.invoke()
            }
        }

        val scope = rememberCoroutineScope()

        // UI Components
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Bookmarks") })
            },
            content = { paddingValues ->
                BookmarkList(
                    bookmarks = appState.value.bookmarks,
                    onAddBookmark = { addBookmark() },
                    onUpdateBookmark = {  ->
                        scope.launch {
                            updateBookmark()
                        }
                    },
                    paddingValues =paddingValues
                )
            }
        )
    }

    @Composable
    fun BookmarkList(
        bookmarks: List<Bookmarks>,
        onAddBookmark: () -> Unit,
        onUpdateBookmark: () -> Unit,
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


    private fun loadBookmarks() {
        viewLifecycleOwner.lifecycleScope.launch {
            val bookmarks = bookmarkRepository.getAllBookmarks()
            store.dispatch(AppAction.SetBookmarks(bookmarks))
        }
    }

    private fun observeBookmarks() {
        viewLifecycleOwner.lifecycleScope.launch {
            bookmarkRepository.getAllBookmarksFlow().collectLatest { bookmarks ->
                store.dispatch(AppAction.SetBookmarks(bookmarks))
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

    private suspend fun updateBookmark() {

        val payload = MutationPayload(
            operation = "add",
            collection = "bookmarks",
            data = mapOf(
                "_id" to "12",
                "title" to "sgrmhdk12",
                "url" to "https://example.com",
                "isFavorite" to false
            )
        )

        actionHandler.mutate(payload)
    }
}
