package com.example.shared.db
import com.example.shared.model.Settings
import com.example.shared.model.User
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.notifications.DeletedObject
import io.realm.kotlin.notifications.InitialObject
import io.realm.kotlin.notifications.PendingObject
import io.realm.kotlin.notifications.SingleQueryChange
import io.realm.kotlin.notifications.UpdatedObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RealmDbManager {
    private val realm = RealmDbInitializer.getRealm()

    fun saveSettings(settings: Settings) {
        realm.writeBlocking {
            copyToRealm(settings.toRealmObject(), updatePolicy = UpdatePolicy.ALL)
        }
    }

    fun getSettings(): Settings? {
        return realm.query<SettingsRealm>().first().find()?.let { Settings.fromRealmObject(it) }
    }

    fun getSettingsFlow(): Flow<Settings?> {
        return realm.query<SettingsRealm>().first().asFlow()
            .map { change: SingleQueryChange<SettingsRealm> ->
                when (change) {
                    is DeletedObject -> null
                    is InitialObject -> change.obj.let { Settings.fromRealmObject(it) }
                    is UpdatedObject -> change.obj.let { Settings.fromRealmObject(it) }
                    is PendingObject -> null // or handle pending state as needed
                }
            }
    }

    fun saveUser(user: User) {
        realm.writeBlocking {
            copyToRealm(user.toRealmObject(), updatePolicy = UpdatePolicy.ALL)
        }
    }

    fun getUserFlow(): Flow<User?> {
        return realm.query<UserRealm>().first().asFlow()
            .map { change: SingleQueryChange<UserRealm> ->
                when (change) {
                    is DeletedObject -> null
                    is InitialObject -> change.obj.let { User.fromRealmObject(it) }
                    is UpdatedObject -> change.obj.let { User.fromRealmObject(it) }
                    is PendingObject -> null // or handle pending state as needed
                }
            }
    }

    fun setSyncId(syncId: Float) {
        realm.writeBlocking {
            val existingSync = query<SyncRealm>().first().find()
            if (existingSync != null) {
                findLatest(existingSync)?.syncId = syncId
            } else {
                // If no sync document exists, create a new one
                copyToRealm(SyncRealm().apply {
                    this._id = "sync_document" // Use a fixed ID for the single document
                    this.syncId = syncId
                })
            }
        }
    }

    fun getSyncId(): Float? {
        return realm.query<SyncRealm>().first().find()?.syncId
    }
}