package com.criptext.mail.utils.ui

import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.criptext.mail.R
import com.criptext.mail.api.Hosts
import com.criptext.mail.utils.EmailAddressUtils
import com.criptext.mail.utils.UIUtils
import com.criptext.mail.utils.Utility
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.ui.data.DialogData
import com.criptext.mail.utils.ui.data.DialogResult
import com.criptext.mail.utils.ui.data.DialogType
import com.criptext.mail.utils.uiobserver.UIObserver
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class YayDialog(val context: Context, val email: String) {

    private var dialog: AlertDialog? = null
    private val res = context.resources
    private lateinit var btnYay: Button

    private lateinit var view: View

    fun showDialog(observer: UIObserver?) {

        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        view = inflater.inflate(R.layout.yay_dialog, null)
        view.findViewById<TextView>(R.id.email).text = email

        dialogBuilder.setView(view)

        dialog = createDialog(view, dialogBuilder, observer)
    }

    private fun createDialog(dialogView: View, dialogBuilder: AlertDialog.Builder,
                             observer: UIObserver?): AlertDialog {

        val width = res.getDimension(R.dimen.password_login_dialog_width).toInt()
        val newLogoutDialog = dialogBuilder.create()
        val window = newLogoutDialog.window
        newLogoutDialog.show()
        window?.setLayout(width, RelativeLayout.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER_VERTICAL)
        val drawableBackground = ContextCompat.getDrawable(dialogView.context,
                R.drawable.dialog_label_chooser_shape)
        newLogoutDialog.window?.setBackgroundDrawable(drawableBackground)
        newLogoutDialog.setCancelable(false)
        newLogoutDialog.setCanceledOnTouchOutside(false)

        val avatar = dialogView.findViewById<CircleImageView>(R.id.profile_picture)
        val emailTextView = dialogView.findViewById<TextView>(R.id.email)
        emailTextView.text = email
        val domain = EmailAddressUtils.extractEmailAddressDomain(email)
        val recipientId = EmailAddressUtils.extractRecipientIdFromAddress(email, domain)
        val urlAvatar = Hosts.restApiBaseUrl.plus("/user/avatar/$domain/$recipientId")
        Picasso.get()
                .load(urlAvatar)
                .placeholder(R.drawable.img_profile)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(avatar, object : Callback {
                    override fun onSuccess() {

                    }

                    override fun onError(e: Exception) {
                        Picasso.get()
                                .load(urlAvatar)
                                .placeholder(R.drawable.img_profile)
                                .into(avatar, object : Callback {
                                    override fun onSuccess() {

                                    }

                                    override fun onError(e: Exception) {
                                        avatar.setImageResource(R.drawable.img_profile)
                                    }
                                })
                    }
                })

        assignButtonEvents(dialogView, newLogoutDialog)


        return newLogoutDialog
    }

    private fun assignButtonEvents(view: View, dialog: AlertDialog) {

        btnYay = view.findViewById(R.id.btn_ok) as Button

        btnYay.setOnClickListener {
            dialog.dismiss()
        }
    }

    fun dismissDialog(){
        dialog?.dismiss()
    }
}
