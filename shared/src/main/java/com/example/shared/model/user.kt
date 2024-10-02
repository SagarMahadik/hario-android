package com.example.shared.model

import com.example.shared.db.UserRealm
import java.util.Date

data class User(
    val _id: String,
    val email: String,
    val userId: String,
    val updatedAt: Date? = null
) : UpdatableItem<User> {

    override fun update(data: Map<String, Any>): User {
        var updated = this
        data.forEach { (key, value) ->
            updated = when (key) {
                "email" -> updated.copy(email = value as String)
                "userId" -> updated.copy(userId = value as String)
                "updatedAt" -> updated.copy(updatedAt = value as Date)
                else -> updated
            }
        }
        return updated
    }

    fun toRealmObject(): UserRealm {
        return UserRealm().apply {
            _id = this@User._id
            email = this@User.email
            userId = this@User.userId
        }
    }

    companion object {
        fun fromRealmObject(realmObject: UserRealm): User {
            return User(
                _id = realmObject._id,
                email = realmObject.email,
                userId = realmObject.userId
            )
        }
    }
}
