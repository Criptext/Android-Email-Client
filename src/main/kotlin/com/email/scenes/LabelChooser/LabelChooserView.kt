package com.email.scenes.LabelChooser

import android.support.v7.widget.RecyclerView
import com.email.androidui.labelthread.LabelThreadListView
import com.email.androidui.labelthread.LabelThreadRecyclerView
import com.email.IHostActivity
import com.email.R
import android.view.View


/**
 * Created by sebas on 2/2/18.
 */

interface LabelChooserScene: LabelThreadListView {
    fun getIHostActivity(): IHostActivity
    fun attachView(labelThreadEventListener: LabelThreadAdapter.OnLabelThreadEventListener)

    class LabelChooserView(val hostActivity: IHostActivity,
                           val labelChooserView: View,
                           val threadListHandler: LabelChooserDialog.LabelThreadListHandler)
        : LabelChooserScene {

        override fun getIHostActivity(): IHostActivity {
            return hostActivity
        }


        private val recyclerView: RecyclerView by lazy {
            labelChooserView.findViewById<RecyclerView>(R.id.label_recycler)
        }

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
            labelThreadRecyclerView = LabelThreadRecyclerView(recyclerView,
                    labelThreadEventListener,
                    threadListHandler)
            this.labelThreadListener= labelThreadEventListener
        }

        override fun notifyLabelThreadChanged(position: Int) {
            labelThreadRecyclerView.notifyLabelThreadChanged(position)
        }

    }
}
