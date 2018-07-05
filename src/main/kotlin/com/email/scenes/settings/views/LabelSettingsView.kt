package com.email.scenes.settings.views

import android.support.v7.widget.RecyclerView
import android.view.View
import com.email.R
import com.email.scenes.settings.SettingsUIObserver
import com.email.scenes.settings.labels.LabelWrapperAdapter
import com.email.scenes.settings.labels.VirtualLabelWrapperList
import com.email.utils.ui.TabView
import com.email.utils.virtuallist.VirtualListView
import com.email.utils.virtuallist.VirtualRecyclerView

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
    
}