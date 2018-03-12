package com.email.scenes.emaildetail.ui

import android.support.v7.widget.RecyclerView
import com.email.db.models.FullEmail
import com.email.utils.VirtualList

/**
 * Created by sebas on 3/12/18.
 */

class FullEmailRecyclerView(
        val recyclerView: RecyclerView,
        fullEmailEventListener: FullEmailAdapter.OnFullEmailEventListener?,
        fullEmailList: VirtualList<FullEmail>) {
}
