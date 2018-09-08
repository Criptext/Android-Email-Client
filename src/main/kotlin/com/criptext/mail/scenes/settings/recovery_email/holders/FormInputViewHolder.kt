package com.criptext.mail.scenes.settings.recovery_email.holders

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatEditText
import android.view.View
import com.criptext.mail.R
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.validation.FormInputState

/**
 * Created by gabriel on 5/15/18.
 */
class FormInputViewHolder(private val textInputLayout: TextInputLayout,
                          private val editText: AppCompatEditText,
                          private val validView: View?,
                          private val errorView: View?,
                          private val disableSubmitButton: () -> Unit) {

    private val ctx = textInputLayout.context


    @SuppressLint("RestrictedApi")
    fun setState(state: FormInputState) {
        when (state) {
            is FormInputState.Valid -> {
                validView?.visibility = View.VISIBLE
                errorView?.visibility = View.INVISIBLE
                textInputLayout.error = ""

                editText.supportBackgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(ctx, R.color.non_criptext_email_send_text_line_focus))
            }

            is FormInputState.Unknown -> {
                validView?.visibility = View.INVISIBLE
                errorView?.visibility = View.INVISIBLE
                textInputLayout.error = ""

                editText.supportBackgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(ctx, R.color.black))
            }

            is FormInputState.Error -> {
                validView?.visibility = View.INVISIBLE
                errorView?.visibility = View.VISIBLE
                textInputLayout.error = ctx.getLocalizedUIMessage(state.message)

                editText.supportBackgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(ctx, R.color.red))

                disableSubmitButton()
            }
        }
    }

    fun setHintTextAppearance(resId: Int) = textInputLayout.setHintTextAppearance(resId)
}