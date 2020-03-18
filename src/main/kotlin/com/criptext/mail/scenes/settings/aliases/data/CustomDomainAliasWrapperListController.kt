package com.criptext.mail.scenes.settings.aliases.data

import com.criptext.mail.scenes.settings.aliases.AliasesModel
import com.criptext.mail.utils.virtuallist.VirtualListView

class CustomDomainAliasWrapperListController(
        private val model: AliasesModel,
        private val listView: VirtualListView?){

    fun updateActive(position: Int, domain: String, enable: Boolean){
        val index = model.domains.indexOfFirst { it.name == domain }
        if(index > -1) {
            model.domains[index].aliases[position] = model.domains[index].aliases[position].copy(isActive = enable)
            listView?.notifyDataSetChanged()
        }
    }

    fun remove(position: Int, domain: String) {
        val index = model.domains.indexOfFirst { it.name == domain }
        if(index > -1) {
            model.domains[index].aliases.removeAt(position)
            listView?.notifyDataSetChanged()
        }
    }

    fun add(domain: String, aliasItem: AliasItem) {
        val index = model.domains.indexOfFirst { it.name == domain }
        if(index > -1) {
            model.domains[index].aliases.add(aliasItem)
            listView?.notifyDataSetChanged()
        }
    }

    fun update() {
        listView?.notifyDataSetChanged()
    }

}