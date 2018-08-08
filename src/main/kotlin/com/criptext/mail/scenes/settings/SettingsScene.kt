package com.criptext.mail.scenes.settings

import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.criptext.mail.R
import com.criptext.mail.scenes.settings.devices.VirtualDeviceList
import com.criptext.mail.scenes.settings.views.DevicesSettingsView
import com.criptext.mail.scenes.settings.views.GeneralSettingsView
import com.criptext.mail.scenes.settings.views.LabelSettingsView
import com.criptext.mail.scenes.settings.labels.VirtualLabelWrapperList
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.ui.ViewPagerAdapter
import com.criptext.mail.utils.virtuallist.VirtualListView

interface SettingsScene{

    fun attachView(name: String, model: SettingsModel, settingsUIObserver: SettingsUIObserver)
    fun showMessage(message : UIMessage)
    fun showProfileNameDialog(fullName: String)
    fun showLogoutDialog()
    fun showLoginOutDialog()
    fun dismissLoginOutDialog()
    fun showCreateLabelDialog(keyboardManager: KeyboardManager)
    fun getLabelListView(): VirtualListView

    var settingsUIObserver: SettingsUIObserver?

    class Default(private val view: View): SettingsScene{

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

        private val labelView: LabelSettingsView by lazy {
            LabelSettingsView(view.findViewById(R.id.viewSettingsLabels),
                    context.getString(R.string.labels))
        }

        private val settingsProfileNameDialog = SettingsProfileNameDialog(context)
        private val settingCustomLabelDialog = SettingsCustomLabelDialog(context)
        private val settingLogoutDialog = SettingsLogoutDialog(context)
        private val settingLoginOutDialog = SettingsLoginOutDialog(context)

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

        override fun showCreateLabelDialog(keyboardManager: KeyboardManager) {
            settingCustomLabelDialog.showCustomLabelDialog(settingsUIObserver, keyboardManager)
        }

        override fun showLogoutDialog() {
            settingLogoutDialog.showLogoutDialog(settingsUIObserver)
        }

        override fun showLoginOutDialog() {
            settingLoginOutDialog.showLoginOutDialog(settingsUIObserver)
        }

        override fun dismissLoginOutDialog() {
            settingLoginOutDialog.dismiss()
        }

        override fun getLabelListView(): VirtualListView {
            return labelView.getListView()
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