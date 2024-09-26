package com.example.shared.state

import org.reduxkotlin.Store
import org.reduxkotlin.threadsafe.createThreadSafeStore

object AppStore {
    val store: Store<AppState> = createThreadSafeStore(
        reducer = appReducer,
        preloadedState = AppState()
    )
}