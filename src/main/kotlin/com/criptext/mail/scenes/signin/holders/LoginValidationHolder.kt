package com.criptext.mail.scenes.signin.holders

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.criptext.mail.R
import com.criptext.mail.scenes.signin.OnPasswordLoginDialogListener
import com.criptext.mail.scenes.signin.PasswordLoginDialog

/**
 * Created by sebas on 3/8/18.
 */

class LoginValidationHolder(
        val view: View,
        val initialState: SignInLayoutState.LoginValidation
): BaseSignInHolder() {

    private var animLoading: AnimatorSet? = null
    private val rootLayout: View
    private val cantAccessDevice: TextView
    private val textViewTitle: TextView
    private val textViewBody: TextView
    private val textViewPrompt: TextView
    private val textViewNotApproved: TextView
    private val backButton: View
    private val buttonResend: Button

    private val passwordLoginDialog = PasswordLoginDialog(view.context)

    init {
        rootLayout = view.findViewById<View>(R.id.viewRoot)
        cantAccessDevice = view.findViewById(R.id.cant_access_device)
        textViewTitle = view.findViewById(R.id.textViewTitle)
        textViewBody = view.findViewById(R.id.textViewBody)
        buttonResend = view.findViewById(R.id.buttonResend)
        textViewNotApproved = view.findViewById(R.id.textViewNotAproved)
        textViewPrompt = view.findViewById(R.id.textViewPrompt)
        backButton = view.findViewById(R.id.icon_back)

        setListeners()
        startLoadingAnimation()
    }

    private fun startLoadingAnimation(){

        rootLayout.post {
            animLoading = initLoadingAnimatorSet(view.findViewById(R.id.imageViewCircularArrow),
                    view.findViewById(R.id.imageViewWatch))
            animLoading!!.start()
        }

    }

    private fun initLoadingAnimatorSet(viewArrow: View, viewWatch: View): AnimatorSet{

        val animArray = arrayOfNulls<ObjectAnimator>(4)
        var animObj = ObjectAnimator.ofFloat(viewArrow, "rotation", 45f)

        initSyncObjectAnim(animObj, ValueAnimator.RESTART, 250,0 )
        animArray[0] = animObj

        animObj = ObjectAnimator.ofFloat(viewWatch, "rotation", 45f)
        initSyncObjectAnim(animObj, ValueAnimator.RESTART, 250, 0)
        animArray[1] = animObj

        animObj = ObjectAnimator.ofFloat(viewArrow, "rotation", 360f)
        initSyncObjectAnim(animObj, ValueAnimator.RESTART, 125, 250)
        animArray[2] = animObj

        animObj = ObjectAnimator.ofFloat(viewWatch, "rotation", 360f)
        initSyncObjectAnim(animObj, ValueAnimator.RESTART, 125, 250)
        animArray[3] = animObj

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

    private fun initFailedAnimatorSet(viewArrow: View, viewWatch: View): AnimatorSet{

        val animArray = arrayOfNulls<ObjectAnimator>(2)
        var animObj = ObjectAnimator.ofFloat(viewArrow, "rotation", 0f)
        animObj.duration = 250
        animArray[0] = animObj

        animObj = ObjectAnimator.ofFloat(viewWatch, "rotation", 0f)
        animObj.duration = 250
        animArray[1] = animObj

        val animSet = AnimatorSet()
        animSet.playTogether(*animArray)
        animSet.duration = 250
        return animSet
    }

    private fun showFailedLogin(){

        textViewTitle.text = view.resources.getText(R.string.failed)
        textViewBody.text = view.resources.getText(R.string.login_rejected)
        textViewNotApproved.visibility = View.VISIBLE
        buttonResend.visibility = View.GONE
        textViewPrompt.visibility = View.GONE
        animLoading?.cancel()
        rootLayout.post {
            val animLoading = initFailedAnimatorSet(view.findViewById(R.id.imageViewCircularArrow),
                    view.findViewById(R.id.imageViewWatch))
            animLoading.start()
        }
    }

    private fun setListeners() {

        backButton.setOnClickListener {
            uiObserver?.onBackPressed()
        }

        cantAccessDevice.setOnClickListener {
            uiObserver?.onCantAccessDeviceClick()
        }
    }

    fun showPasswordLoginDialog(onPasswordLoginDialogListener: OnPasswordLoginDialogListener) {
        passwordLoginDialog.showPasswordLoginDialog(initialState.username, onPasswordLoginDialogListener)
    }
}
