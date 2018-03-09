package com.email.scenes.signin.holders

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.os.Build
import android.os.Handler
import android.support.annotation.RequiresApi
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewTreeObserver
import android.view.animation.AccelerateInterpolator
import android.widget.Button
import android.widget.TextView
import com.email.R
import com.email.scenes.signin.OnPasswordLoginDialogListener
import com.email.scenes.signin.PasswordLoginDialog
import com.email.scenes.signin.SignInSceneController

/**
 * Created by sebas on 3/8/18.
 */

class LoginValidationHolder(val view: View) {

    private val cantAccessDevice: TextView
    private var animLoading: AnimatorSet? = null
    private val rootLayout: View

    private val textViewTitle: TextView
    private val textViewBody: TextView
    private val textViewPrompt: TextView
    private val textViewNotApproved: TextView

    private val buttonResend: Button

    private val passwordLoginDialog = PasswordLoginDialog(view.context)
    var signInListener: SignInSceneController.SignInListener? = null

    init {
        cantAccessDevice = view.findViewById(R.id.cant_access_device)
        rootLayout = view.findViewById<View>(R.id.viewRoot)
        textViewTitle = view.findViewById(R.id.textViewTitle)
        textViewBody = view.findViewById(R.id.textViewBody)
        buttonResend = view.findViewById(R.id.buttonResend)
        textViewNotApproved = view.findViewById<TextView>(R.id.textViewNotAproved)
        textViewPrompt = view.findViewById(R.id.textViewPrompt)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            rootLayout.visibility = View.INVISIBLE
            addGlobalLayout()
        } else {
            rootLayout.visibility = View.VISIBLE
        }

        startLoadingAnimation()
        Handler().postDelayed({
            showFailedLogin()
        }, 3000)
    }

    private fun addGlobalLayout(){

        val viewTreeObserver = rootLayout.viewTreeObserver
        if (viewTreeObserver.isAlive) {
            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                override fun onGlobalLayout() {
                    revealActivity(view.resources.displayMetrics.widthPixels / 2,
                            view.resources.displayMetrics.heightPixels / 2)
                    rootLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun revealActivity(x: Int, y: Int) {

        val finalRadius = (Math.max(rootLayout.width, rootLayout.height) * 1.1).toFloat()

        val circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, x, y, 0f, finalRadius)
        circularReveal.duration = 500
        circularReveal.interpolator = AccelerateInterpolator()

        rootLayout.visibility = View.VISIBLE;
        circularReveal.start()

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

    fun assignCantAccessDeviceListener() {
        cantAccessDevice.setOnClickListener {
            signInListener!!.onCantAccessDeviceClick()
        }
    }

    fun showPasswordLoginDialog(
            onPasswordLoginDialogListener: OnPasswordLoginDialogListener) {
        passwordLoginDialog.showPasswordLoginDialog(onPasswordLoginDialogListener)
    }
}
