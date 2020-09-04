package com.criptext.mail.scenes.emaildetail.ui

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.criptext.mail.IHostActivity
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.FileDetail
import com.criptext.mail.db.models.FullEmail
import com.criptext.mail.db.models.Label
import com.criptext.mail.utils.virtuallist.VirtualList

/**
 * Created by sebas on 3/12/18.
 */

class FullEmailRecyclerView(
        private val recyclerView: RecyclerView,
        fullEmailEventListener: FullEmailListAdapter.OnFullEmailEventListener?,
        private val fullEmailList: VirtualList<FullEmail>,
        fileDetailList: Map<Long, List<FileDetail>>,
        val labels: VirtualList<Label>,
        val isStarred: Boolean,
        shouldOpenExpanded: Boolean,
        val activeAccount: ActiveAccount,
        blockRemoteContentSetting: Boolean,
        isDarkTheme: Boolean) {

    val ctx: Context = recyclerView.context
    private val fullEmailListAdapter = FullEmailListAdapter(
            mContext = ctx,
            fullEmails = fullEmailList,
            fullEmailListener = fullEmailEventListener,
            fileDetails = fileDetailList,
            labels = labels,
            isStarred = isStarred,
            shouldOpenExpanded = shouldOpenExpanded,
            isDarkTheme = isDarkTheme)

    init {
        recyclerView.layoutManager = LinearLayoutManager(ctx)
        recyclerView.adapter = fullEmailListAdapter
        fullEmailListAdapter.blockRemoteContentSetting = blockRemoteContentSetting
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

    fun expandAndNotify(){
        fullEmailListAdapter.isExpanded = true
        fullEmailListAdapter.notifyDataSetChanged()
    }


    fun changeBlockremoteSetting(block: Boolean){
        fullEmailListAdapter.blockRemoteContentSetting = block
        fullEmailListAdapter.notifyDataSetChanged()
    }

    fun notifyFullEmailChanged(position: Int) {
        fullEmailListAdapter.notifyItemChanged(position)
    }

    fun notifyLabelsChanged(updatedLabels: VirtualList<Label>, updatedHasStar: Boolean) {
        fullEmailListAdapter.notifyLabelsChanged(updatedLabels, updatedHasStar)
    }
}
