package com.example.shared.db

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

object RealmDbInitializer {
    private lateinit var realm: Realm

    fun initialize() {
        val config = RealmConfiguration.Builder(
            schema = setOf(
                SettingsRealm::class,
                UserRealm::class,
                DockRealm::class,
                SidebarRealm::class,
                SyncRealm::class
            )
        ).schemaVersion(2).build()
        realm = Realm.open(config)
    }

    fun getRealm(): Realm {
        if (!::realm.isInitialized) {
            initialize()
        }
        return realm
    }
}

class SettingsRealm : RealmObject {
    @PrimaryKey
    var _id: String = ""
    var dock: DockRealm? = null
    var sidebar: SidebarRealm? = null
    var userId: String = ""
}

class DockRealm : RealmObject {
    var size: Float = 1.0f
}

class SidebarRealm : RealmObject {
    var position: String = "left_pinned"
    var size: Float = 0.7f
}

class UserRealm : RealmObject {
    @PrimaryKey
    var _id: String = ""
    var email: String = ""
    var userId: String = ""
}

class SyncRealm : RealmObject {
    @PrimaryKey
    var _id: String = ""
    var syncId: Float = 0f
}