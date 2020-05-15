package com.criptext.mail.scenes.settings.custom_domain.data

import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.criptext.mail.R
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage


class DomainHolder(val view: View) : RecyclerView.ViewHolder(view) {

    private val rootView: RelativeLayout
    private val textViewDomainName: TextView
    private val buttonVerify: TextView
    private val textViewVerificationState: TextView
    private val deviceLayout: FrameLayout
    private val imageTrashDomain: ImageView

    init {
        rootView = view.findViewById(R.id.domain_root_view) as RelativeLayout
        textViewDomainName = view.findViewById(R.id.textViewDomainName) as TextView
        buttonVerify = view.findViewById(R.id.verifyButton) as TextView
        textViewVerificationState = view.findViewById(R.id.verifiedText) as TextView
        deviceLayout = view.findViewById(R.id.domainItem) as FrameLayout
        imageTrashDomain = view.findViewById(R.id.imageViewTrashDomain) as ImageView
    }

    fun bindDomain(domainItem: DomainItem){
        textViewDomainName.text = domainItem.name
        if(domainItem.validated){
            textViewVerificationState.text = view.context.getLocalizedUIMessage(UIMessage(R.string.domain_verified))
            textViewVerificationState.setTextColor(ContextCompat.getColor(view.context, R.color.current_device))
            buttonVerify.isEnabled = false
            buttonVerify.visibility = View.INVISIBLE
        } else {
            textViewVerificationState.text = view.context.getLocalizedUIMessage(UIMessage(R.string.domain_not_verified))
            textViewVerificationState.setTextColor(ContextCompat.getColor(view.context, R.color.design_default_color_error))
        }
    }

    fun setValidateClickListener(onClick: () -> Unit){
        buttonVerify.setOnClickListener {
            onClick()
        }
    }

    fun setTrashClickListener(onClick: () -> Boolean){
        imageTrashDomain.setOnClickListener {
            onClick()
        }
    }
}
