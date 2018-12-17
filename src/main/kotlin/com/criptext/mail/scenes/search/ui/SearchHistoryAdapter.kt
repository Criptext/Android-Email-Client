package com.criptext.mail.scenes.search.ui

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.criptext.mail.R
import com.criptext.mail.scenes.search.VirtualSearchHistoryList
import com.criptext.mail.scenes.search.holders.SearchHolder
import com.criptext.mail.utils.ui.EmptyViewHolder
import com.criptext.mail.utils.virtuallist.VirtualListAdapter

/**
 * Created by danieltigse on 2/5/18.
 */

class SearchHistoryAdapter(private val searchHistoryList: VirtualSearchHistoryList,
                           private val searchListener: OnSearchEventListener)
    :VirtualListAdapter(searchHistoryList) {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is SearchHolder -> {
                val result = searchHistoryList[position]
                holder.bindWithSearch(result)
                holder.rootView.setOnClickListener {
                    searchListener.onSearchSelected(result)
                }
            }
        }
    }

    override fun createEmptyViewHolder(parent: ViewGroup): EmptyViewHolder {
        val inflatedView = LayoutInflater.from(parent.context)
                                         .inflate(R.layout.layout_no_search_history, parent,
                                                 false)
        return EmptyViewHolder(inflatedView)
    }


    override fun onCreateActualViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context)
                                         .inflate(R.layout.search_history_item, parent, false)
        return SearchHolder(inflatedView)
    }

    override fun getActualItemViewType(position: Int): Int {
        return 1
    }

    override fun onApproachingEnd() {
        searchListener.onApproachingEnd()
    }

    override fun getActualItemId(position: Int): Long {
        return searchHistoryList[position].hashCode().toLong()
    }

    interface OnSearchEventListener {
        fun onSearchSelected(searchItem: String)
        fun onApproachingEnd()
    }

}