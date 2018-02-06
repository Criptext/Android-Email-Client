package com.email.scenes.LabelChooser

import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatAutoCompleteTextView
import android.support.v7.widget.RecyclerView
import com.email.androidui.labelthread.LabelThreadListView
import com.email.androidui.labelthread.LabelThreadRecyclerView
import android.view.ViewGroup
import com.email.IHostActivity
import com.email.MailboxActivity


/**
 * Created by sebas on 2/2/18.
 */

interface LabelChooserScene: LabelThreadListView {
    fun attachView(labelThreadEventListener: LabelThreadAdapter.OnLabelThreadEventListener)

    class LabelChooserView(private val sceneContainer: ViewGroup,
                           val hostActivity: IHostActivity,
                           val threadListHandler: MailboxActivity.LabelThreadListHandler)
        : LabelChooserScene {

        var labelChooserDialog : DialogLabelsChooser = DialogLabelsChooser()
        lateinit var recyclerView: RecyclerView

        private lateinit var labelThreadRecyclerView: LabelThreadRecyclerView

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
            bindLabelChooserDialog(labelThreadEventListener)
            labelChooserDialog.show((hostActivity as AppCompatActivity).supportFragmentManager, "")
        }

        override fun notifyLabelThreadChanged(position: Int) {
            labelThreadRecyclerView.notifyLabelThreadChanged(position)
        }

        fun bindLabelChooserDialog(labelThreadEventListener: LabelThreadAdapter.OnLabelThreadEventListener) {
            labelChooserDialog.labelChooserView = this
            labelChooserDialog.labelThreadEventListener = labelThreadEventListener
            labelChooserDialog.dialogLabelsListener = (hostActivity as MailboxActivity)
        }

        fun initRecyclerView(labelThreadEventListener: LabelThreadAdapter.OnLabelThreadEventListener) {
            recyclerView = labelChooserDialog.recyclerView
            labelThreadRecyclerView = LabelThreadRecyclerView(recyclerView, labelThreadEventListener, threadListHandler)
        }
    }
}
