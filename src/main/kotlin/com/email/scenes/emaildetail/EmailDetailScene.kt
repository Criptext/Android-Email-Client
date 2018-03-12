package com.email.scenes.emaildetail

import android.view.View
import com.email.IHostActivity
import com.email.db.models.FullEmail
import com.email.scenes.emaildetail.ui.FullEmailRecyclerView
import com.email.scenes.mailbox.data.EmailThread
import com.email.utils.VirtualList

/**
 * Created by sebas on 3/12/18.
 */

interface EmailDetailScene {

    class EmailDetailSceneView(private val emailDetailView: View,
                           val hostActivity: IHostActivity,
                           val emailThread: EmailThread,
                           val fullEmailList: VirtualList<FullEmail>)
        : EmailDetailScene {

        private val context = emailDetailView.context

        private lateinit var FullEmailRecyclerView: FullEmailRecyclerView

    }


}
