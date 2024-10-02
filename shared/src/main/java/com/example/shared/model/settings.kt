package com.example.shared.model

import com.example.shared.db.SettingsRealm
import com.example.shared.db.DockRealm
import com.example.shared.db.SidebarRealm
import java.util.Date

data class Settings(
    val _id: String,
    val dock: Dock,
    val sidebar: Sidebar,
    val userId: String,
    val updatedAt: Date? = null
) : UpdatableItem<Settings> {
    data class Dock(val size: Float)
    data class Sidebar(val position: String, val size: Float)

    override fun update(data: Map<String, Any>): Settings {
        var updated = this
        data.forEach { (key, value) ->
            updated = when (key) {
                "dock" -> updated.copy(dock = (value as Map<String, Any>).let { Dock(it["size"] as Float) })
                "sidebar" -> updated.copy(sidebar = (value as Map<String, Any>).let { Sidebar(it["position"] as String, it["size"] as Float) })
                "userId" -> updated.copy(userId = value as String)
                "updatedAt" -> updated.copy(updatedAt = value as Date)
                else -> updated
            }
        }
        return updated
    }

    fun toRealmObject(): SettingsRealm {
        return SettingsRealm().apply {
            _id = this@Settings._id
            dock = DockRealm().apply {
                size = this@Settings.dock.size
            }
            sidebar = SidebarRealm().apply {
                position = this@Settings.sidebar.position
                size = this@Settings.sidebar.size
            }
            userId = this@Settings.userId
        }
    }

    companion object {
        fun fromRealmObject(realmObject: SettingsRealm): Settings {
            return Settings(
                _id = realmObject._id,
                dock = realmObject.dock?.let { Dock(it.size) } ?: Dock(1.0f),
                sidebar = realmObject.sidebar?.let { Sidebar(it.position, it.size) } ?: Sidebar("left_pinned", 0.7f),
                userId = realmObject.userId
            )
        }
    }
}