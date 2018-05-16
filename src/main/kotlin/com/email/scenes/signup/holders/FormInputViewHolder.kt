package com.email.scenes.signup.holders

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatEditText
import android.view.View
import com.email.R
import com.email.utils.form.FormInputState
import com.email.utils.getLocalizedUIMessage

/**
 * Created by gabriel on 5/15/18.
 */
class FormInputViewHolder(private val textInputLayout: TextInputLayout,
                          private val editText: AppCompatEditText,
                          private val validView: View,
                          private val errorView: View,
                          private val disableSubmitButton: () -> Unit) {

    private val ctx = textInputLayout.context


    @SuppressLint("RestrictedApi")
    fun setState(state: FormInputState) {
        when (state) {
            is FormInputState.Valid -> {
                validView.visibility = View.VISIBLE
                errorView.visibility = View.INVISIBLE
                textInputLayout.error = ""

                textInputLayout.setHintTextAppearance(R.style.textinputlayout_login)
                editText.supportBackgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(ctx, R.color.signup_hint_color))
            }

            is FormInputState.Unknown -> {
                validView.visibility = View.INVISIBLE
                errorView.visibility = View.INVISIBLE
                textInputLayout.error = ""

                textInputLayout.setHintTextAppearance(R.style.textinputlayout_login)
                editText.supportBackgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(ctx, R.color.signup_hint_color))
            }

            is FormInputState.Error -> {
                validView.visibility = View.INVISIBLE
                errorView.visibility = View.VISIBLE
                textInputLayout.error = ctx.getLocalizedUIMessage(state.message)

                textInputLayout.setHintTextAppearance(R.style.textinputlayout_login_error)
                editText.supportBackgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(ctx, R.color.black))

                disableSubmitButton()
            }
        }
    }

    fun setHintTextAppearance(resId: Int) = textInputLayout.setHintTextAppearance(resId)
}