package com.example.shared.db.repository

import com.example.shared.db.StringListConverter
import com.example.shared.db.daos.HighlightDao
import com.example.shared.db.entity.HighlightEntity
import com.example.shared.db.refs.HighlightTagRef
import com.example.shared.model.Highlight

class HighlightRepository(private val highlightDao: HighlightDao) {
    suspend fun insertHighlights(highlights: List<Highlight>) {
        highlightDao.insertHighlights(highlights.map { it.toHighlightEntity() })
        highlights.forEach { highlight ->
            val tagRefs = highlight.tags.map { HighlightTagRef(highlight._id, it) }
            highlightDao.insertHighlightTagRefs(tagRefs)
        }
    }

    private fun HighlightEntity.toHighlight(): Highlight {
        return Highlight(_id,bookmarkId, isFavorite, isSticky, color, StringListConverter().fromString(tags))
    }

    private fun Highlight.toHighlightEntity(): HighlightEntity {
        return HighlightEntity(_id, bookmarkId, isFavorite, color, isSticky, StringListConverter().fromList(tags))
    }
}
