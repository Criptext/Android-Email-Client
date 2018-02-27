package com.email

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView

/**
 * Created by danieltigse on 2/16/18.
 */

class ConnectionActivity : AppCompatActivity(){

    private val loadingView: View by lazy {
        findViewById<View>(R.id.viewAnimation)
    }

    private val textViewStatus: TextView by lazy {
        findViewById<TextView>(R.id.textViewStatus)
    }

    private val textViewEmail: TextView by lazy {
        findViewById<TextView>(R.id.textViewEmail)
    }

    private lateinit var animLoading: AnimatorSet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_connection)
        startLoadingAnimation()

        Handler().postDelayed({
            startSucceedAnimation()
        }, 3000)

    }

    private fun startSucceedAnimation(){

        animLoading.cancel()
        loadingView.post {
            val animSucceed = initSuccessAnimatorSet(findViewById(R.id.viewCircle1), findViewById(R.id.viewCircle2),
                    findViewById(R.id.viewCircle3), findViewById(R.id.viewCircle4),
                    findViewById(R.id.viewCircle5), findViewById(R.id.viewCircle6),
                    findViewById(R.id.viewCircle7), findViewById(R.id.viewCircle8),
                    findViewById(R.id.viewCircle9), findViewById(R.id.viewCircle10),
                    findViewById(R.id.viewCircle11), findViewById(R.id.viewCircle12),
                    findViewById(R.id.imageViewDevice1), findViewById(R.id.imageViewDevice2),
                    findViewById(R.id.imageViewSucceed))
            animSucceed.start()
        }
        textViewEmail.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
        textViewStatus.text = resources.getText(R.string.device_ready)
    }

    private fun startLoadingAnimation(){

        loadingView.post {
            animLoading = initSyncingAnimatorSet(findViewById(R.id.viewCircle1), findViewById(R.id.viewCircle2),
                    findViewById(R.id.viewCircle3), findViewById(R.id.viewCircle4),
                    findViewById(R.id.viewCircle5), findViewById(R.id.viewCircle6),
                    findViewById(R.id.viewCircle7), findViewById(R.id.viewCircle8),
                    findViewById(R.id.viewCircle9), findViewById(R.id.viewCircle10),
                    findViewById(R.id.viewCircle11), findViewById(R.id.viewCircle12))
            animLoading.start()
        }

    }

    private fun initSyncingAnimatorSet(circle1: View, circle2: View, circle3: View, circle4: View,
                                       circle5: View, circle6: View, circle7: View, circle8: View,
                                       circle9: View, circle10: View, circle11: View, circle12: View): AnimatorSet {

        val animArray = arrayOfNulls<ObjectAnimator>(12)
        var animObj = ObjectAnimator.ofFloat(circle7, "alpha", 0.1f, 1f)
        initSyncObjectAnim(animObj, 0)
        animArray[0] = animObj
        animObj = ObjectAnimator.ofFloat(circle8, "alpha", 0.1f, 1f)
        initSyncObjectAnim(animObj, 100)
        animArray[1] = animObj
        animObj = ObjectAnimator.ofFloat(circle9, "alpha", 0.1f, 1f)
        initSyncObjectAnim(animObj, 200)
        animArray[2] = animObj
        animObj = ObjectAnimator.ofFloat(circle10, "alpha", 0.1f, 1f)
        initSyncObjectAnim(animObj, 300)
        animArray[3] = animObj
        animObj = ObjectAnimator.ofFloat(circle11, "alpha", 0.1f, 1f)
        initSyncObjectAnim(animObj, 400)
        animArray[4] = animObj
        animObj = ObjectAnimator.ofFloat(circle12, "alpha", 0.1f, 1f)
        initSyncObjectAnim(animObj, 500)
        animArray[5] = animObj
        animObj = ObjectAnimator.ofFloat(circle1, "alpha", 1f, 0.1f)
        initSyncObjectAnim(animObj, 0)
        animArray[6] = animObj
        animObj = ObjectAnimator.ofFloat(circle2, "alpha", 1f, 0.1f)
        initSyncObjectAnim(animObj, 100)
        animArray[7] = animObj
        animObj = ObjectAnimator.ofFloat(circle3, "alpha", 1f, 0.1f)
        initSyncObjectAnim(animObj, 200)
        animArray[8] = animObj
        animObj = ObjectAnimator.ofFloat(circle4, "alpha", 1f, 0.1f)
        initSyncObjectAnim(animObj, 300)
        animArray[9] = animObj
        animObj = ObjectAnimator.ofFloat(circle5, "alpha", 1f, 0.1f)
        initSyncObjectAnim(animObj, 400)
        animArray[10] = animObj
        animObj = ObjectAnimator.ofFloat(circle6, "alpha", 1f, 0.1f)
        initSyncObjectAnim(animObj, 500)
        animArray[11] = animObj
        val animSet = AnimatorSet()
        animSet.playTogether(*animArray)
        animSet.duration = 500
        return animSet

    }

    private fun initSyncObjectAnim(animObj: ObjectAnimator, delay: Long) {
        animObj.repeatMode = ValueAnimator.REVERSE
        animObj.repeatCount = -1
        if (delay > 0)
            animObj.startDelay = delay
    }

    private fun initSuccessAnimatorSet(circle1: View, circle2: View, circle3: View, circle4: View,
                                       circle5: View, circle6: View, circle7: View, circle8: View,
                                       circle9: View, circle10: View, circle11: View, circle12: View,
                                       device1: View, device2: View, viewSucceed: View): AnimatorSet {

        val animArray = arrayOfNulls<ObjectAnimator>(15)
        var animObj = ObjectAnimator.ofFloat(circle7, "alpha", 1f, 0f)
        initSuccessObjectAnim(animObj, 0)
        animArray[0] = animObj
        animObj = ObjectAnimator.ofFloat(circle8, "alpha", 1f, 0f)
        initSuccessObjectAnim(animObj, 100)
        animArray[1] = animObj
        animObj = ObjectAnimator.ofFloat(circle9, "alpha", 1f, 0f)
        initSuccessObjectAnim(animObj, 200)
        animArray[2] = animObj
        animObj = ObjectAnimator.ofFloat(circle10, "alpha", 1f, 0f)
        initSuccessObjectAnim(animObj, 300)
        animArray[3] = animObj
        animObj = ObjectAnimator.ofFloat(circle11, "alpha", 1f, 0f)
        initSuccessObjectAnim(animObj, 400)
        animArray[4] = animObj
        animObj = ObjectAnimator.ofFloat(circle12, "alpha", 1f, 0f)
        initSuccessObjectAnim(animObj, 500)
        animArray[5] = animObj
        animObj = ObjectAnimator.ofFloat(circle1, "alpha", 1f, 0.0f)
        initSuccessObjectAnim(animObj, 0)
        animArray[6] = animObj
        animObj = ObjectAnimator.ofFloat(circle2, "alpha", 1f, 0.0f)
        initSuccessObjectAnim(animObj, 100)
        animArray[7] = animObj
        animObj = ObjectAnimator.ofFloat(circle3, "alpha", 1f, 0.0f)
        initSuccessObjectAnim(animObj, 200)
        animArray[8] = animObj
        animObj = ObjectAnimator.ofFloat(circle4, "alpha", 1f, 0.0f)
        initSuccessObjectAnim(animObj, 300)
        animArray[9] = animObj
        animObj = ObjectAnimator.ofFloat(circle5, "alpha", 1f, 0.0f)
        initSuccessObjectAnim(animObj, 400)
        animArray[10] = animObj
        animObj = ObjectAnimator.ofFloat(circle6, "alpha", 1f, 0.0f)
        initSuccessObjectAnim(animObj, 500)
        animArray[11] = animObj

        animObj = ObjectAnimator.ofFloat(device1, "x", device1.x, circle2.x
                + circle2.width + circle3.width)
        initSuccessObjectAnim(animObj, 600)
        animArray[12] = animObj

        animObj = ObjectAnimator.ofFloat(device2, "x", device2.x, circle8.x)
        initSuccessObjectAnim(animObj, 600)
        animArray[13] = animObj

        animObj = ObjectAnimator.ofFloat(viewSucceed, "alpha", 0.0f, 1f)
        initSuccessObjectAnim(animObj, 600)
        animArray[14] = animObj

        val animSet = AnimatorSet()
        animSet.playTogether(*animArray)
        animSet.duration = 700
        return animSet

    }

    private fun initSuccessObjectAnim(animObj: ObjectAnimator, delay: Long) {
        if (delay > 0)
            animObj.startDelay = delay
    }
}