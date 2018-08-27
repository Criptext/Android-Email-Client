package com.criptext.mail.scenes.settings.recovery_email

import android.os.CountDownTimer
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.criptext.mail.R
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage


interface RecoveryEmailScene{

    fun attachView(recoveryEmailUIObserver: RecoveryEmailUIObserver,
                   keyboardManager: KeyboardManager, model: RecoveryEmailModel)
    fun showMessage(message: UIMessage)
    fun onResendLinkTimeSet(startTime: Long)
    fun onResendLinkFailed()
    fun showConfirmationSentDialog()

    class Default(val view: View): RecoveryEmailScene{
        private lateinit var recoveryEmailUIObserver: RecoveryEmailUIObserver

        private val context = view.context

        private val backButton: ImageView by lazy {
            view.findViewById<ImageView>(R.id.mailbox_back_button)
        }

        private val textViewCurrentEmail: TextView by lazy {
            view.findViewById<TextView>(R.id.recovery_email_text)
        }

        private val textViewConfirmText: TextView by lazy {
            view.findViewById<TextView>(R.id.not_confirmed_text)
        }

        private val resendLinkProgressButton: View by lazy {
            view.findViewById<View>(R.id.resend_progress_button)
        }

        private val resendLinkButton: Button by lazy {
            view.findViewById<Button>(R.id.resend_link_button)
        }

        private val changeEmailButton: Button by lazy {
            view.findViewById<Button>(R.id.change_email_button)
        }

        private val confirmationSentDialog = ConfirmationSentDialog(context)

        override fun attachView(recoveryEmailUIObserver: RecoveryEmailUIObserver, keyboardManager: KeyboardManager,
                                model: RecoveryEmailModel) {
            this.recoveryEmailUIObserver = recoveryEmailUIObserver
            if(model.isEmailConfirmed) {
                textViewConfirmText.setTextColor(ContextCompat.getColor(
                        view.context, R.color.green))
                textViewConfirmText.setText(R.string.status_confirmed)
                resendLinkButton.visibility = View.GONE
            }else{
                textViewConfirmText.setTextColor(ContextCompat.getColor(
                        view.context, R.color.red))
                textViewConfirmText.setText(R.string.status_not_confirmed)
                val lastResend = System.currentTimeMillis() - model.lastTimeConfirmationLinkSent
                if(model.lastTimeConfirmationLinkSent != 0L && (lastResend < RecoveryEmailController.RESEND_TIME)){
                    resendLinkButton.isEnabled = false
                    timerListener(RecoveryEmailController.RESEND_TIME - lastResend)
                }else {
                    resendLinkButton.visibility = View.VISIBLE
                }
            }
            backButton.setOnClickListener {
                this.recoveryEmailUIObserver.onBackButtonPressed()
            }

            resendLinkButton.setOnClickListener {
                this.recoveryEmailUIObserver.onResendRecoveryLinkPressed()
                resendLinkButton.visibility = View.GONE
                resendLinkProgressButton.visibility = View.VISIBLE
            }

            changeEmailButton.setOnClickListener {
                this.recoveryEmailUIObserver.onChangeRecoveryEmailPressed()
            }

            textViewCurrentEmail.text = model.recoveryEmail
        }

        private fun timerListener(startTime: Long) {
            object : CountDownTimer(startTime, 1000) {

                override fun onTick(millisUntilFinished: Long) {
                    val min = ((millisUntilFinished / 1000) / 60).toInt()
                    val sec = ((millisUntilFinished / 1000) % 60).toInt()
                    resendLinkButton.text = if(sec < 10) "$min:0$sec" else "$min:$sec"
                }

                override fun onFinish() {
                    resendLinkButton.setText(R.string.button_resend_confirmation)
                    resendLinkButton.isEnabled = true
                }
            }.start()
        }

        override fun onResendLinkTimeSet(startTime: Long) {
            timerListener(startTime)
        }

        override fun onResendLinkFailed() {
            resendLinkButton.visibility = View.VISIBLE
            resendLinkProgressButton.visibility = View.GONE
        }

        override fun showConfirmationSentDialog() {
            confirmationSentDialog.showDialog(recoveryEmailUIObserver)
            resendLinkButton.visibility = View.VISIBLE
            resendLinkButton.isEnabled = false
            resendLinkProgressButton.visibility = View.GONE
        }

        override fun showMessage(message: UIMessage) {
            val duration = Toast.LENGTH_LONG
            val toast = Toast.makeText(
                    context,
                    context.getLocalizedUIMessage(message),
                    duration)
            toast.show()
        }
    }

}