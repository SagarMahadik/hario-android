package com.example.shared.db.repository

import com.example.shared.db.daos.TagDao
import com.example.shared.db.entity.TagEntity
import com.example.shared.model.Tag  // This assumes you have a domain model called Tags
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date

class TagRepository(private val TagDao: TagDao) {
    suspend fun getAllTags(): List<Tag> {
        return TagDao.getAllTags().map { it.toTag() }
    }

    fun getAllTagsFlow(): Flow<List<Tag>> {
        return TagDao.getAllTagsFlow().map { entities ->
            entities.map { it.toTag() }
        }
    }

    suspend fun insertTags(Tags: List<Tag>) {
        TagDao.insertTags(Tags.map { it.toTagEntity() })
    }

    private fun TagEntity.toTag(): Tag {
        return Tag(_id, name, parent, updatedAt, userId, isFavorite)
    }

    private fun Tag.toTagEntity(): TagEntity {
        return TagEntity(_id, isFavorite, name, parent, updatedAt, userId)
    }

    suspend fun updateTagById(id: String, updatedTag: PartialTagEntity) {
        val existingTag = TagDao.getTagById(id)
        if (existingTag != null) {
            val updatedEntity = existingTag.copy(
                isFavorite = updatedTag.isFavorite ?: existingTag.isFavorite,
                name = updatedTag.name ?: existingTag.name,
                parent = updatedTag.parent ?: existingTag.parent,
                updatedAt = updatedTag.updatedAt ?: existingTag.updatedAt,
                userId = updatedTag.userId ?: existingTag.userId
            )
            TagDao.updateTag(updatedEntity)
        } else {
            // Handle the case where the Tag doesn't exist
            throw NoSuchElementException("Tags with id $id not found")
        }
    }
}

data class PartialTagEntity(
    val _id: String,
    val isFavorite: Boolean? = null,
    val name: String? = null,
    val parent: String? = null,
    val updatedAt: Date? = null,
    val userId: String? = null
)
