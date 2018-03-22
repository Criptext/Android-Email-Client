package com.email.utils

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout

/**
 * Created by hirobreak on 21/06/17.
 */

class ZoomLayout : FrameLayout, ScaleGestureDetector.OnScaleGestureListener {

    var slideContainer : (dx : Int) -> Unit = {}
    var verticalSlideContainer : (dy : Int) -> Unit = {}
    private enum class Mode {
        NONE,
        DRAG,
        ZOOM
    }

    private var mode = Mode.NONE
    var scale = 1.0f
    private var lastScaleFactor = 0f
    private var isPinching = false
    // Where the finger first  touches the screen
    private var startX = 0f
    private var startY = 0f

    // How much to translate the canvas
    private var dx = 0f
    private var dy = 0f

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if(ev == null){
            return false
        }
        if(ev.action == MotionEvent.ACTION_DOWN){
            startMotionEvent(ev)
        }else if(ev.actionMasked == MotionEvent.ACTION_POINTER_DOWN){
            startMotionEvent(ev)
            mode = Mode.ZOOM
            isPinching = true
        }

        return isPinching
    }

    private fun init(context: Context) {
        val scaleDetector = ScaleGestureDetector(context, this)
        this.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {

                when (motionEvent.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_DOWN -> {
                        startMotionEvent(motionEvent, view)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        dx = motionEvent.x - startX
                        dy = motionEvent.y - startY
                    }
                    MotionEvent.ACTION_POINTER_DOWN -> {
                        mode = Mode.ZOOM
                        startMotionEvent(motionEvent, view)
                    }
                    MotionEvent.ACTION_POINTER_UP -> mode = Mode.DRAG
                    MotionEvent.ACTION_UP -> {
                        stopMotionEvent(motionEvent)
                    }
                }
                if(isPinching) {
                    scaleDetector.onTouchEvent(motionEvent)
                    scaleTransition()
                }
                return true
            }
        })
    }

    private fun getRelativeTop(myView: View): Int {
        if (myView.parent === myView.rootView)
            return myView.top
        else
            return myView.top + getRelativeTop(myView.parent as View)
    }

    private fun getRelativeLeft(myView: View): Int {
        if (myView.parent === myView.rootView)
            return myView.left
        else
            return myView.left + getRelativeLeft(myView.parent as View)
    }

    fun startMotionEvent(motionEvent: MotionEvent){
        startX = motionEvent.x - getRelativeLeft(this)
        startY = motionEvent.y - getRelativeTop(this)
    }

    fun startMotionEvent(motionEvent: MotionEvent, view: View){
        startX = motionEvent.x - getRelativeLeft(this)
        startY = motionEvent.y - getRelativeTop(this)
    }

    fun stopMotionEvent(motionEvent: MotionEvent){
        isPinching = false
        mode = Mode.NONE
        parent.requestDisallowInterceptTouchEvent(false)
    }

    fun scaleTransition(){
        if (mode == Mode.DRAG && scale >= MIN_ZOOM || mode == Mode.ZOOM) {
            parent.requestDisallowInterceptTouchEvent(true)
            val maxDx = (child().getWidth() - child().getWidth() / scale) / 2 * scale
            val maxDy = (child().getHeight() - child().getHeight() / scale) / 2 * scale
            dx = Math.min(Math.max(dx/2, -maxDx), maxDx)
            dy = Math.min(Math.max(dy/2, -maxDy), maxDy)
            applyScaleAndTranslation()
        }
    }

    // ScaleGestureDetector

    override fun onScaleBegin(scaleDetector: ScaleGestureDetector): Boolean {
        return true
    }

    override fun onScale(scaleDetector: ScaleGestureDetector): Boolean {
        val scaleFactor = scaleDetector.scaleFactor
        if (lastScaleFactor == 0f || Math.signum(scaleFactor) == Math.signum(lastScaleFactor)) {
            scale *= scaleFactor
            scale = Math.max(MIN_ZOOM, Math.min(scale, MAX_ZOOM))
            lastScaleFactor = scaleFactor
        } else {
            lastScaleFactor = 0f
        }
        return true
    }

    override fun onScaleEnd(scaleDetector: ScaleGestureDetector) {
    }

    fun applyScaleAndTranslation() {
        val childView = child()
        childView.setScaleX(scale)
        childView.setScaleY(scale)
        childView.translationY = (childView.height * scale - childView.height)/2
        childView.translationX = (childView.width * scale - childView.width)/2
        layoutParams = LinearLayout.LayoutParams((scale * childView.width).toInt(), (scale * childView.height).toInt())
    }

    private fun child(): View {
        val container = getChildAt(0)
        return container

    }

    companion object {

        private val TAG = "ZoomLayout"
        private val MIN_ZOOM = 1.0f
        private val MAX_ZOOM = 4.0f
    }
}
