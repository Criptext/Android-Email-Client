package com.criptext.mail.scenes.syncing

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.criptext.mail.R
import com.criptext.mail.scenes.settings.SettingsUIObserver
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage

class SyncBeginDialog(val context: Context, val message: UIMessage) {

    private var dialog: AlertDialog? = null
    private val res = context.resources
    private var uiObserver: SettingsUIObserver? = null
    private var animLoading: AnimatorSet? = null
    private var rootLayout: View? = null
    private var resendButton: TextView? = null
    private var textViewPrompt: TextView? = null
    private var textViewTitle: TextView? = null
    private var textViewBody: TextView? = null
    private var textViewNotApproved: TextView? = null
    private var failedImageLayout: FrameLayout? = null
    private var loadingImageLayout: FrameLayout? = null

    fun showDialog(settingsUIObserver: SettingsUIObserver?) {

        uiObserver = settingsUIObserver

        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        val dialogView = inflater.inflate(R.layout.sync_begin_dialog, null)

        dialogBuilder.setView(dialogView)

        dialog = createDialog(dialogView, dialogBuilder)
    }

    private fun createDialog(dialogView: View, dialogBuilder: AlertDialog.Builder): AlertDialog {

        val width = res.getDimension(R.dimen.password_login_dialog_width).toInt()
        val newLogoutDialog = dialogBuilder.create()
        val window = newLogoutDialog.window
        newLogoutDialog.show()
        window?.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER_VERTICAL)
        val drawableBackground = ContextCompat.getDrawable(dialogView.context,
                R.drawable.dialog_label_chooser_shape)
        newLogoutDialog.window?.setBackgroundDrawable(drawableBackground)
        newLogoutDialog.setCanceledOnTouchOutside(false)
        newLogoutDialog.setCancelable(false)
        rootLayout = dialogView.findViewById(R.id.viewRoot)
        startLoadingAnimation(dialogView)
        loadViews(dialogView)

        textViewTitle?.text = context.getLocalizedUIMessage(message)


        resendButton?.setOnClickListener{
            it.isEnabled = false
            uiObserver?.onResendDeviceLinkAuth()
        }

        dialogView.findViewById<Button>(R.id.cancel_button).setOnClickListener {
            uiObserver?.onSyncMailboxCanceled()
            dismiss()
        }


        return newLogoutDialog
    }

    private fun loadViews(dialogView: View){
        resendButton = dialogView.findViewById(R.id.resend_button)
        textViewPrompt = dialogView.findViewById(R.id.textViewPrompt)
        textViewTitle = dialogView.findViewById(R.id.message)
        textViewBody = dialogView.findViewById(R.id.textViewBody)
        textViewNotApproved = dialogView.findViewById(R.id.textViewNotAproved)
        failedImageLayout = dialogView.findViewById(R.id.failed_x)
        loadingImageLayout = dialogView.findViewById(R.id.sign_in_anim)
    }

    private fun startLoadingAnimation(view: View){

        rootLayout?.post {
            animLoading = initLoadingAnimatorSet(view.findViewById(R.id.imageViewCircularArrow))
            animLoading!!.start()
        }

    }

    private fun initLoadingAnimatorSet(viewArrow: View): AnimatorSet{

        val animArray = arrayOfNulls<ObjectAnimator>(2)
        var animObj = ObjectAnimator.ofFloat(viewArrow, "rotation", 45f)

        initSyncObjectAnim(animObj, ValueAnimator.RESTART, 250,0 )
        animArray[0] = animObj

        animObj = ObjectAnimator.ofFloat(viewArrow, "rotation", 360f)
        initSyncObjectAnim(animObj, ValueAnimator.RESTART, 125, 250)
        animArray[1] = animObj

        val animSet = AnimatorSet()
        animSet.playTogether(*animArray)
        animSet.duration = 1000
        return animSet
    }

    private fun initSyncObjectAnim(animObj: ObjectAnimator, repeatMode: Int,
                                   duration: Long, delay: Long) {
        animObj.repeatMode = repeatMode
        animObj.repeatCount = -1
        animObj.duration = duration
        animObj.startDelay = delay
    }

    fun showFailedSync(){
        resendButton?.visibility = View.GONE
        textViewPrompt?.visibility = View.GONE
        textViewTitle?.text = context.getLocalizedUIMessage(UIMessage(R.string.title_failed))
        textViewBody?.text = context.getLocalizedUIMessage(UIMessage(R.string.login_failed_body))
        textViewNotApproved?.visibility = View.VISIBLE
        animLoading?.cancel()
        loadingImageLayout?.visibility = View.GONE
        failedImageLayout?.visibility = View.VISIBLE
    }

    fun enableResendButton(){
        resendButton?.isEnabled = true
    }

    fun dismiss() {
        dialog?.dismiss()
    }
}
