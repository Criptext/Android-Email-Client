package com.criptext.mail.scenes.settings.aliases.data

import android.view.View
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.criptext.mail.R


class AliasHolder(val view: View) : RecyclerView.ViewHolder(view) {

    private val rootView: RelativeLayout
    private val textViewDomainName: TextView
    private val imageTrashDomain: ImageView
    private val switchActiveAlias: Switch

    init {
        rootView = view.findViewById(R.id.alias_root_view) as RelativeLayout
        textViewDomainName = view.findViewById(R.id.alias_name) as TextView
        imageTrashDomain = view.findViewById(R.id.trash_alias_item) as ImageView
        switchActiveAlias = view.findViewById(R.id.switch_alias_item) as Switch
    }

    fun bindAlias(aliasItem: AliasItem){
        textViewDomainName.text = aliasItem.name
        switchActiveAlias.isChecked = aliasItem.isActive
    }

    fun setOnClickListener(onClick: () -> Boolean){
        imageTrashDomain.setOnClickListener {
            onClick()
        }
    }

    fun setOnSwitchListener(onSwitched: (Boolean) -> Unit){
        switchActiveAlias.setOnCheckedChangeListener {_, isChecked ->
            onSwitched(isChecked)
        }
    }
}
