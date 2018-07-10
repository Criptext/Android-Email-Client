package com.email.scenes.search

import com.email.db.KeyValueStorage
import com.email.scenes.search.data.SearchItem


class SearchHistoryManager(private val storage: KeyValueStorage) {

    fun saveSearchHistory(value: String) {

        val searchHistory = getSearchHistory().toMutableList()
        if(searchHistory.size == SearchSceneController.MAXIMUM_SEARCH_HISTORY){
            searchHistory.removeAt(searchHistory.size - 1)
        }
        val newSearchItem = SearchItem(searchHistory.size, value, "")
        newSearchItem.timestampCreated = System.currentTimeMillis()
        searchHistory.add(newSearchItem)

        storage.putStringSet(KeyValueStorage.StringKey.SearchHistory,
                searchHistory.map { "${it.subject}${SearchSceneController.SEPARATOR}${it.timestampCreated}" }.toMutableSet())

    }

    fun getSearchHistory(): List<SearchItem> {

        val setHistory = storage.getStringSet(KeyValueStorage.StringKey.SearchHistory) ?: return listOf()
        val searchHistory =  setHistory.mapIndexed { index, s ->
            val searchItem = SearchItem(index, s.split(SearchSceneController.SEPARATOR)[0], "")
            searchItem.timestampCreated = s.split(SearchSceneController.SEPARATOR)[1].toLong()
            searchItem
        }
        return searchHistory.sortedWith(Comparator { p0, p1 ->
            when {
                p0.timestampCreated!! > p1.timestampCreated!! -> -1
                p0.timestampCreated!! == p1.timestampCreated!! -> 0
                else -> 1
            }
        })
    }

}