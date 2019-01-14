package com.criptext.mail.scenes.emaildetail.ui.labels

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.criptext.mail.db.models.Label
import com.criptext.mail.utils.virtuallist.VirtualList

/**
 * Created by sebas on 3/14/18.
 */

class LabelsRecyclerView(
        val recyclerView: RecyclerView,
        labels: VirtualList<Label>) {

    val ctx: Context = recyclerView.context

    private val labelsListAdapter = LabelListAdapter(
            mContext = ctx,
            labels = labels)

    init {
        recyclerView.layoutManager = LinearLayoutManager(
                ctx,
                LinearLayoutManager.HORIZONTAL,
                false)
        recyclerView.adapter = labelsListAdapter
    }
}
