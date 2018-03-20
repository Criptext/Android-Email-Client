package com.email.scenes.labelChooser

import android.support.v7.widget.RecyclerView
import com.email.androidui.labelthread.LabelThreadListView
import com.email.androidui.labelthread.LabelThreadRecyclerView
import com.email.R
import android.view.View
import com.email.scenes.labelChooser.data.LabelThread
import com.email.utils.VirtualList


/**
 * Created by sebas on 2/2/18.
 */

interface LabelChooserScene: LabelThreadListView {
    fun attachView(labelThreadEventListener: LabelThreadAdapter.OnLabelThreadEventListener)

    class LabelChooserView(private val labelChooserView: View,
                           private val labelsList: VirtualList<LabelThread>)
        : LabelChooserScene {

        private lateinit var labelThreadRecyclerView: LabelThreadRecyclerView

        var labelThreadListener: LabelThreadAdapter.OnLabelThreadEventListener? = null
            set(value) {
                labelThreadRecyclerView.setThreadListener(value)
                field = value
            }

        override fun notifyLabelThreadSetChanged() {
            labelThreadRecyclerView.notifyLabelThreadSetChanged()
        }

        override fun notifyLabelThreadRemoved(position: Int) {
            labelThreadRecyclerView.notifyLabelThreadRemoved(position)
        }

        override fun notifyLabelThreadRangeInserted(positionStart: Int, itemCount: Int) {
            labelThreadRecyclerView.notifyLabelThreadRangeInserted(positionStart, itemCount)
        }

        override fun attachView(labelThreadEventListener: LabelThreadAdapter.OnLabelThreadEventListener) {
            val recycler = labelChooserView.findViewById<RecyclerView>(R.id.label_recycler)
            labelThreadRecyclerView = LabelThreadRecyclerView(recycler,
                    labelThreadEventListener,
                    labelsList)
            this.labelThreadListener= labelThreadEventListener
        }

        override fun notifyLabelThreadChanged(position: Int) {
            labelThreadRecyclerView.notifyLabelThreadChanged(position)
        }

    }
}
