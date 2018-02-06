package com.email.scenes.search.data

import com.email.DB.SearchLocalDB

/**
 * Created by danieltigse on 2/5/18.
 */

class SearchDataSource(private val searchLocalDB: SearchLocalDB){

    fun saveHistorySearch(value: String){
        searchLocalDB.saveHistorySearch(value)
    }

    fun getHistorySearch(): List<SearchResult>{
        return searchLocalDB.getHistorySearch().map { SearchResult(it, "") }
    }

    fun seed(){
        searchLocalDB.seed()
    }

}
