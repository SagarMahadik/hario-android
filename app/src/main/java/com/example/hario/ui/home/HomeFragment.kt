package com.example.hario.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.shared.db.AppDatabase
import com.example.shared.db.DbManager
import com.example.shared.db.MutationPayload
import com.example.shared.db.daos.PartialBookmarkEntity
import com.example.shared.db.repository.BookmarkRepository
import com.example.shared.model.Bookmarks
import com.example.shared.state.AppStore
import com.example.shared.state.AppAction
import com.example.shared.state.ActionType
import com.example.shared.state.AppManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.reduxkotlin.StoreSubscriber

class HomeFragment : Fragment() {

    private val store = AppStore.store
    private var unsubscribe: StoreSubscriber? = null

    private lateinit var bookmarkRepository: BookmarkRepository
    private lateinit var appManager: AppManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = AppDatabase.getInstance(requireContext())
        appManager = AppManager()

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
            appManager.startUpSequence()
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
                if (appState.value.auth.loginRequired) {
                    LoginScreen(
                    )
                } else {
                    BookmarkList(
                        bookmarks = appState.value.bookmarks,
                        onAddBookmark = { /* Add Bookmark Logic */ },
                        onUpdateBookmark = { /* Update Bookmark Logic */ },
                        paddingValues = paddingValues
                    )
                }
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

    @SuppressLint("SuspiciousIndentation")
    @Composable
    fun LoginScreen(
    ) {
        // State to hold the entered email
        var email by remember { mutableStateOf("") }
        var token by remember { mutableStateOf("") }

        val appManager = AppManager()

        val coroutineScope = rememberCoroutineScope()

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Email input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = token,
                onValueChange = { token = it },
                label = { Text("Token") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Login with Email Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        // Create a LoginPayload object from the email string
                        val loginPayload = AppManager.InitiateEmailLoginPayload(email)
                        // Pass this payload to the handleAction method
                        appManager.handleAction(ActionType.initiateEmailBasedLogin, loginPayload)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = email.isNotEmpty()
            ) {
                Text(text = "Login with Email")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Login with Google Button
            Button(
                onClick = {
                    coroutineScope.launch {
                      val verifyEmailLoginPayload = AppManager.VerifyEmailLoginPayload(email, token)
                        appManager.handleAction(ActionType.verifyEmailBasedLogin, verifyEmailLoginPayload)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Validate Token")
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

        appManager.mutate(payload)
    }
}
