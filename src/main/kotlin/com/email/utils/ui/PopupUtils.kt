package com.email.utils.ui

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import com.email.R
import com.michael.easydialog.EasyDialog

/**
 * Created by sebas on 3/19/18.
 */

class PopupUtils {
    companion object {

        private fun shouldPopUpRenderBottom(
                context: Context, anchorView: View) : Boolean {
            val location = IntArray(2)
            anchorView.getLocationOnScreen(location)
            val anchorLocationY = location[1]
            val displayMetrics = DisplayMetrics()
            val wm = context.getSystemService(Context.WINDOW_SERVICE)
                    as WindowManager
            wm.defaultDisplay.getMetrics(displayMetrics)
            val screenHeight = displayMetrics.heightPixels

            return anchorLocationY> screenHeight / 2
        }

        fun createPopUpWindow(
                context: Context,
                anchorView: View,
                contentView: View) {
            val popup = EasyDialog(context)
                    .setLayout(contentView)
                    .setBackgroundColor(
                            ContextCompat.getColor(
                                    context, R.color.white))
                    .setLocationByAttachedView(anchorView)
                    .setAnimationAlphaShow(200, 0.3f, 1.0f)
                    .setAnimationAlphaDismiss(200, 1.0f, 0.0f)
                    .setOutsideColor(R.color.azure)
                    .setTouchOutsideDismiss(true)
                    .setMatchParent(false)
                    .setMarginLeftAndRight(24, 24)

            val shouldPopUpRenderBottom = shouldPopUpRenderBottom(
                    context, anchorView)

            if(shouldPopUpRenderBottom)  {
                popup.setGravity(EasyDialog.GRAVITY_TOP)
            } else {
                popup.setGravity(EasyDialog.GRAVITY_BOTTOM)
            }

            popup.show()
        }

    }
}