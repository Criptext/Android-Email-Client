package com.email.scenes.settings

import android.support.design.widget.TabLayout
import android.support.v4.app.FragmentManager
import android.support.v4.view.ViewPager
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.email.R
import com.email.scenes.settings.devices.VirtualDeviceList
import com.email.scenes.settings.views.DevicesSettingsView
import com.email.scenes.settings.views.GeneralSettingsView
import com.email.scenes.settings.views.LabelSettingsView
import com.email.scenes.settings.labels.VirtualLabelWrapperList
import com.email.utils.UIMessage
import com.email.utils.getLocalizedUIMessage
import com.email.utils.ui.ViewPagerAdapter

interface SettingsScene{

    fun attachView(name: String, model: SettingsModel, settingsUIObserver: SettingsUIObserver)
    fun showMessage(message : UIMessage)
    fun showProfileNameDialog(fullName: String)

    var settingsUIObserver: SettingsUIObserver?

    class Default(private val view: View, private val fragmentManager: FragmentManager): SettingsScene{

        private val context = view.context

        private val mViewPager: ViewPager by lazy {
            view.findViewById<ViewPager>(R.id.viewpager)
        }

        private val tabs: TabLayout by lazy {
            view.findViewById<TabLayout>(R.id.sliding_tabs)
        }

        private val backButton: ImageView by lazy {
            view.findViewById<ImageView>(R.id.mailbox_back_button)
        }

        private val settingsProfileNameDialog = SettingsProfileNameDialog(context)

        override var settingsUIObserver: SettingsUIObserver? = null

        override fun attachView(name: String, model: SettingsModel,
                                settingsUIObserver: SettingsUIObserver){

            this.settingsUIObserver = settingsUIObserver
            loadTabs(name, model)
            backButton.setOnClickListener {
                settingsUIObserver.onBackButtonPressed()
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

        override fun showProfileNameDialog(fullName: String) {
            settingsProfileNameDialog.showProfileNameDialog(fullName, settingsUIObserver)
        }

        private fun loadTabs(name: String, model: SettingsModel) {
            setupViewPager(mViewPager, name, model)
            tabs.setupWithViewPager(mViewPager)
        }

        private fun setupViewPager(viewPager: ViewPager, name: String, model: SettingsModel) {

            val adapter = ViewPagerAdapter()
            val generalView = GeneralSettingsView(view.findViewById(R.id.viewSettingsGeneral),
                    context.getString(R.string.general))
            generalView.setExternalListeners(settingsUIObserver)
            adapter.addView(generalView)

            val labelView = LabelSettingsView(view.findViewById(R.id.viewSettingsLabels),
                    context.getString(R.string.labels))
            labelView.initView(VirtualLabelWrapperList(model), settingsUIObserver)
            adapter.addView(labelView)

            val deviceView = DevicesSettingsView(view.findViewById(R.id.viewSettingsDevices),
                    context.getString(R.string.devices))
            deviceView.initView(VirtualDeviceList(model))
            adapter.addView(deviceView)
            viewPager.offscreenPageLimit = 2
            viewPager.adapter = adapter
        }

    }

}