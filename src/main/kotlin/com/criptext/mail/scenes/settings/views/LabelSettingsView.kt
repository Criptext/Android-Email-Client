package com.criptext.mail.scenes.settings.views

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.criptext.mail.R
import com.criptext.mail.scenes.settings.SettingsUIObserver
import com.criptext.mail.scenes.settings.labels.LabelWrapperAdapter
import com.criptext.mail.scenes.settings.labels.VirtualLabelWrapperList
import com.criptext.mail.utils.ui.TabView
import com.criptext.mail.utils.virtuallist.VirtualListView
import com.criptext.mail.utils.virtuallist.VirtualRecyclerView

class LabelSettingsView(view: View, title: String): TabView(view, title){

    private lateinit var recyclerViewLabels: RecyclerView
    private lateinit var labelListView: VirtualListView

    override fun onCreateView(){

        recyclerViewLabels = view.findViewById(R.id.recyclerViewLabels)
        labelListView = VirtualRecyclerView(recyclerViewLabels)

    }

    fun initView(virtualLabelWrapperList: VirtualLabelWrapperList, settingsUIObserver: SettingsUIObserver?){
        labelListView.setAdapter(LabelWrapperAdapter(view.context, settingsUIObserver, virtualLabelWrapperList))
    }

    fun getListView(): VirtualListView{
        return labelListView
    }
    
}