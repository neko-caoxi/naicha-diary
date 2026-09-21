package com.naicha.diary.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import java.io.File

class DrinkRepository(context: Context) {

    private val file = File(context.filesDir, "drinks.json")
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val _items = MutableStateFlow<List<Drink>>(emptyList())
    val items: StateFlow<List<Drink>> = _items.asStateFlow()

    init {
        _items.value = read()
    }

    private fun read(): List<Drink> = runCatching {
        if (!file.exists()) return emptyList()
        json.decodeFromString<List<Drink>>(file.readText())
    }.getOrDefault(emptyList())

    private fun persist(list: List<Drink>) {
        _items.value = list
        runCatching { file.writeText(json.encodeToString(list)) }
    }

    fun add(item: Drink) = persist(_items.value + item)

    fun update(item: Drink) =
        persist(_items.value.map { if (it.id == item.id) item else it })

    fun remove(id: String) = persist(_items.value.filterNot { it.id == id })

    fun clearAll() = persist(emptyList())

    fun sorted(): List<Drink> = _items.value.sortedByDescending { it.timestamp }
}
