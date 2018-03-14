package com.email.scenes.emaildetail.ui.labels

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.email.db.models.Label
import com.email.utils.VirtualList

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
