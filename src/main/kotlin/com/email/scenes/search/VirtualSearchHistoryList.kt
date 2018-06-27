package com.email.scenes.search

import com.email.scenes.search.data.SearchItem
import com.email.utils.virtuallist.VirtualList

class VirtualSearchHistoryList(private val model: SearchSceneModel)
    :VirtualList<SearchItem>{

    override fun get(i: Int) = model.searchItems[i]

    override val size: Int
        get() = model.searchItems.size

    override val hasReachedEnd: Boolean
        get() = model.hasReachedEnd

}