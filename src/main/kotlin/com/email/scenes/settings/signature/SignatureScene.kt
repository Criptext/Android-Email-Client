package com.email.scenes.settings.signature

import android.view.View
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.email.R
import com.email.scenes.composer.ComposerScene
import com.email.scenes.composer.ui.HTMLEditText
import com.email.utils.KeyboardManager
import com.email.utils.UIMessage
import com.email.utils.getLocalizedUIMessage
import com.email.utils.ui.SnackBarHelper
import org.wordpress.aztec.Aztec
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.source.SourceViewEditText
import org.wordpress.aztec.toolbar.AztecToolbar
import org.wordpress.aztec.toolbar.IAztecToolbarClickListener

interface SignatureScene{

    fun attachView(signatureUIObserver: SignatureUIObserver, signature: String,
                   keyboardManager: KeyboardManager)
    fun showMessage(message: UIMessage)
    fun getSignature(): String

    class Default(val view: View): SignatureScene{

        private val context = view.context

        private val backButton: ImageView by lazy {
            view.findViewById<ImageView>(R.id.mailbox_back_button)
        }

        private val textViewStatus: TextView by lazy {
            view.findViewById<TextView>(R.id.textview_status)
        }

        private val swicthStatus: Switch by lazy {
            view.findViewById<Switch>(R.id.switch_status)
        }

        private val viewSignature: View by lazy{
            view.findViewById<View>(R.id.view_signature)
        }

        private val viewToolbar: View by lazy{
            view.findViewById<View>(R.id.view_toolbar)
        }

        private val bodyEditText: HTMLEditText by lazy({
            HTMLEditText(
                    visualEditor = view.findViewById(R.id.visual),
                    sourceEditor = view.findViewById(R.id.source),
                    toolbar = view.findViewById(R.id.formatting_toolbar),
                    hint = context.getString(R.string.write_signature))
        })

        override fun attachView(signatureUIObserver: SignatureUIObserver, signature: String,
                                keyboardManager: KeyboardManager) {

            backButton.setOnClickListener {
                signatureUIObserver.onBackButtonPressed()
            }

            swicthStatus.setOnCheckedChangeListener { _, isChecked ->
                viewSignature.visibility = if(isChecked) View.VISIBLE else View.GONE
                viewToolbar.visibility = if(isChecked) View.VISIBLE else View.GONE
                textViewStatus.text = if(isChecked) context.resources.getString(R.string.on)
                                        else context.resources.getString(R.string.off)
                if(isChecked){
                    keyboardManager.showKeyboard(bodyEditText.view)
                }
                else{
                    keyboardManager.hideKeyboard()
                }
            }

            displaySignature(signature)
        }

        private fun displaySignature(signature: String) {
            bodyEditText.text = signature
            if(signature.isNotEmpty()){
                swicthStatus.isChecked = true
                textViewStatus.text = context.resources.getString(R.string.on)
            }
        }

        override fun showMessage(message: UIMessage) {
            val duration = Toast.LENGTH_LONG
            val toast = Toast.makeText(
                    context,
                    context.getLocalizedUIMessage(message),
                    duration)
            toast.show()
        }

        override fun getSignature(): String {
            return if(swicthStatus.isChecked) bodyEditText.text else ""
        }

    }

}