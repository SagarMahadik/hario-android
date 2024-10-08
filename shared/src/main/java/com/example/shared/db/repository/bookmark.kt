package com.example.shared.db.repository

import com.example.shared.db.daos.BookmarkDao
import com.example.shared.db.daos.PartialBookmarkEntity
import com.example.shared.db.entity.BookmarkEntity
import com.example.shared.model.Bookmarks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.shared.db.StringListConverter
import com.example.shared.db.refs.BookmarkTagRef

class BookmarkRepository(private val bookmarkDao: BookmarkDao) {
    suspend fun getAllBookmarks(): List<Bookmarks> {
        return bookmarkDao.getAllBookmarks().map { it.toBookmark() }
    }

    fun getAllBookmarksFlow(): Flow<List<Bookmarks>> {
        return bookmarkDao.getAllBookmarksFlow().map { entities ->
            entities.map { it.toBookmark() }
        }
    }

    suspend fun insertBookmarks(bookmarks: List<Bookmarks>) {
        bookmarkDao.insertBookmarks(bookmarks.map { it.toBookmarkEntity() })
        bookmarks.forEach { bookmark ->
            val tagRefs = bookmark.tags.map { BookmarkTagRef(bookmark._id, it) }
            bookmarkDao.insertBookmarkTagRefs(tagRefs)
        }
    }

    private fun BookmarkEntity.toBookmark(): Bookmarks {
        return Bookmarks(_id, title, url, isFavorite, StringListConverter().fromString(tags))
    }

    private fun Bookmarks.toBookmarkEntity(): BookmarkEntity {
        return BookmarkEntity(_id, title, url, isFavorite, StringListConverter().fromList(tags))
    }

    suspend fun updateBookmarkById(id: String, updatedBookmark: PartialBookmarkEntity) {
        val existingBookmark = bookmarkDao.getBookmarkById(id)
        if (existingBookmark != null) {
            val updatedEntity = existingBookmark.copy(
                title = updatedBookmark.title ?: existingBookmark.title,
                url = updatedBookmark.url ?: existingBookmark.url,
                isFavorite = updatedBookmark.isFavorite ?: existingBookmark.isFavorite
            )
            bookmarkDao.updateBookmark(updatedEntity)
        } else {
            // Handle the case where the bookmark doesn't exist
            throw NoSuchElementException("Bookmark with id $id not found")
        }
    }
}