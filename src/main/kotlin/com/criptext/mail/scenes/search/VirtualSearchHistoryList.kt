package com.criptext.mail.scenes.search

import com.criptext.mail.utils.virtuallist.VirtualList

class VirtualSearchHistoryList(private val model: SearchSceneModel, private val manager: SearchHistoryManager)
    :VirtualList<String>{

    override fun get(i: Int) = manager.getSearchHistoryItem(i)

    override val size: Int
        get() = manager.historySize

    override val hasReachedEnd: Boolean
        get() = model.hasReachedEnd

}