package com.criptext.mail.scenes.settings.custom_domain.data

import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.criptext.mail.R


class DomainHolder(val view: View) : RecyclerView.ViewHolder(view) {

    private val rootView: RelativeLayout
    private val textViewDomainName: TextView
    private val deviceLayout: FrameLayout
    private val imageTrashDomain: ImageView

    init {
        rootView = view.findViewById(R.id.domain_root_view) as RelativeLayout
        textViewDomainName = view.findViewById(R.id.textViewDomainName) as TextView
        deviceLayout = view.findViewById(R.id.domainItem) as FrameLayout
        imageTrashDomain = view.findViewById(R.id.imageViewTrashDomain) as ImageView
    }

    fun bindDevice(domainItem: DomainItem){
        textViewDomainName.text = domainItem.name
    }

    fun setOnClickListener(onClick: () -> Boolean){
        imageTrashDomain.setOnClickListener {
            onClick()
        }
    }
}
