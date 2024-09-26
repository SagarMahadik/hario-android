package com.example.shared.model

interface UpdatableItem<T> {
    fun update(data: Map<String, Any>): T
}
