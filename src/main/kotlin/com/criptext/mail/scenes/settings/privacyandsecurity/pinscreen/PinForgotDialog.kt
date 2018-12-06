package com.criptext.mail.scenes.settings.privacyandsecurity.pinscreen

import android.content.Context
import android.os.CountDownTimer
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.criptext.mail.R
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.github.omadahealth.lollipin.lib.managers.AppLockActivity


class PinForgotDialog(val context: Context) {

    private var dialog: AlertDialog? = null
    private val res = context.resources

    fun showDialog(observer: LockScreenUIObserver?, dataSource: GeneralDataSource) {

        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppLockActivity).layoutInflater
        val dialogView = inflater.inflate(R.layout.settings_forgot_pin_dialog, null)

        dialogBuilder.setView(dialogView)

        dialog = createDialog(dialogView, dialogBuilder, observer, dataSource)
    }

    private fun createDialog(dialogView: View, dialogBuilder: AlertDialog.Builder,
                             observer: LockScreenUIObserver?, dataSource: GeneralDataSource): AlertDialog {

        val width = res.getDimension(R.dimen.password_login_dialog_width).toInt()
        val newLogoutDialog = dialogBuilder.create()
        val window = newLogoutDialog.window
        newLogoutDialog.show()
        window.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.CENTER_VERTICAL)
        val drawableBackground = ContextCompat.getDrawable(dialogView.context,
                R.drawable.dialog_label_chooser_shape)
        newLogoutDialog.window.setBackgroundDrawable(drawableBackground)

        assignButtonEvents(dialogView, newLogoutDialog, observer, dataSource)


        return newLogoutDialog
    }

    private fun assignButtonEvents(view: View, dialog: AlertDialog,
                                   observer: LockScreenUIObserver?, dataSource: GeneralDataSource) {

        val btn_yes = view.findViewById(R.id.settings_logout_yes) as Button
        btn_yes.isEnabled = false
        timerListener(btn_yes,10000)
        val btn_no = view.findViewById(R.id.settings_logout_cancel) as Button

        btn_yes.setOnClickListener {
            dialog.dismiss()
            observer?.onForgotPinYesPressed(dataSource)
        }

        btn_no.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun timerListener(button: Button, startTime: Long) {
        object : CountDownTimer(startTime, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                val sec = ((millisUntilFinished / 1000) % 60).toInt()
                button.text = context.getString(R.string.yes_with_time, sec)
            }

            override fun onFinish() {
                button.setText(R.string.yes)
                button.isEnabled = true
            }
        }.start()
    }
}
