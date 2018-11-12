package com.criptext.mail.scenes.emaildetail.ui

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.criptext.mail.db.models.FileDetail
import com.criptext.mail.db.models.FullEmail
import com.criptext.mail.db.models.Label
import com.criptext.mail.utils.virtuallist.VirtualList

/**
 * Created by sebas on 3/12/18.
 */

class FullEmailRecyclerView(
        val recyclerView: RecyclerView,
        fullEmailEventListener: FullEmailListAdapter.OnFullEmailEventListener?,
        val fullEmailList: VirtualList<FullEmail>,
        val fileDetailList: Map<Long, List<FileDetail>>,
        val labels: VirtualList<Label>,
        val isStarred: Boolean) {

    val ctx: Context = recyclerView.context
    private val fullEmailListAdapter = FullEmailListAdapter(
            mContext = ctx,
            fullEmails = fullEmailList,
            fullEmailListener = fullEmailEventListener,
            fileDetails = fileDetailList,
            labels = labels,
            isStarred = isStarred)

    init {
        recyclerView.layoutManager = LinearLayoutManager(ctx)
        recyclerView.adapter = fullEmailListAdapter
    }

    fun updateAndNotify(fullEmailEventListener: FullEmailListAdapter.OnFullEmailEventListener?,
                        fileDetailList: Map<Long, List<FileDetail>>,
                        labels: VirtualList<Label>,
                        isStarred: Boolean){

    }

    fun notifyFullEmailListChanged() {
        fullEmailListAdapter.notifyDataSetChanged()
    }

    fun scrollTo(position: Int){
        recyclerView.scrollToPosition(position - 1)
    }

    fun scrollToLast() {
        recyclerView.scrollToPosition(fullEmailList.size - 1)
    }

    fun notifyFullEmailRemoved(position: Int) {
        fullEmailListAdapter.notifyItemRemoved(position)
    }

    fun notifyFullEmailChanged(position: Int) {
        fullEmailListAdapter.notifyItemChanged(position)
    }

    fun notifyLabelsChanged(updatedLabels: VirtualList<Label>, updatedHasStar: Boolean) {
        fullEmailListAdapter.notifyLabelsChanged(updatedLabels, updatedHasStar)
    }
}
