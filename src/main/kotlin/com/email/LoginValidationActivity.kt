package com.email

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewTreeObserver
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.TextView

/**
 * Created by danieltigse on 2/16/18.
 */

class LoginValidationActivity: AppCompatActivity() {

    private lateinit var animLoading: AnimatorSet

    private val rootLayout: View by lazy {
        this.findViewById<View>(R.id.viewRoot)
    }

    private val textViewTitle: TextView by lazy {
        findViewById<TextView>(R.id.textViewTitle)
    }

    private val textViewBody: TextView by lazy {
        findViewById<TextView>(R.id.textViewBody)
    }

    private val textViewPromot: TextView by lazy {
        findViewById<TextView>(R.id.textViewPromot)
    }

    private val textViewNotAproved: TextView by lazy {
        findViewById<TextView>(R.id.textViewNotAproved)
    }

    private val buttonResend: Button by lazy {
        findViewById<Button>(R.id.buttonResend)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login_validation)

        if (savedInstanceState == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
                override fun onGlobalLayout() {
                    revealActivity(resources.displayMetrics.widthPixels / 2,
                            resources.displayMetrics.heightPixels / 2)
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
            animLoading = initLoadingAnimatorSet(findViewById(R.id.imageViewCircularArrow),
                    findViewById(R.id.imageViewWatch))
            animLoading.start()
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

        textViewTitle.text = resources.getText(R.string.failed)
        textViewBody.text = resources.getText(R.string.login_rejected)
        textViewNotAproved.visibility = View.VISIBLE
        buttonResend.visibility = View.GONE
        textViewPromot.visibility = View.GONE
        animLoading.cancel()
        rootLayout.post {
            val animLoading = initFailedAnimatorSet(findViewById(R.id.imageViewCircularArrow),
                    findViewById(R.id.imageViewWatch))
            animLoading.start()
        }
    }
}