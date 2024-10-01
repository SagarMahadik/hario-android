package com.example.shared.db.repository

import com.example.shared.db.daos.CollectionDao
import com.example.shared.db.entity.CollectionEntity
import com.example.shared.model.Collection  // This assumes you have a domain model called Collections
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.sql.Date

class CollectionRepository(private val collectionDao: CollectionDao) {
    suspend fun insertCollections(collections: List<Collection>) {
        collectionDao.insertCollections(collections.map { it.toCollectionEntity() })
    }

    suspend fun getAllCollections(): List<Collection> {
        return collectionDao.getAllCollections().map { it.toCollection() }
    }

    fun getAllCollectionsFlow(): Flow<List<Collection>> {
        return collectionDao.getAllCollectionsFlow().map { entities ->
            entities.map { it.toCollection() }
        }
    }

    private fun CollectionEntity.toCollection(): Collection {
        return Collection(_id, name, parent, updatedAt, userId, isFavorite)
    }

    private fun Collection.toCollectionEntity(): CollectionEntity {
        return CollectionEntity(_id, isFavorite, name, parent, updatedAt, userId)
    }

    suspend fun updateCollectionById(id: String, updatedCollection: PartialCollectionEntity) {
        val existingCollection = collectionDao.getCollectionById(id)
        if (existingCollection != null) {
            val updatedEntity = existingCollection.copy(
                isFavorite = updatedCollection.isFavorite ?: existingCollection.isFavorite,
                name = updatedCollection.name ?: existingCollection.name,
                parent = updatedCollection.parent ?: existingCollection.parent,
                updatedAt = updatedCollection.updatedAt ?: existingCollection.updatedAt,
                userId = updatedCollection.userId ?: existingCollection.userId
            )
            collectionDao.updateCollection(updatedEntity)
        } else {
            // Handle the case where the collection doesn't exist
            throw NoSuchElementException("Collections with id $id not found")
        }
    }
}

data class PartialCollectionEntity(
    val _id: String?,
    val isFavorite: Boolean? = null,
    val name: String? = null,
    val parent: String? = null,
    val updatedAt: Date? = null,
    val userId: String? = null
)

fun Map<String, Any>.toPartialCollectionEntity(): PartialCollectionEntity {
    return PartialCollectionEntity(
        _id = this["_id"] as String,
        isFavorite = this["isFavorite"] as? Boolean,
        name = this["name"] as? String,
        parent = this["parent"] as? String,
        updatedAt = this["updatedAt"] as? Date,
        userId = this["userId"] as? String
    )
}