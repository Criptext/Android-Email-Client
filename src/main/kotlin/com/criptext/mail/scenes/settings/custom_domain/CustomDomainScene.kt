package com.criptext.mail.scenes.settings.custom_domain

import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.widget.AppCompatEditText
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.scenes.settings.DomainListItemListener
import com.criptext.mail.scenes.settings.SettingsRemoveDomainDialog
import com.criptext.mail.scenes.settings.custom_domain.data.DomainAdapter
import com.criptext.mail.scenes.settings.custom_domain.data.VirtualDomainList
import com.criptext.mail.scenes.settings.recovery_email.holders.FormInputViewHolder
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.ui.*
import com.criptext.mail.utils.ui.data.DialogData
import com.criptext.mail.utils.ui.data.DialogType
import com.criptext.mail.utils.uiobserver.UIObserver
import com.criptext.mail.utils.virtuallist.VirtualListView
import com.criptext.mail.utils.virtuallist.VirtualRecyclerView
import com.criptext.mail.validation.FormInputState


interface CustomDomainScene{

    fun attachView(customDomainEntryUIObserver: CustomDomainUIObserver,
                   keyboardManager: KeyboardManager, model: CustomDomainModel,
                   domainsListItemListener: DomainListItemListener)
    fun showMessage(message: UIMessage)
    fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo)
    fun showSyncDeviceAuthConfirmation(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo)
    fun dismissLinkDeviceDialog()
    fun dismissSyncDeviceDialog()
    fun showAccountSuspendedDialog(observer: UIObserver, email: String, dialogType: DialogType)
    fun dismissAccountSuspendedDialog()
    fun getDomainListView(): VirtualListView
    fun showRemoveDomainDialog(domainName: String, position: Int)
    fun showProgressBar(show: Boolean)

    class Default(val view: View): CustomDomainScene{
        private lateinit var uiObserver: CustomDomainUIObserver

        private val context = view.context

        private val backButton: ImageView by lazy {
            view.findViewById<ImageView>(R.id.mailbox_back_button)
        }

        private val hintButton: ImageView by lazy {
            view.findViewById<ImageView>(R.id.info)
        }

        private val recyclerViewDomains: RecyclerView by lazy {
            view.findViewById<RecyclerView>(R.id.recyclerViewDomains)
        }

        private val customDomainLoadProgress: ProgressBar by lazy {
            view.findViewById<ProgressBar>(R.id.customDomainsLoadProgress)
        }

        private val linkAuthDialog = LinkNewDeviceAlertDialog(context)
        private val syncAuthDialog = SyncDeviceAlertDialog(context)
        private val accountSuspended = AccountSuspendedDialog(context)
        private val settingRemoveDomainDialog = SettingsRemoveDomainDialog(context)

        private val domainsListView: VirtualListView = VirtualRecyclerView(recyclerViewDomains)

        override fun attachView(customDomainEntryUIObserver: CustomDomainUIObserver, keyboardManager: KeyboardManager,
                                model: CustomDomainModel, domainsListItemListener: DomainListItemListener) {
            this.uiObserver = customDomainEntryUIObserver

            backButton.setOnClickListener {
                this.uiObserver.onBackButtonPressed()
            }

            hintButton.setOnClickListener {
                GeneralMessageOkDialog(
                        context = view.context,
                        dialogData = DialogData.DialogMessageData(
                                title = UIMessage(R.string.title_custom_domain),
                                type = DialogType.Message(),
                                message = listOf(UIMessage(R.string.custom_domain_help)),
                                onOkPress = {}
                        )
                ).showDialog()
            }

            domainsListView.setAdapter(DomainAdapter(view.context, domainsListItemListener, VirtualDomainList(model)))
        }

        override fun showRemoveDomainDialog(domainName: String, position: Int) {
            settingRemoveDomainDialog.showRemoveDomainDialog(uiObserver, domainName, position)
        }

        override fun getDomainListView(): VirtualListView {
            return domainsListView
        }

        override fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {
            if(linkAuthDialog.isShowing() != null && linkAuthDialog.isShowing() == false)
                linkAuthDialog.showLinkDeviceAuthDialog(uiObserver, untrustedDeviceInfo)
            else if(linkAuthDialog.isShowing() == null)
                linkAuthDialog.showLinkDeviceAuthDialog(uiObserver, untrustedDeviceInfo)
        }

        override fun showSyncDeviceAuthConfirmation(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {
            if(syncAuthDialog.isShowing() != null && syncAuthDialog.isShowing() == false)
                syncAuthDialog.showLinkDeviceAuthDialog(uiObserver, trustedDeviceInfo)
            else if(syncAuthDialog.isShowing() == null)
                syncAuthDialog.showLinkDeviceAuthDialog(uiObserver, trustedDeviceInfo)
        }

        override fun dismissLinkDeviceDialog() {
            linkAuthDialog.dismiss()
        }

        override fun dismissSyncDeviceDialog() {
            syncAuthDialog.dismiss()
        }

        override fun dismissAccountSuspendedDialog() {
            accountSuspended.dismissDialog()
        }

        override fun showAccountSuspendedDialog(observer: UIObserver, email: String, dialogType: DialogType) {
            accountSuspended.showDialog(observer, email, dialogType)
        }

        override fun showProgressBar(show: Boolean) {
            customDomainLoadProgress.visibility = if(show) View.VISIBLE else View.GONE
        }

        override fun showMessage(message: UIMessage) {
            val duration = Toast.LENGTH_LONG
            val toast = Toast.makeText(
                    context,
                    context.getLocalizedUIMessage(message),
                    duration)
            toast.show()
        }
    }

}