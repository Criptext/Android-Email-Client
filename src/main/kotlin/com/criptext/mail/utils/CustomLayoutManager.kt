package com.criptext.mail.utils

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

/**
 * Created by hirobreak on 04/05/17.
 */
class CustomLayoutManager(val mContext : Context, orientation: Int, b: Boolean) : LinearLayoutManager(mContext, orientation, b) {

    override fun supportsPredictiveItemAnimations(): Boolean {
        return true
    }


}