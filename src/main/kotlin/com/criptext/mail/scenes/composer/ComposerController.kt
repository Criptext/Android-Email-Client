package com.criptext.mail.scenes.composer


import android.Manifest
import android.content.pm.PackageManager
import android.view.View
import com.criptext.mail.BaseActivity
import com.criptext.mail.ExternalActivityParams
import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Contact
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.composer.data.*
import com.criptext.mail.scenes.composer.ui.ComposerUIObserver
import com.criptext.mail.scenes.params.EmailDetailParams
import com.criptext.mail.scenes.params.LinkingParams
import com.criptext.mail.scenes.params.MailboxParams
import com.criptext.mail.scenes.params.SignInParams
import com.criptext.mail.utils.*
import com.criptext.mail.utils.file.FileUtils
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.ui.data.DialogResult
import com.criptext.mail.utils.ui.data.DialogType
import java.io.File
import java.util.*


/**
 * Created by gabriel on 2/26/18.
 */

class ComposerController(private val storage: KeyValueStorage,
                         private val model: ComposerModel,
                         private val scene: ComposerScene,
                         private val host: IHostActivity,
                         private var activeAccount: ActiveAccount,
                         private val generalDataSource: GeneralDataSource,
                         private val dataSource: ComposerDataSource)
    : SceneController() {

    private val dataSourceController = DataSourceController(dataSource)
    private val observer = object: ComposerUIObserver {
        override fun onSenderSelectedItem(sender: String) {
            model.selectedAccount = model.accounts.find { it.userEmail == sender }
        }

        override fun onSnackbarClicked() {

        }

        override fun onSyncAuthConfirmed(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {

        }

        override fun onSyncAuthDenied(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {


        }

        override fun onGeneralOkButtonPressed(result: DialogResult) {
            when(result){
                is DialogResult.DialogConfirmation -> {
                    when(result.type){
                        is DialogType.SwitchAccount -> {
                            generalDataSource.submitRequest(GeneralRequest.ChangeToNextAccount())
                        }
                        is DialogType.SignIn ->
                            host.goToScene(SignInParams(true), true)
                    }
                }
            }
        }

        override fun leaveComposer() {
            checkForDraft()
        }

        override fun onLinkAuthConfirmed(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {
            generalDataSource.submitRequest(GeneralRequest.LinkAccept(untrustedDeviceInfo))
        }

        override fun onLinkAuthDenied(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {
            generalDataSource.submitRequest(GeneralRequest.LinkDenied(untrustedDeviceInfo))
        }

        override fun onNewCamAttachmentRequested() {
            PinLockUtils.setPinLockTimeout(PinLockUtils.TIMEOUT_TO_DISABLE)
            host.launchExternalActivityForResult(ExternalActivityParams.Camera())
        }

        override fun onNewFileAttachmentRequested() {
            PinLockUtils.setPinLockTimeout(PinLockUtils.TIMEOUT_TO_DISABLE)
            host.launchExternalActivityForResult(ExternalActivityParams.FilePicker())
        }

        override fun onNewGalleryAttachmentRequested() {
            PinLockUtils.setPinLockTimeout(PinLockUtils.TIMEOUT_TO_DISABLE)
            host.launchExternalActivityForResult(ExternalActivityParams.ImagePicker())
        }

        override fun sendDialogButtonPressed() {
            val data = scene.getDataInputByUser()
            updateModelWithInputData(data)

            if(isReadyForSending())
                saveEmailAsDraft(data, onlySave = false)
        }

        override fun sendDialogCancelPressed() {
            model.passwordText = ""
        }

        override fun showStartGuideAttachments(view: View) {
            host.showStartGuideView(
                    view,
                    R.string.start_guide_secure_attachments,
                    R.dimen.focal_padding_attachments
            )
        }

        override fun onAttachmentRemoveClicked(position: Int) {
            val file = File(model.attachments[position].filepath)
            model.filesSize -= file.length()
            model.attachments.removeAt(position)
            scene.notifyAttachmentSetChanged()
        }

        override fun onSelectedEditTextChanged(userIsEditingRecipients: Boolean) {
            scene.toggleExtraFieldsVisibility(visible = userIsEditingRecipients)
        }

        override fun onRecipientAdded() {
            if(!model.initialized) return
            val data = scene.getDataInputByUser()
            if(model.to.size < data.to.size || model.cc.size < data.cc.size || model.bcc.size < data.bcc.size) {
                updateModelWithInputData(data)
                host.refreshToolbarItems()
                val emails = data.to.map { it.email }.plus(data.cc.map { it.email }).plus(data.bcc.map { it.email })
                if(!model.checkedDomains.map { it.name }.containsAll(emails.map { EmailAddressUtils.extractEmailAddressDomain(it) })){
                    if(emails.map { EmailAddressUtils.extractEmailAddressDomain(it) }.isNotEmpty())
                        dataSource.submitRequest(ComposerRequest.CheckDomain(emails.distinct()))
                }
            }
        }

        override fun onRecipientListChanged() {
            if(!model.initialized) return
            val data = scene.getDataInputByUser()
            if(data.to.size < model.to.size || data.cc.size < model.cc.size || data.bcc.size < model.bcc.size) {
                updateModelWithInputData(data)
                host.refreshToolbarItems()
            }

        }

        override fun onAttachmentButtonClicked() {
            if(host.checkPermissions(BaseActivity.RequestCode.writeAccess.ordinal,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                scene.showAttachmentsBottomDialog(this)
            }
        }

        override fun onBackButtonClicked() {
            if(model.isUploadingAttachments) {
                if(model.attachments.isEmpty())
                    checkForDraft()
                else
                    scene.showStayInComposerDialog(this)
            }else{
                checkForDraft()
            }
        }

        override fun onOkButtonPressed(password: String) {
            generalDataSource.submitRequest(GeneralRequest.ConfirmPassword(password))
        }

        override fun onCancelButtonPressed() {
            generalDataSource.submitRequest(GeneralRequest.DeviceRemoved(true))
        }
    }

    private val generalDataSourceListener: (GeneralResult) -> Unit = { result ->
        when(result) {
            is GeneralResult.DeviceRemoved -> onDeviceRemovedRemotely(result)
            is GeneralResult.ConfirmPassword -> onPasswordChangedRemotely(result)
            is GeneralResult.LinkAccept -> onLinkAccept(result)
            is GeneralResult.GetRemoteFile -> onGetRemoteFile(result)
            is GeneralResult.ChangeToNextAccount -> onChangeToNextAccount(result)
        }
    }

    private val dataSourceListener: (ComposerResult) -> Unit = { result ->
        when(result) {
            is ComposerResult.GetAllContacts -> onContactsLoaded(result)
            is ComposerResult.GetAllFromAddresses -> onFromAddressesLoaded(result)
            is ComposerResult.SaveEmail -> onEmailSavesAsDraft(result)
            is ComposerResult.UploadFile -> onUploadFile(result)
            is ComposerResult.LoadInitialData -> onLoadedInitialData(result)
            is ComposerResult.CheckDomain -> onCheckDomain(result)
        }
    }

    private fun showSuspendedAccountDialog(){
        val jwtList = storage.getString(KeyValueStorage.StringKey.JWTS, "").split(",").map { it.trim() }
        val dialogType = if(jwtList.isNotEmpty() && jwtList.size > 1) DialogType.SwitchAccount()
        else DialogType.SignIn()
        scene.showAccountSuspendedDialog(observer, activeAccount.userEmail, dialogType)
    }

    private fun onLoadedInitialData(result: ComposerResult.LoadInitialData) {
        when (result) {
            is ComposerResult.LoadInitialData.Success -> {
                updateModelWithInputData(result.initialData)
                model.fileKey = result.initialData.fileKey
                if(result.initialData.attachments != null && result.initialData.attachments.isNotEmpty()) {
                    model.attachments = result.initialData.attachments
                }
                bindWithModel(result.initialData, activeAccount.signature)
                model.originalBody = model.body
                model.initialized = true
                host.refreshToolbarItems()
            }

            is ComposerResult.LoadInitialData.Failure -> {
                scene.showError(result.message)
            }
        }
    }

    private fun getCriptextContacts(contacts: LinkedList<Contact>, checkedData: List<ContactDomainCheckData>): List<Contact> {
        val isCriptext = contacts.map { it.email }
                .filter { email ->
                    EmailAddressUtils.extractEmailAddressDomain(email) in
                            checkedData.filter { it.isCriptextDomain }
                                    .map { it.name } }
        contacts.forEachIndexed { _, contact ->
            if(contact.email in isCriptext)
                contact.isCriptextDomain = true
        }
        return contacts
    }

    private fun onCheckDomain(result: ComposerResult.CheckDomain) {
        when (result) {
            is ComposerResult.CheckDomain.Success -> {
                model.checkedDomains.addAll(result.contactDomainCheck)
                model.checkedDomains = model.checkedDomains.distinctBy { it.name }.toMutableList()
                model.to = LinkedList(getCriptextContacts(model.to, model.checkedDomains))
                model.cc = LinkedList(getCriptextContacts(model.cc, model.checkedDomains))
                model.bcc = LinkedList(getCriptextContacts(model.bcc, model.checkedDomains))
                scene.contactsInputUpdate(
                        model.to,
                        model.cc,
                        model.bcc
                )
            }
        }
    }

    private fun onGetRemoteFile(result: GeneralResult.GetRemoteFile) {
        when (result) {
            is GeneralResult.GetRemoteFile.Success -> {
                scene.dismissPreparingFileDialog()
                model.attachments.addAll(result.remoteFiles.map { ComposerAttachment(UUID.randomUUID().toString(), it.first, it.second, model.fileKey!!) })
                scene.notifyAttachmentSetChanged()
                handleNextUpload()
            }
        }
    }

    private fun onChangeToNextAccount(result: GeneralResult.ChangeToNextAccount){
        when(result) {
            is GeneralResult.ChangeToNextAccount.Success -> {
                activeAccount = result.activeAccount
                generalDataSource.activeAccount = activeAccount
                dataSource.activeAccount = activeAccount
                scene.dismissAccountSuspendedDialog()

                scene.showMessage(UIMessage(R.string.snack_bar_active_account, arrayOf(activeAccount.userEmail)))

                host.exitToScene(MailboxParams(), null, false, true)
            }
        }
    }

    private fun onUploadFile(result: ComposerResult.UploadFile){
        when (result) {
            is ComposerResult.UploadFile.Register -> {
                val composerAttachment = getAttachmentByUUID(result.uuid) ?: return
                composerAttachment.filetoken = result.filetoken
            }
            is ComposerResult.UploadFile.Progress -> {
                val composerAttachment = getAttachmentByUUID(result.uuid) ?: return
                composerAttachment.uploadProgress = result.percentage
            }
            is ComposerResult.UploadFile.Success -> {
                val composerAttachment = getAttachmentByUUID(result.uuid)
                composerAttachment?.uploadProgress = 100
                model.filesSize = result.filesSize
                handleNextUpload()
            }
            is ComposerResult.UploadFile.Failure -> {
                removeAttachmentByUUID(result.uuid)
                scene.showAttachmentErrorDialog(result.filepath)
                handleNextUpload()
            }
            is ComposerResult.UploadFile.Unauthorized -> {
                generalDataSource.submitRequest(GeneralRequest.DeviceRemoved(false))
            }
            is ComposerResult.UploadFile.Forbidden -> {
                scene.showConfirmPasswordDialog(observer)
            }
            is ComposerResult.UploadFile.MaxFilesExceeds -> {
                removeAttachmentByUUID(result.uuid, true)
                model.filesExceedingMaxEmailSize.add(FileUtils.getName(result.filepath))
                scene.showMaxFilesExceedsDialog()
                handleNextUpload()
            }
            is ComposerResult.UploadFile.PayloadTooLarge -> {
                removeAttachmentByUUID(result.uuid)
                scene.showPayloadTooLargeDialog(result.filepath, result.headers.getLong("Max-Size"))
                handleNextUpload()
            }
            is ComposerResult.UploadFile.EnterpriseSuspended -> {
                showSuspendedAccountDialog()
            }
        }
        scene.notifyAttachmentSetChanged()
    }

    private fun getAttachmentByUUID(uuid: String): ComposerAttachment? {
        return model.attachments.firstOrNull{it.uuid == uuid}
    }

    private fun removeAttachmentByUUID(uuid: String, removeRemaining: Boolean = false) {
        if(!removeRemaining) {
            model.attachments.removeAll { it.uuid == uuid }
        } else {
            val attachment = model.attachments.firstOrNull { it.uuid == uuid } ?: return
            model.attachments.remove(attachment)
            model.attachments.removeAll(model.attachments.filter { it.uploadProgress == -1 })
        }
    }

    private fun onEmailSavesAsDraft(result: ComposerResult.SaveEmail) {
        when (result) {
            is ComposerResult.SaveEmail.Success -> {
                if(result.onlySave) {
                    host.exitToScene(MailboxParams(), ActivityMessage.DraftSaved(result.preview), false)
                } else {
                    val sendMailMessage = ActivityMessage.SendMail(emailId = result.emailId,
                            threadId = result.threadId,
                            composerInputData = result.composerInputData,
                            attachments = result.attachments, fileKey = model.fileKey,
                            senderAccount = model.selectedAccount)
                    host.exitToScene(MailboxParams(), sendMailMessage, false)
                }
            }
            is ComposerResult.SaveEmail.TooManyRecipients ->
                scene.showError(UIMessage(R.string.error_saving_too_many_recipients,
                        arrayOf(EmailUtils.RECIPIENT_LIMIT)))
            is ComposerResult.SaveEmail.Failure -> {
                scene.showError(UIMessage(R.string.error_saving_as_draft))
            }
        }
    }

    private fun onContactsLoaded(result: ComposerResult.GetAllContacts){
        when (result) {
            is ComposerResult.GetAllContacts.Success -> {
                scene.setContactSuggestionList(result.contacts)
            }
            is ComposerResult.GetAllContacts.Failure -> {
                scene.showError(result.message)
            }
        }
        if(storage.getBool(KeyValueStorage.StringKey.StartGuideShowAttachments, true)){
            scene.showStartGuideAttachments()
            storage.putBool(KeyValueStorage.StringKey.StartGuideShowAttachments, false)
        }
    }

    private fun onFromAddressesLoaded(result: ComposerResult.GetAllFromAddresses){
        when (result) {
            is ComposerResult.GetAllFromAddresses.Success -> {
                model.accounts = result.accounts.map { ActiveAccount.loadFromDB(it)!! }
                scene.fillFromOptions(result.accounts.sortedBy { !it.isActive }.map { it.recipientId.plus("@${it.domain}") })
                if(!(model.type is ComposerType.Empty || model.type is ComposerType.Support)
                        || model.accounts.size == 1){
                    model.selectedAccount = model.accounts.find { it.userEmail == activeAccount.userEmail }
                    scene.switchToSimpleFrom(model.selectedAccount!!.userEmail)
                }
            }
            is ComposerResult.GetAllFromAddresses.Failure -> {
                scene.showError(result.message)
            }
        }
    }

    private fun onDeviceRemovedRemotely(result: GeneralResult.DeviceRemoved){
        when (result) {
            is GeneralResult.DeviceRemoved.Success -> {
                if(result.activeAccount == null)
                    host.exitToScene(SignInParams(), ActivityMessage.ShowUIMessage(UIMessage(R.string.device_removed_remotely_exception)),
                            true, true)
                else {
                    activeAccount = result.activeAccount
                    host.exitToScene(MailboxParams(),
                            ActivityMessage.ShowUIMessage(UIMessage(R.string.snack_bar_active_account, arrayOf(activeAccount.userEmail))),
                            false, true)
                }
            }
        }
    }

    private fun onPasswordChangedRemotely(result: GeneralResult.ConfirmPassword){
        when (result) {
            is GeneralResult.ConfirmPassword.Success -> {
                scene.dismissConfirmPasswordDialog()
                scene.showMessage(UIMessage(R.string.update_password_success))
            }
            is GeneralResult.ConfirmPassword.Failure -> {
                scene.setConfirmPasswordError(UIMessage(R.string.password_enter_error))
            }
        }
    }

    private fun onLinkAccept(resultData: GeneralResult.LinkAccept){
        when (resultData) {
            is GeneralResult.LinkAccept.Success -> {
                host.exitToScene(LinkingParams(resultData.linkAccount, resultData.deviceId,
                        resultData.uuid, resultData.deviceType), null,
                        false, true)
            }
            is GeneralResult.LinkAccept.Failure -> {
                scene.showMessage(resultData.message)
            }
        }
    }

    private fun updateModelWithInputData(data: ComposerInputData) {
        model.to.clear()
        model.to.addAll(data.to.map {
            val contact = Contact(it.id, it.email.toLowerCase(), it.name, it.isTrusted, it.score, it.spamScore)
            contact.isCriptextDomain = it.isCriptextDomain
            contact
        })
        model.cc.clear()
        model.cc.addAll(data.cc.map {
            val contact = Contact(it.id, it.email.toLowerCase(), it.name, it.isTrusted, it.score, it.spamScore)
            contact.isCriptextDomain = it.isCriptextDomain
            contact
        })
        model.bcc.clear()
        model.bcc.addAll(data.bcc.map {
            val contact = Contact(it.id, it.email.toLowerCase(), it.name, it.isTrusted, it.score, it.spamScore)
            contact.isCriptextDomain = it.isCriptextDomain
            contact
        })
        model.body = data.body
        model.subject = data.subject
    }

    private fun isReadyForSending() = (model.to.isNotEmpty() || model.cc.isNotEmpty() || model.bcc.isNotEmpty())

    private fun uploadSelectedFile(filepath: String, fileKey: String, uuid: String){
        scene.dismissPreparingFileDialog()
        dataSource.submitRequest(ComposerRequest.UploadAttachment(
                filepath = filepath,
                fileKey = fileKey,
                filesSize = model.filesSize,
                uuid = uuid
        ))
    }

    private fun getThreadPreview(): EmailPreview? {
        return when (model.type) {
            is ComposerType.Reply -> model.type.threadPreview
            is ComposerType.ReplyAll -> model.type.threadPreview
            is ComposerType.Forward -> model.type.threadPreview
            is ComposerType.Draft -> model.type.threadPreview
            else -> null
        }
    }

    private fun saveEmailAsDraft(composerInputData: ComposerInputData, onlySave: Boolean) {
        val draftId = when (model.type) {
            is ComposerType.Draft -> model.type.draftId
            else -> null
        }
        val originalId = when (model.type) {
            is ComposerType.Forward -> model.type.originalId
            else -> null
        }
        val threadPreview = getThreadPreview()

        dataSource.submitRequest(ComposerRequest.SaveEmailAsDraft(
                threadId = threadPreview?.threadId,
                emailId = draftId,
                originalId = originalId,
                composerInputData = composerInputData,
                onlySave = onlySave, attachments = model.attachments, fileKey = model.fileKey,
                senderAccount = model.selectedAccount,
                currentLabel = model.currentLabel))

    }

    private fun onSendButtonClicked() {
        val data = scene.getDataInputByUser()
        updateModelWithInputData(data)

        if(isReadyForSending() && !model.isUploadingAttachments) {
            val validationError = Validator.validateContacts(data)
            when {
                validationError != null -> scene.showError(validationError.toUIMessage())
                Validator.criptextOnlyContacts(data) -> saveEmailAsDraft(data, onlySave = false)
                else -> observer.sendDialogButtonPressed()
            }
        } else if(model.isUploadingAttachments && model.attachments.isNotEmpty()) {
            scene.showError(UIMessage(R.string.wait_for_attachments))
        } else {
            scene.showError(UIMessage(R.string.no_recipients_error))
        }
    }

    override val menuResourceId
        get() = if (isReadyForSending()) R.menu.composer_menu_enabled
                              else R.menu.composer_menu_disabled

    private fun addNewAttachments(filesMetadata: List<Pair<String, Long>>) {
        val isNewAttachment: (Pair<String, Long>) -> (Boolean) = { data ->
            model.attachments.indexOfFirst { it.filepath == data.first  } < 0
        }
        val localAttachments = filesMetadata
                .filter(isNewAttachment)
                .filter {it.second != -1L}
                .map{ComposerAttachment(UUID.randomUUID().toString(), it.first, it.second, model.fileKey!!)}
        val remoteAttachments = filesMetadata
                .filter(isNewAttachment)
                .filter{ it.second == -1L }
        if(remoteAttachments.isNotEmpty()) {
            val resolver = host.getContentResolver()
            if(resolver != null) {
                scene.showPreparingFileDialog()
                generalDataSource.submitRequest(GeneralRequest.GetRemoteFile(
                        remoteAttachments.map { it.first }, resolver)
                )
            }
        }
        model.attachments.addAll(localAttachments)
        scene.notifyAttachmentSetChanged()
        handleNextUpload()
    }

    private fun handleNextUpload(){
        if(model.attachments.indexOfFirst { it.uploadProgress in 0..99 } >= 0){
            return
        }
        val attachmentToUpload = model.attachments.firstOrNull { it.uploadProgress == -1 } ?: return
        val composerAttachment = getAttachmentByUUID(attachmentToUpload.uuid)
        if(composerAttachment == null){
            scene.showMaxFilesExceedsDialog()
            return
        }else {
            composerAttachment.uploadProgress = 0
            uploadSelectedFile(attachmentToUpload.filepath, composerAttachment.fileKey, composerAttachment.uuid)
        }
    }

    private fun handleActivityMessage(activityMessage: ActivityMessage?): Boolean {
        if (activityMessage is ActivityMessage.AddAttachments) {
            if(!activityMessage.isShare){
                PinLockUtils.resetLastMillisPin()
                PinLockUtils.setPinLockTimeoutPosition(storage.getInt(KeyValueStorage.StringKey.PINTimeout, 1))
            }
            if(activityMessage.filesMetadata.isNotEmpty()){
                generateEmailFileKey()
                addNewAttachments(activityMessage.filesMetadata)
            }
            return true
        } else if (activityMessage is ActivityMessage.AddUrls){
            if(!activityMessage.isShare){
                PinLockUtils.resetLastMillisPin()
                PinLockUtils.setPinLockTimeoutPosition(storage.getInt(KeyValueStorage.StringKey.PINTimeout, 1))
            }
            if(activityMessage.urls.isNotEmpty()){
                activityMessage.urls.forEach {
                    model.body = model.body.plus(it.plus("\n"))
                }
                bindWithModel(ComposerInputData.fromModel(model), activeAccount.signature)
            }
        } else if(activityMessage is ActivityMessage.ShowUIMessage){
            scene.showError(activityMessage.message)
            return true
        }
        return false
    }

    private fun generateEmailFileKey(){
        if(model.fileKey != null)  return
        model.fileKey =
            FileUtils.generateFileKey()
    }

    private fun loadInitialData() {
        val type = model.type
        val request = when (type) {
            is ComposerType.Reply -> ComposerRequest.LoadInitialData(type, type.originalId)
            is ComposerType.ReplyAll -> ComposerRequest.LoadInitialData(type, type.originalId)
            is ComposerType.Forward -> ComposerRequest.LoadInitialData(type, type.originalId)
            is ComposerType.Draft -> ComposerRequest.LoadInitialData(type, type.draftId)
            is ComposerType.Support -> ComposerRequest.LoadInitialData(type, 0)
            is ComposerType.Report -> ComposerRequest.LoadInitialData(type, 0)
            is ComposerType.MailTo -> ComposerRequest.LoadInitialData(type, 0)
            else -> null
        }

        if (request != null) dataSource.submitRequest(request)
    }

    private fun bindWithModel(composerInputData: ComposerInputData, signature: String) {
        if(model.isReplyOrDraft || model.isSupport){
            scene.setFocusToComposer()
        } else {
            if(model.to.isEmpty()) scene.setFocusToTo() else scene.setFocusToComposer()
        }
        if(model.type is ComposerType.MailTo) scene.setFocusToSubject()
        scene.bindWithModel(firstTime = model.firstTime,
                composerInputData = composerInputData,
                attachments = model.attachments,
                signature = signature)
        scene.notifyAttachmentSetChanged()
        model.firstTime = false
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        PinLockUtils.enablePinLock()

        dataSourceController.setDataSourceListener()
        generalDataSource.listener = generalDataSourceListener

        model.checkedDomains.addAll(
              ContactDomainCheckData.KNOWN_EXTERNAL_DOMAINS.plus(ContactDomainCheckData(activeAccount.domain, true))
        )

        if (model.initialized)
            bindWithModel(ComposerInputData.fromModel(model), activeAccount.signature)
        else
            loadInitialData()

        dataSourceController.getAllContacts()
        dataSourceController.getAllFromAddresses()
        scene.observer = observer
        return handleActivityMessage(activityMessage)
    }

    override fun onResume(activityMessage: ActivityMessage?): Boolean {
        if(scene.observer == null) scene.observer = this.observer
        return false
    }

    override fun onPause() {
        cleanup(false)
    }

    override fun onStop() {
        cleanup(true)
    }

    private fun cleanup(fullCleanup: Boolean){
        val data = scene.getDataInputByUser()
        updateModelWithInputData(data)
        if(fullCleanup) {
            scene.observer = null
        }
    }

    override fun onBackPressed(): Boolean {

        if(model.isUploadingAttachments) {
            if(model.attachments.isEmpty())
                checkForDraft()
            else
                scene.showStayInComposerDialog(observer)
        }else{
            checkForDraft()
        }

        return false
    }

    override fun onMenuChanged(menu: IHostActivity.IActivityMenu) {}

    private fun exitToEmailDetailScene(){
        val threadPreview =  getThreadPreview()
        val currentLabel = when (model.type) {
            is ComposerType.Reply -> model.type.currentLabel
            is ComposerType.ReplyAll -> model.type.currentLabel
            is ComposerType.Forward -> model.type.currentLabel
            is ComposerType.Draft -> model.type.currentLabel
            else -> null
        }
        if(model.type is ComposerType.Empty || threadPreview == null || currentLabel == null){
            return host.finishScene()
        }
        val params = EmailDetailParams(threadId = threadPreview.threadId,
                currentLabel = currentLabel, threadPreview = threadPreview)
        host.exitToScene(params, null, true)
    }

    private fun shouldGoBackWithoutSave(): Boolean{
        val data = scene.getDataInputByUser()
        updateModelWithInputData(data)
        return !Validator.mailHasMoreThanSignature(data, activeAccount.signature, model.originalBody, model.type)
    }

    private fun checkForDraft(){
        if (shouldGoBackWithoutSave()) {
            exitToEmailDetailScene()
        } else {
            saveEmailAsDraft(composerInputData = scene.getDataInputByUser(), onlySave = true)
        }
    }

    override fun onOptionsItemSelected(itemId: Int) {
        when (itemId) {
            R.id.composer_send -> onSendButtonClicked()
        }
    }

    private inner class DataSourceController(
            private val dataSource: BackgroundWorkManager<ComposerRequest, ComposerResult>){

        fun setDataSourceListener() {
            dataSource.listener = dataSourceListener
        }

        fun getAllContacts(){
            val req = ComposerRequest.GetAllContacts()
            dataSource.submitRequest(req)
        }

        fun getAllFromAddresses(){
            val req = ComposerRequest.GetAllFromAddresses()
            dataSource.submitRequest(req)
        }

    }

    override fun requestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode != BaseActivity.RequestCode.writeAccess.ordinal) return

        val indexOfPermission = permissions.indexOfFirst { it == Manifest.permission.WRITE_EXTERNAL_STORAGE }
        if (indexOfPermission < 0) return
        if (grantResults[indexOfPermission] != PackageManager.PERMISSION_GRANTED) {
            scene.showError(UIMessage(R.string.permission_filepicker_rationale))
            return
        }
        scene.showAttachmentsBottomDialog(observer)

    }

}