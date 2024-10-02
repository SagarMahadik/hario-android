package com.example.shared.model

import com.example.shared.db.SyncRealm

data class Sync(
    val _id: String,
    val syncId:Float
) : UpdatableItem<Sync> {

    override fun update(data: Map<String, Any>): Sync {
        var updated = this
        data.forEach { (key, value) ->
            updated = when (key) {
                "syncId" -> updated.copy(syncId = value as Float)
                else -> updated
            }
        }
        return updated
    }

    fun toRealmObject(): SyncRealm {
        return SyncRealm().apply {
            _id = this@Sync._id
            syncId=this@Sync.syncId
        }
    }

    companion object {
        fun fromRealmObject(realmObject: SyncRealm): Sync {
            return Sync(
                _id = realmObject._id,
                syncId = realmObject.syncId
            )
        }
    }
}
