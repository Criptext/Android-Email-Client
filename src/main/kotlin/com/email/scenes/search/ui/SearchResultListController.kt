package com.email.scenes.search.ui

import com.email.scenes.search.data.SearchResult

/**
 * Created by danieltigse on 02/14/18.
 */

class SearchResultListController(private var searchResults: ArrayList<SearchResult>) {

    fun setSearchList(searchResults: List<SearchResult>) {
        this.searchResults.clear()
        this.searchResults.addAll(searchResults)
    }

}
