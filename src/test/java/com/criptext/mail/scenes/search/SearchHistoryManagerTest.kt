package com.criptext.mail.scenes.search

import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.mocks.MockSearchHistoryData
import io.mockk.*
import org.amshove.kluent.`should equal`
import org.json.JSONArray
import org.junit.Before
import org.junit.Test


class SearchHistoryManagerTest {

    private lateinit var storage: KeyValueStorage
    private lateinit var manager: SearchHistoryManager

    @Before
    open fun setUp(){
        storage = mockk()
        manager = spyk(SearchHistoryManager(storage))
    }

    @Test
    fun `load search history info from storage should return a list of strings with all the data saved in storage`() {

        every { storage.getString(KeyValueStorage.StringKey.SearchHistory, "")
                } returns MockSearchHistoryData.sampleJSonString

        manager.getSearchHistory() `should equal` MockSearchHistoryData.sampleSearchHistoryList
    }

    @Test
    fun `save searched text in storage when search history is not full`() {

        every { storage.getString(KeyValueStorage.StringKey.SearchHistory, "") } returns ""

        val expectedList = mutableListOf("UnitTest")

        every {
            storage.putString(KeyValueStorage.StringKey.SearchHistory,
                    JSONArray(expectedList).toString())
        } just runs
        manager.saveSearchHistory("UnitTest")

        manager.getSearchHistory() `should equal` expectedList
    }

    @Test
    fun `save searched text in storage when search history is full`() {

        every { storage.getString(KeyValueStorage.StringKey.SearchHistory, "")
            } returns MockSearchHistoryData.sampleJSonString


        val expectedList = MockSearchHistoryData.sampleSearchHistoryList.toMutableList()
        expectedList.removeAt(expectedList.size - 1)
        expectedList.add(0, "UnitTest")


        every {
            storage.putString(KeyValueStorage.StringKey.SearchHistory,
                    JSONArray(expectedList).toString())
        } just runs
        manager.saveSearchHistory("UnitTest")

        manager.getSearchHistory() `should equal`  expectedList
    }

    @Test
    fun `SearchHistoryManager should not store duplicated values`() {

        every { storage.getString(KeyValueStorage.StringKey.SearchHistory, "") } returns ""

        val expectedList = mutableListOf("UnitTest")

        every {
            storage.putString(KeyValueStorage.StringKey.SearchHistory,
                    JSONArray(expectedList).toString())
        } just runs

        manager.saveSearchHistory("UnitTest")
        manager.saveSearchHistory("UnitTest")

        manager.getSearchHistory() `should equal` expectedList
    }
}