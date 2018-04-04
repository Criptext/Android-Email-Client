package com.email.scenes.emaildetail.ui.holders

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import com.email.R
import com.email.db.models.FullEmail
import com.email.scenes.emaildetail.ui.FullEmailListAdapter

/**
 * Created by sebas on 3/19/18.
 */


class FooterViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

    private val context = view.context
    val forward : Button
    val reply: Button
    val replyAll: Button

    init {
        forward = view.findViewById(R.id.forward)
        reply = view.findViewById(R.id.reply)
        replyAll = view.findViewById(R.id.reply_all)
    }

    private fun addButtonOnTouchListener(
            button: Button,
            view: View?,
            background: Drawable,
            motionEvent: MotionEvent) :Boolean {

        val drawables = button.compoundDrawables
        val leftDrawable = drawables[0]


        //leftDrawable.clearColorFilter()

        when(motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                leftDrawable.colorFilter = PorterDuffColorFilter(
                        ContextCompat.getColor(context, R.color.white),
                        PorterDuff.Mode.SRC_ATOP)
                background.colorFilter = PorterDuffColorFilter(
                        ContextCompat.getColor(context, R.color.azure),
                        PorterDuff.Mode.SRC_ATOP)

                button.setTextColor(
                        ContextCompat.getColor(context, R.color.white))
            }

            MotionEvent.ACTION_UP -> {
                view?.performClick()
                return false
            }

            MotionEvent.ACTION_BUTTON_RELEASE -> {
                view?.performClick()
                return false
            }

            MotionEvent.ACTION_CANCEL -> {
                button.setTextColor(
                        ContextCompat.getColor(context, R.color.black))
                leftDrawable.clearColorFilter()
                return true
            }
        }

        button.background = background
        return true

    }

    fun setListeners(emailListener: FullEmailListAdapter.OnFullEmailEventListener?) {

        replyAll.setOnTouchListener(object: View.OnTouchListener {
            override fun onTouch(
                    view: View?,
                    motionEvent: MotionEvent?): Boolean {

                val background = ContextCompat.getDrawable(
                        context,
                        R.drawable.email_detail_reply_all_btn_shape)
                if(motionEvent != null) {
                    return addButtonOnTouchListener(
                            button = replyAll,
                            background = background,
                            motionEvent = motionEvent,
                            view = view )
                }

                return true
            }
        })

        reply.setOnTouchListener(object: View.OnTouchListener {
            override fun onTouch(
                    view: View?,
                    motionEvent: MotionEvent?): Boolean {

                val background = ContextCompat.getDrawable(
                        context,
                        R.drawable.email_detail_reply_btn_shape)
                if(motionEvent != null) {
                    return addButtonOnTouchListener(
                            button = reply,
                            background = background,
                            motionEvent = motionEvent,
                            view = view )
                }

                return true
            }
        })

        forward.setOnTouchListener(object: View.OnTouchListener {
            override fun onTouch(
                    view: View?,
                    motionEvent: MotionEvent?): Boolean {

                val background = ContextCompat.getDrawable(
                        context,
                        R.drawable.email_detail_forward_btn_shape)

                if(motionEvent != null) {
                    return addButtonOnTouchListener(
                            button = forward,
                            background = background,
                            motionEvent = motionEvent,
                            view = view )
                }

                return true
            }
        })

        forward.setOnClickListener{
            emailListener?.onForwardBtnClicked()
        }

        reply.setOnClickListener{
            emailListener?.onReplyBtnClicked()
        }

        replyAll.setOnClickListener{
            emailListener?.onReplyAllBtnClicked()
        }
    }

}
