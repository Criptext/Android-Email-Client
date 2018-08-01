package com.criptext.mail.scenes.search

import com.criptext.mail.db.KeyValueStorage
import org.json.JSONArray



class SearchHistoryManager(private val storage: KeyValueStorage) {

    private val historyList by lazy { getHistoryFromStorage().toMutableList() }

    val historySize get() = historyList.size

    fun saveSearchHistory(value: String) {
        if(historyList.size == SearchSceneController.MAXIMUM_SEARCH_HISTORY){
            historyList.removeAt(historyList.size - 1)
        }
        if(!historyList.contains(value)) {
            historyList.add(0, value)
            storage.putString(KeyValueStorage.StringKey.SearchHistory, JSONArray(historyList).toString())
        }
    }

    fun getSearchHistory(): List<String> {
        return historyList
    }

    fun getSearchHistoryItem(index: Int): String {
        return historyList[index]
    }

    private fun getHistoryFromStorage(): List<String>{
        val storageString = storage.getString(KeyValueStorage.StringKey.SearchHistory, "")
        val list = mutableListOf<String>()
        if(!storageString.isEmpty()) {
            val storageHistory = JSONArray(storageString)
            for (i in 0..(storageHistory.length() - 1)) {
                val item = storageHistory[i]
                list.add(item.toString())
            }
        }
        return list
    }
}