package com.criptext.mail.scenes.emaildetail.ui.holders


import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.criptext.mail.R
import com.criptext.mail.scenes.emaildetail.ui.FullEmailListAdapter
import com.criptext.mail.utils.Utility
import com.criptext.mail.utils.getColorFromAttr
import de.hdodenhof.circleimageview.CircleImageView

class CollapsedViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

    val collapsedView: View = view.findViewById(R.id.collapsed_view)
    val circleImage: CircleImageView = view.findViewById(R.id.collapsed_mails_number)

    fun setListeners(emailListener: FullEmailListAdapter.OnFullEmailEventListener?) {
        collapsedView.setOnClickListener {
            emailListener?.onCollapsedClicked()
        }
    }

    fun setNumber(number: Int){
        circleImage.setImageBitmap(
            Utility.getBitmapFromNumber(number, 250, 250,
                    view.context.getColorFromAttr(R.attr.criptextEmailDetailBgColor),
                    view.context.getColorFromAttr(R.attr.criptextCollapsedEmailsNumber))
        )

    }

}
