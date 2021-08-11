package com.criptext.mail.scenes.signin.holders

import android.view.View
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.criptext.mail.BuildConfig
import com.criptext.mail.R
import com.criptext.mail.utils.compat.ViewAnimationUtilsCompat
import com.criptext.mail.validation.ProgressButtonState

/**
 * Created by sebas on 3/2/18.
 */

class SignInStartHolder(
        val view: View,
        firstTime: Boolean,
        isMultipleAccountLogin: Boolean): BaseSignInHolder() {

    private val rootLayout: View = view.findViewById<View>(R.id.viewRoot)
    private val signInButton : Button = view.findViewById(R.id.signin_button)
    private val progressBar: ProgressBar = view.findViewById(R.id.signin_progress_login)
    private val backButton: View = view.findViewById(R.id.icon_back)
    private val versionText: TextView = view.findViewById(R.id.version_text)

    init {
        versionText.text = "v${BuildConfig.VERSION_NAME}"
        setListeners()

        if(firstTime) {
            rootLayout.visibility = View.INVISIBLE
            addGlobalLayout()
        }

        if(isMultipleAccountLogin)
            backButton.visibility = View.VISIBLE
    }

    private fun addGlobalLayout(){

        val viewTreeObserver = rootLayout.viewTreeObserver
        if (viewTreeObserver.isAlive) {
            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    revealActivity(view.resources.displayMetrics.widthPixels / 2,
                            view.resources.displayMetrics.heightPixels / 2)
                    rootLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        }
    }

    private fun revealActivity(x: Int, y: Int) {

        val finalRadius = (Math.max(rootLayout.width, rootLayout.height) * 1.1).toFloat()
        val circularReveal = ViewAnimationUtilsCompat.createCircularReveal(rootLayout, x, y, 0f, finalRadius)
        rootLayout.visibility = View.VISIBLE
        circularReveal?.start()

    }

    private fun setListeners() {

        signInButton.setOnClickListener {
            uiObserver?.onSubmitButtonClicked()
        }
        backButton.setOnClickListener{
            uiObserver?.onBackPressed()
        }
    }

    fun setSubmitButtonState(state : ProgressButtonState) {
        when (state) {
            ProgressButtonState.disabled -> {
                signInButton.visibility = View.VISIBLE
                signInButton.isEnabled = false
                progressBar.visibility = View.GONE
            }
            ProgressButtonState.enabled -> {
                signInButton.visibility = View.VISIBLE
                signInButton.isEnabled = true
                progressBar.visibility = View.GONE
            }
            ProgressButtonState.waiting -> {
                signInButton.visibility = View.GONE
                signInButton.isEnabled = false
                progressBar.visibility = View.VISIBLE
            }
        }
    }
}