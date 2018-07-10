package com.email.mocks

import org.json.JSONArray

object MockSearchHistoryData {
    val sampleSearchHistoryList = mutableListOf("hi", "test", "subject", "address", "business", "video games", "monday", "june", "world cup 2018", "event at convention center")
    val sampleJSonString = JSONArray(sampleSearchHistoryList).toString()
}