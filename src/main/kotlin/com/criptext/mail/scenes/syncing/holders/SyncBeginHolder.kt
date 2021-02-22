package com.criptext.mail.scenes.syncing.holders

import android.view.View
import android.widget.Button
import com.criptext.mail.R

class SyncBeginHolder(
        val view: View
): BaseSyncingHolder() {

    val resendButton: Button = view.findViewById(R.id.resend_button)

    init {
        setListeners()
    }

    private fun setListeners() {
        resendButton.setOnClickListener {
            uiObserver?.onSubmitButtonPressed()
        }
    }
}
