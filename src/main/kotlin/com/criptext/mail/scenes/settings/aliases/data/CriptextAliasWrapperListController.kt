package com.criptext.mail.scenes.settings.aliases.data

import com.criptext.mail.scenes.settings.aliases.AliasesModel
import com.criptext.mail.utils.virtuallist.VirtualListView

class CriptextAliasWrapperListController(
        private val model: AliasesModel,
        private val listView: VirtualListView?){

    fun remove(position: Int) {
        model.criptextAliases.removeAt(position)
        listView?.notifyDataSetChanged()
    }

    fun add(alias: AliasItem) {
        model.criptextAliases.add(alias)
        listView?.notifyDataSetChanged()
    }

    fun addAll(aliases: List<AliasItem>) {
        model.criptextAliases.addAll(aliases)
        listView?.notifyDataSetChanged()
    }

    fun updateActive(position: Int, enable: Boolean){
        model.criptextAliases[position] = model.criptextAliases[position].copy(isActive = enable)
    }

    fun update() {
        listView?.notifyDataSetChanged()
    }

}