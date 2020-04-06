package com.criptext.mail.scenes.settings.aliases

import android.view.View
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.db.models.Contact
import com.criptext.mail.scenes.settings.AliasListItemListener
import com.criptext.mail.scenes.settings.SettingsAddAliasDialog
import com.criptext.mail.scenes.settings.SettingsRemoveAliasDialog
import com.criptext.mail.scenes.settings.aliases.data.AliasAdapter
import com.criptext.mail.scenes.settings.aliases.data.VirtualAliasList
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.ui.*
import com.criptext.mail.utils.ui.data.DialogData
import com.criptext.mail.utils.ui.data.DialogType
import com.criptext.mail.utils.uiobserver.UIObserver
import com.criptext.mail.utils.virtuallist.VirtualListView
import com.criptext.mail.utils.virtuallist.VirtualRecyclerView


interface AliasesScene{

    fun attachView(aliasesUIObserver: AliasesUIObserver,
                   keyboardManager: KeyboardManager, model: AliasesModel)
    fun showMessage(message: UIMessage)
    fun showConfirmPasswordDialog(observer: UIObserver)
    fun dismissConfirmPasswordDialog()
    fun setConfirmPasswordError(message: UIMessage)
    fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo)
    fun showSyncDeviceAuthConfirmation(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo)
    fun dismissLinkDeviceDialog()
    fun dismissSyncDeviceDialog()
    fun showAccountSuspendedDialog(observer: UIObserver, email: String, dialogType: DialogType)
    fun dismissAccountSuspendedDialog()
    fun getCriptextAliasesListView(): VirtualListView
    fun getCustomAliasesListView(): VirtualListView
    fun showRemoveAliasDialog(aliasName: String, domainName: String?, position: Int)
    fun setupAliasesFromModel(model: AliasesModel,
                              aliasListItemListener: AliasListItemListener)
    fun setAddAliasDialogError(message: UIMessage?)
    fun showAddAliasDialog(domains: List<String>)
    fun addAliasDialogToggleLoad(isLoading: Boolean)
    fun addAliasDialogDismiss()
    fun deleteAliasDialogDismiss()
    fun showCriptextAliasDeleteRestrictionDialog(message: UIMessage)
    fun addButtonEnable(enabled: Boolean)
    fun showProgressBar(show: Boolean)

    class Default(val view: View): AliasesScene{
        private lateinit var uiObserver: AliasesUIObserver

        private val context = view.context

        private val backButton: ImageView by lazy {
            view.findViewById<ImageView>(R.id.mailbox_back_button)
        }

        private val hintButton: ImageView by lazy {
            view.findViewById<ImageView>(R.id.info)
        }

        private val recyclerViewCriptextAliases: RecyclerView by lazy {
            view.findViewById<RecyclerView>(R.id.recyclerViewCriptextAliases)
        }

        private val recyclerViewCustomAliases: RecyclerView by lazy {
            view.findViewById<RecyclerView>(R.id.recyclerViewCustomAliases)
        }

        private val criptextDomainHeader: TextView by lazy {
            view.findViewById<TextView>(R.id.criptext_domain_header)
        }

        private val customDomainHeader: TextView by lazy {
            view.findViewById<TextView>(R.id.custom_domain_header)
        }

        private val criptextAliasesLayout: LinearLayout by lazy {
            view.findViewById<LinearLayout>(R.id.criptext_aliases_layout)
        }

        private val customAliasesLayout: LinearLayout by lazy {
            view.findViewById<LinearLayout>(R.id.custom_domain_layout)
        }

        private val addAliasButon: TextView by lazy {
            view.findViewById<TextView>(R.id.add_alias_button)
        }

        private val aliasLoadProgress: ProgressBar by lazy {
            view.findViewById<ProgressBar>(R.id.aliasLoadProgress)
        }

        private val confirmPassword = ConfirmPasswordDialog(context)
        private val linkAuthDialog = LinkNewDeviceAlertDialog(context)
        private val syncAuthDialog = SyncDeviceAlertDialog(context)
        private val accountSuspended = AccountSuspendedDialog(context)
        private val settingRemoveAliasDialog = SettingsRemoveAliasDialog(context)
        private val settingAddedAliasDialog = SettingsAddAliasDialog(context)

        private val criptextAliasesListView: VirtualListView = VirtualRecyclerView(recyclerViewCriptextAliases)
        private val customAliasesListView: VirtualListView = VirtualRecyclerView(recyclerViewCustomAliases)

        override fun attachView(aliasesUIObserver: AliasesUIObserver, keyboardManager: KeyboardManager,
                                model: AliasesModel) {
            this.uiObserver = aliasesUIObserver

            backButton.setOnClickListener {
                this.uiObserver.onBackButtonPressed()
            }
            addAliasButon.setOnClickListener {
                this.uiObserver.onAddAliasButtonPressed()
            }

            hintButton.setOnClickListener {
                GeneralMessageOkDialog(
                        context = view.context,
                        dialogData = DialogData.DialogMessageData(
                                title = UIMessage(R.string.aliases_dialog_title),
                                type = DialogType.Message(),
                                message = listOf(UIMessage(R.string.aliases_dialog_message)),
                                onOkPress = {}
                        )
                ).showDialog()
            }
        }

        override fun setupAliasesFromModel(model: AliasesModel, aliasListItemListener: AliasListItemListener) {
            if(model.criptextAliases.isEmpty()){
                criptextAliasesLayout.visibility = View.GONE
            } else {
                criptextAliasesLayout.visibility = View.VISIBLE
                criptextDomainHeader.text = Contact.mainDomain
                criptextAliasesListView.setAdapter(AliasAdapter(view.context, aliasListItemListener, VirtualAliasList(model.criptextAliases)))
            }
            if(model.domains.isEmpty() || model.domains.first().aliases.isEmpty()){
                customAliasesLayout.visibility = View.GONE
            } else {
                customAliasesLayout.visibility = View.VISIBLE
                customDomainHeader.text = model.domains.first().name
                customAliasesListView.setAdapter(AliasAdapter(view.context, aliasListItemListener, VirtualAliasList(model.domains.first().aliases)))
            }
            if(criptextAliasesLayout.visibility == View.GONE && customAliasesLayout.visibility == View.GONE)
                addAliasButon.text = context.getLocalizedUIMessage(UIMessage(R.string.aliases_create_button))
            else
                addAliasButon.text = context.getLocalizedUIMessage(UIMessage(R.string.aliases_create_button_add))
        }

        override fun setAddAliasDialogError(message: UIMessage?) {
            settingAddedAliasDialog.setError(message)
        }


        override fun showRemoveAliasDialog(aliasName: String, domainName: String?, position: Int) {
            settingRemoveAliasDialog.showRemoveAliasDialog(uiObserver, aliasName, domainName, position)
        }

        override fun showAddAliasDialog(domains: List<String>) {
            settingAddedAliasDialog.showAddAliasDialog(uiObserver, domains)
        }

        override fun getCriptextAliasesListView(): VirtualListView {
            return criptextAliasesListView
        }

        override fun getCustomAliasesListView(): VirtualListView {
            return customAliasesListView
        }

        override fun addAliasDialogToggleLoad(isLoading: Boolean) {
            settingAddedAliasDialog.toggleLoad(isLoading)
        }

        override fun addAliasDialogDismiss() {
            settingAddedAliasDialog.dismissDialog()
        }

        override fun deleteAliasDialogDismiss() {
            settingRemoveAliasDialog.dismissDialog()
        }

        override fun addButtonEnable(enabled: Boolean) {
            addAliasButon.isEnabled = enabled
            addAliasButon.isClickable = enabled
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

        override fun showConfirmPasswordDialog(observer: UIObserver) {
            confirmPassword.showDialog(observer)
        }

        override fun dismissConfirmPasswordDialog() {
            confirmPassword.dismissDialog()
        }

        override fun setConfirmPasswordError(message: UIMessage) {
            confirmPassword.setPasswordError(message)
        }

        override fun showProgressBar(show: Boolean) {
            aliasLoadProgress.visibility = if(show) View.VISIBLE else View.GONE
        }

        override fun showCriptextAliasDeleteRestrictionDialog(message: UIMessage) {
            GeneralMessageOkDialog(context,
                    DialogData.DialogMessageData(
                            title = UIMessage(R.string.delete_criptext_alias_wait_title),
                            message = listOf(message),
                            onOkPress = {},
                            type = DialogType.Message()
                    )
            ).showDialog()
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