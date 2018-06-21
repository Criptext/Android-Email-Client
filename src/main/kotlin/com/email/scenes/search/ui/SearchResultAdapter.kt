package com.email.scenes.search.ui

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.email.R
import com.email.scenes.search.VirtualSearchResultList
import com.email.scenes.search.data.SearchItem
import com.email.scenes.search.holders.SearchHolder
import com.email.utils.virtuallist.VirtualListAdapter

/**
 * Created by danieltigse on 2/5/18.
 */

class SearchResultAdapter(private val searchResultList: VirtualSearchResultList,
                          private val searchListener: OnSearchEventListener)
    :VirtualListAdapter(searchResultList) {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when(holder) {
            is SearchHolder -> {
                val result = searchResultList[position]
                holder.bindWithSearch(result)
                holder.rootView.setOnClickListener {
                    searchListener.onSearchSelected(result)
                }
            }
        }
    }

    override fun onCreateActualViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.search_item, parent, false)
        return SearchHolder(inflatedView)
    }

    override fun getActualItemViewType(position: Int): Int {
        return 1
    }

    override fun onApproachingEnd() {
        searchListener.onApproachingEnd()
    }

    override fun getActualItemId(position: Int): Long {
        return searchResultList[position].id.toLong()
    }

    interface OnSearchEventListener {
        fun onSearchSelected(searchItem: SearchItem)
        fun onApproachingEnd()
    }

}