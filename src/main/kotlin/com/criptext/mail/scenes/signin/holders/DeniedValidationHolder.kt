package com.criptext.mail.scenes.signin.holders

import android.view.View
import com.criptext.mail.R

class DeniedValidationHolder(
        val view: View
): BaseSignInHolder() {

    private val backButton: View = view.findViewById(R.id.icon_back)

    init {
        setListeners()
    }

    private fun setListeners() {
        backButton.setOnClickListener {
            uiObserver?.onBackPressed()
        }
    }
}
