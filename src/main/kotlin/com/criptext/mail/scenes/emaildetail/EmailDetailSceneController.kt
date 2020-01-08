package com.criptext.mail.scenes.emaildetail

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.view.View
import com.criptext.mail.BaseActivity
import com.criptext.mail.ExternalActivityParams
import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.api.models.SyncStatusData
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.*
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.composer.data.ComposerType
import com.criptext.mail.scenes.emaildetail.data.EmailDetailDataSource
import com.criptext.mail.scenes.emaildetail.data.EmailDetailRequest
import com.criptext.mail.scenes.emaildetail.data.EmailDetailResult
import com.criptext.mail.scenes.emaildetail.ui.EmailDetailUIObserver
import com.criptext.mail.scenes.emaildetail.ui.FullEmailListAdapter
import com.criptext.mail.scenes.label_chooser.LabelDataHandler
import com.criptext.mail.scenes.label_chooser.SelectedLabels
import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.scenes.mailbox.OnDeleteEmailListener
import com.criptext.mail.scenes.mailbox.OnDeleteThreadListener
import com.criptext.mail.scenes.mailbox.OnMoveThreadsListener
import com.criptext.mail.scenes.mailbox.data.EmailThread
import com.criptext.mail.scenes.params.*
import com.criptext.mail.scenes.signin.data.LinkStatusData
import com.criptext.mail.utils.*
import com.criptext.mail.utils.file.FileUtils
import com.criptext.mail.utils.file.PathUtil
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.generaldatasource.data.UserDataWriter
import com.criptext.mail.utils.mailtemplates.CriptextMailTemplate
import com.criptext.mail.utils.mailtemplates.FWMailTemplate
import com.criptext.mail.utils.mailtemplates.REMailTemplate
import com.criptext.mail.utils.ui.data.DialogResult
import com.criptext.mail.utils.ui.data.DialogType
import com.criptext.mail.websocket.WebSocketEventListener
import com.criptext.mail.websocket.WebSocketEventPublisher
import com.criptext.mail.websocket.WebSocketSingleton
import java.net.URLDecoder
import java.util.*

/**
 * Created by sebas on 3/12/18.
 */

class EmailDetailSceneController(private val storage: KeyValueStorage,
                                 private val scene: EmailDetailScene,
                                 private val model: EmailDetailSceneModel,
                                 private val host: IHostActivity,
                                 private var activeAccount: ActiveAccount,
                                 private val generalDataSource: GeneralDataSource,
                                 private val dataSource: EmailDetailDataSource,
                                 private var websocketEvents: WebSocketEventPublisher,
                                 private val keyboard: KeyboardManager) : SceneController() {


    private val remoteChangeDataSourceListener: (GeneralResult) -> Unit = { result ->
        when(result) {
            is GeneralResult.DeviceRemoved -> onDeviceRemovedRemotely(result)
            is GeneralResult.ConfirmPassword -> onPasswordChangedRemotely(result)
            is GeneralResult.ActiveAccountUpdateMailbox -> onActiveAccountMailboxUpdate(result)
            is GeneralResult.LinkAccept -> onLinkAccept(result)
            is GeneralResult.SyncAccept -> onSyncAccept(result)
            is GeneralResult.ResendEmail -> onResendEmail(result)
            is GeneralResult.ChangeToNextAccount -> onChangeToNextAccount(result)
            is GeneralResult.SetActiveAccountFromPush -> onSetActiveAccountFromPush(result)
            is GeneralResult.GetEmailPreview -> onGetEmailPreview(result)
        }
    }

    private val dataSourceListener = { result: EmailDetailResult ->
        when (result) {
            is EmailDetailResult.LoadFullEmailsFromThreadId -> onFullEmailsLoaded(result)
            is EmailDetailResult.UnsendFullEmailFromEmailId -> onUnsendEmail(result)
            is EmailDetailResult.GetSelectedLabels -> onSelectedLabelsLoaded(result)
            is EmailDetailResult.UpdateEmailThreadsLabelsRelations -> onUpdatedLabels(result)
            is EmailDetailResult.UpdateUnreadStatus -> onUpdateUnreadStatus(result)
            is EmailDetailResult.MoveEmailThread -> onMoveEmailThread(result)
            is EmailDetailResult.MoveEmail -> onMoveEmail(result)
            is EmailDetailResult.DownloadFile -> onDownloadedFile(result)
            is EmailDetailResult.ReadEmails -> onReadEmails(result)
            is EmailDetailResult.MarkAsReadEmail -> onMarAsReadEmail(result)
            is EmailDetailResult.CopyToDownloads -> onCopyToDownloads(result)
            is EmailDetailResult.DeleteDraft -> onDeleteDraft(result)
        }
    }

    private val emailDetailUIObserver = object: EmailDetailUIObserver{

        override fun onSnackbarClicked() {

        }

        override fun onSyncAuthConfirmed(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {
            if(trustedDeviceInfo.syncFileVersion == UserDataWriter.FILE_SYNC_VERSION)
                generalDataSource.submitRequest(GeneralRequest.SyncAccept(trustedDeviceInfo))
            else
                scene.showMessage(UIMessage(R.string.sync_version_incorrect))
        }

        override fun onSyncAuthDenied(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {
            generalDataSource.submitRequest(GeneralRequest.SyncDenied(trustedDeviceInfo))
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

        override fun onLinkAuthConfirmed(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {
            if(untrustedDeviceInfo.syncFileVersion == UserDataWriter.FILE_SYNC_VERSION)
                generalDataSource.submitRequest(GeneralRequest.LinkAccept(untrustedDeviceInfo))
            else
                scene.showMessage(UIMessage(R.string.sync_version_incorrect))
        }

        override fun onLinkAuthDenied(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {
            generalDataSource.submitRequest(GeneralRequest.LinkDenied(untrustedDeviceInfo))
        }

        override fun onOkButtonPressed(password: String) {
            generalDataSource.submitRequest(GeneralRequest.ConfirmPassword(password))
        }

        override fun onCancelButtonPressed() {
            generalDataSource.submitRequest(GeneralRequest.DeviceRemoved(true))
        }

        override fun onBackButtonPressed() {
            if(model.emails.any { it.email.unread }){
                model.threadPreview = model.threadPreview.copy(
                        unread = true)
            }
            if(model.exitToMailbox){
                host.exitToScene(
                        params = MailboxParams(),
                        activityMessage = ActivityMessage.UpdateThreadPreview(model.threadPreview),
                        forceAnimation = true,
                        deletePastIntents = true
                )
            } else {
                host.exitToScene(
                        params = MailboxParams(),
                        activityMessage = ActivityMessage.UpdateThreadPreview(model.threadPreview),
                        forceAnimation = false
                )
            }
        }

        override fun showStartGuideEmailIsRead(view: View) {
            host.showStartGuideView(
                    view,
                    R.string.start_guide_email_read,
                    R.dimen.focal_padding_read_mail
            )
        }

        override fun showStartGuideMenu(view: View) {
            host.showStartGuideView(
                    view,
                    R.string.start_guide_unsend_button,
                    R.dimen.focal_padding_read_mail
            )
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
                scene.showError(UIMessage(R.string.update_password_success))
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
                scene.showError(resultData.message)
            }
        }
    }

    private fun onSyncAccept(resultData: GeneralResult.SyncAccept){
        when (resultData) {
            is GeneralResult.SyncAccept.Success -> {
                host.exitToScene(LinkingParams(resultData.syncAccount, resultData.deviceId,
                        resultData.uuid, resultData.deviceType), ActivityMessage.SyncMailbox(),
                        false, true)
            }
            is GeneralResult.SyncAccept.Failure -> {
                scene.showMessage(resultData.message)
            }
        }
    }

    private fun onResendEmail(resultData: GeneralResult.ResendEmail){
        when (resultData) {
            is GeneralResult.ResendEmail.Success -> {
                model.emails[resultData.position].email.delivered = DeliveryTypes.SENT
                scene.notifyFullEmailChanged(resultData.position + 1)

                val latestEmailWasUpdated = resultData.position == model.emails.size - 1
                if (latestEmailWasUpdated) {

                    model.threadPreview = model.threadPreview.copy(
                            deliveryStatus = DeliveryTypes.SENT)
                }

                if(resultData.isSecure){
                    scene.showMessage(UIMessage(R.string.email_sent_secure))
                } else {
                    scene.showMessage(UIMessage(R.string.email_sent))
                }
            }
        }
    }

    private fun onSetActiveAccountFromPush(resultData: GeneralResult.SetActiveAccountFromPush){
        when(resultData){
            is GeneralResult.SetActiveAccountFromPush.Success -> {
                model.waitForAccountSwitch = false
                activeAccount = resultData.activeAccount
                generalDataSource.activeAccount = activeAccount
                dataSource.activeAccount = activeAccount

                IntentUtils.handleIntentExtras(resultData.extrasData, generalDataSource, activeAccount,
                        host, model.currentLabel, true)

                websocketEvents.setListener(webSocketEventListener)
                scene.showMessage(UIMessage(R.string.snack_bar_active_account, arrayOf(activeAccount.userEmail)))
            }
        }
    }

    private fun onChangeToNextAccount(result: GeneralResult.ChangeToNextAccount){
        when(result) {
            is GeneralResult.ChangeToNextAccount.Success -> {
                activeAccount = result.activeAccount
                generalDataSource.activeAccount = activeAccount
                dataSource.activeAccount = activeAccount
                val jwts = storage.getString(KeyValueStorage.StringKey.JWTS, "")
                websocketEvents = if(jwts.isNotEmpty())
                    WebSocketSingleton.getInstance(jwts)
                else
                    WebSocketSingleton.getInstance(activeAccount.jwt)

                websocketEvents.setListener(webSocketEventListener)

                scene.dismissAccountSuspendedDialog()

                scene.showMessage(UIMessage(R.string.snack_bar_active_account, arrayOf(activeAccount.userEmail)))

                host.exitToScene(MailboxParams(), null, false, true)
            }
        }
    }

    private fun onActiveAccountMailboxUpdate(result: GeneralResult.ActiveAccountUpdateMailbox){
        when (result) {
            is GeneralResult.ActiveAccountUpdateMailbox.Success -> {
                dataSource.submitRequest(EmailDetailRequest.LoadFullEmailsFromThreadId(
                            model.threadId, model.currentLabel, null))
            }
            is GeneralResult.ActiveAccountUpdateMailbox.SuccessAndRepeat -> {
                generalDataSource.submitRequest(GeneralRequest.ActiveAccountUpdateMailbox(
                        model.currentLabel
                ))
            }
            is GeneralResult.ActiveAccountUpdateMailbox.Unauthorized -> {
                generalDataSource.submitRequest(GeneralRequest.DeviceRemoved(false))
            }
            is GeneralResult.ActiveAccountUpdateMailbox.Forbidden -> {
                scene.showConfirmPasswordDialog(emailDetailUIObserver)
            }
            is GeneralResult.ActiveAccountUpdateMailbox.EnterpriseSuspended -> {
                showSuspendedAccountDialog()
            }
        }
    }

    private fun onSelectedLabelsLoaded(result: EmailDetailResult.GetSelectedLabels) {
        when (result) {
            is EmailDetailResult.GetSelectedLabels.Success -> {
                scene.onFetchedSelectedLabels(result.selectedLabels,
                        result.allLabels)
            }

            is EmailDetailResult.GetSelectedLabels.Failure -> {
                scene.showError(UIMessage(R.string.error_getting_labels))
            }
        }
    }

    private fun onUpdatedLabels(result: EmailDetailResult.UpdateEmailThreadsLabelsRelations) {

        when(result) {
            is EmailDetailResult.UpdateEmailThreadsLabelsRelations.Success ->  {
                model.threadPreview.isStarred = result.selectedLabels.contains(Label.defaultItems.starred)
                scene.notifyLabelsChanged(result.selectedLabels)
                if(result.exitAndReload)
                    host.exitToScene(
                            params = MailboxParams(),
                            activityMessage = ActivityMessage.UpdateMailBox(),
                            forceAnimation = false
                    )
            } else -> {
                scene.showError(UIMessage(R.string.error_updating_labels))
            }
        }
    }

    private fun onUpdateUnreadStatus(result: EmailDetailResult.UpdateUnreadStatus){
        when(result) {
            is EmailDetailResult.UpdateUnreadStatus.Success ->  {
                val message = ActivityMessage.UpdateUnreadStatusThread(result.threadId, result.unread)
                host.exitToScene(
                        params = MailboxParams(),
                        activityMessage = message,
                        forceAnimation = false)
            }
            is EmailDetailResult.UpdateUnreadStatus.Failure -> {
                    scene.showError(UIMessage(R.string.error_updating_status))
            }
            is EmailDetailResult.UpdateUnreadStatus.Unauthorized -> {
                generalDataSource.submitRequest(GeneralRequest.DeviceRemoved(false))
            }
            is EmailDetailResult.UpdateUnreadStatus.Forbidden -> {
                scene.showConfirmPasswordDialog(emailDetailUIObserver)
            }
        }
    }

    private fun onMoveEmailThread(result: EmailDetailResult.MoveEmailThread){
        when(result) {
            is EmailDetailResult.MoveEmailThread.Success ->  {
                val message = ActivityMessage.MoveThread(result.threadId)
                host.exitToScene(
                        params = MailboxParams(),
                        activityMessage = message,
                        forceAnimation = false)
            }
            is EmailDetailResult.MoveEmailThread.Failure -> {
                    scene.showError(UIMessage(R.string.error_moving_emails))
            }
            is EmailDetailResult.MoveEmailThread.Unauthorized -> {
                generalDataSource.submitRequest(GeneralRequest.DeviceRemoved(false))
            }
            is EmailDetailResult.MoveEmailThread.Forbidden -> {
                scene.showConfirmPasswordDialog(emailDetailUIObserver)
            }
        }
    }

    private fun onMoveEmail(result: EmailDetailResult.MoveEmail){
        when(result) {
            is EmailDetailResult.MoveEmail.Success ->  {
                val position = model.emails.indexOfFirst { it.email.id == result.emailId }
                if(position > -1){
                    if(model.emails.size == 1){
                        val message = ActivityMessage.MoveThread(model.threadId)
                        host.exitToScene(
                                params = MailboxParams(),
                                activityMessage = message,
                                forceAnimation = false)
                    } else {
                        model.emails.removeAt(position)
                        model.threadPreview = model.threadPreview.copy(
                                count = model.threadPreview.count - 1
                        )
                        scene.notifyFullEmailRemoved(position + 1)
                    }
                }
            }
            is EmailDetailResult.MoveEmail.Failure -> {
                scene.showError(UIMessage(R.string.error_moving_emails))
            }
            is EmailDetailResult.MoveEmail.Unauthorized -> {
                generalDataSource.submitRequest(GeneralRequest.DeviceRemoved(false))
            }
            is EmailDetailResult.MoveEmail.Forbidden -> {
                scene.showConfirmPasswordDialog(emailDetailUIObserver)
            }
        }
    }

    private fun onUnsendEmail(result: EmailDetailResult.UnsendFullEmailFromEmailId) {
        when (result) {
            is EmailDetailResult.UnsendFullEmailFromEmailId.Success -> {
                if(result.position > -1){
                    model.emails[result.position].isUnsending = false
                    model.emails[result.position].email.unsentDate = result.unsentDate
                    scene.notifyFullEmailChanged(result.position + 1)
                }
                setEmailAtPositionAsUnsend(result.position, result.unsentDate)
            }

            is EmailDetailResult.UnsendFullEmailFromEmailId.Failure -> {
                if (result.position > -1) {
                    model.emails[result.position].isUnsending = false
                    scene.notifyFullEmailChanged(result.position + 1)
                }
                scene.showError(result.message)
            }
            is EmailDetailResult.UnsendFullEmailFromEmailId.Unauthorized -> {
                generalDataSource.submitRequest(GeneralRequest.DeviceRemoved(false))
            }
            is EmailDetailResult.UnsendFullEmailFromEmailId.Forbidden -> {
                scene.showConfirmPasswordDialog(emailDetailUIObserver)
            }
        }
    }

    private fun setEmailAtPositionAsUnsend(position: Int, unsentDate: Date) {
        val fullEmail = model.emails[position]
        fullEmail.email.delivered = DeliveryTypes.UNSEND
        for(file in fullEmail.files){
            file.status = 0
        }
        scene.notifyFullEmailChanged(position + 1)

        val latestEmailWasUpdated = position == model.emails.size - 1
        if (latestEmailWasUpdated) {

            model.threadPreview = model.threadPreview.copy(
                    deliveryStatus = DeliveryTypes.UNSEND,
                    latestEmailUnsentDate = unsentDate)
        }
    }

    private fun downloadFile(emailId: Long, fileToken: String, fileKey: String?, fileName: String,
                             fileSize: Long){
        updateAttachmentProgress(emailId, fileToken, 0)
        dataSource.submitRequest(EmailDetailRequest.DownloadFile(fileToken = fileToken,
                emailId = emailId, fileKey = fileKey, fileName = fileName, fileSize = fileSize))
    }

    private fun onDownloadedFile(result: EmailDetailResult){
        when(result){
            is EmailDetailResult.DownloadFile.Success -> {
                if(result.cid == null || result.cid == "") {
                    updateAttachmentProgress(result.emailId, result.filetoken, 100)
                    openFile(result.filepath)
                }else{
                    updateInlineImage(result.emailId, result.cid, result.filepath)
                }
            }
            is EmailDetailResult.DownloadFile.Failure -> {
                updateAttachmentProgress(result.emailId, result.fileToken, -1)
                scene.showError(result.message)
            }
            is EmailDetailResult.DownloadFile.Progress -> {
                updateAttachmentProgress(result.emailId, result.filetoken, result.progress)
            }
            is EmailDetailResult.DownloadFile.Unauthorized -> {
                generalDataSource.submitRequest(GeneralRequest.DeviceRemoved(false))
            }
            is EmailDetailResult.DownloadFile.Forbidden -> {
                scene.showConfirmPasswordDialog(emailDetailUIObserver)
            }
            is EmailDetailResult.DownloadFile.EnterpriseSuspended -> {
                showSuspendedAccountDialog()
            }
        }
    }

    private fun onReadEmails(result: EmailDetailResult.ReadEmails){
        when(result){
            is EmailDetailResult.ReadEmails.Success -> {
                if(model.doReply){
                    emailHolderEventListener.onReplyBtnClicked()
                }
            }
        }
    }

    private fun onMarAsReadEmail(result: EmailDetailResult.MarkAsReadEmail){
        when(result){
            is EmailDetailResult.MarkAsReadEmail.Success -> {
                result.metadataKeys.forEach { metadataKey ->
                    val fullEmail = model.emails.find { it.email.metadataKey == metadataKey }
                    if(fullEmail != null) {
                        val position = model.emails.indexOf(fullEmail)
                        if(position >= 0) {
                            model.emails[position] = model.emails[position].copy(
                                    email = model.emails[position].email.copy(unread = result.unread)
                            )
                        }
                    }
                    emailDetailUIObserver.onBackButtonPressed()
                }
            }
        }
    }

    private fun onCopyToDownloads(result: EmailDetailResult.CopyToDownloads){
        when(result){
            is EmailDetailResult.CopyToDownloads.Success -> {
                scene.showError(result.message)
            }
        }
    }

    private fun onDeleteDraft(result: EmailDetailResult.DeleteDraft){
        when(result){
            is EmailDetailResult.DeleteDraft.Success -> {
                val position = model.emails.indexOfFirst { it.email.id == result.id }
                if(position > -1){
                    if(model.emails.size == 1){
                        val message = ActivityMessage.MoveThread(model.threadId)
                        host.exitToScene(
                                params = MailboxParams(),
                                activityMessage = message,
                                forceAnimation = false)
                    } else {
                        model.emails.removeAt(position)
                        val headerData = mutableListOf<EmailThread.HeaderData>()
                        headerData.addAll(model.threadPreview.headerData)
                        val removedHeader = headerData.getOrNull(position)
                        if(removedHeader != null) headerData.removeAt(position)
                        model.threadPreview = model.threadPreview.copy(
                                count = model.threadPreview.count - 1,
                                bodyPreview = model.emails[position - 1].email.preview,
                                headerData = headerData,
                                sender = model.emails[position - 1].from,
                                timestamp = model.emails[position - 1].email.date
                        )
                        scene.notifyFullEmailRemoved(position + 1)
                    }
                }
            }
        }
    }

    private fun updateAttachmentProgress(emailId: Long, filetoken: String, progress: Int){
        val emailIndex = model.emails.indexOfFirst { it.email.id == emailId }
        if (emailIndex < 0) return
        val attachmentIndex = model.emails[emailIndex].files.indexOfFirst { it.token == filetoken }
        if (attachmentIndex < 0) return
        if(model.fileDetails[emailId]!![attachmentIndex].progress != 100)
            model.fileDetails[emailId]!![attachmentIndex].progress = progress
        scene.updateAttachmentProgress(emailIndex + 1, attachmentIndex)
    }

    private fun updateInlineImage(emailId: Long, cid: String, filePath: String){
        val emailIndex = model.emails.indexOfFirst { it.email.id == emailId }
        if (emailIndex < 0) return
        scene.updateInlineImage(emailIndex + 1, cid, filePath)
    }

    private fun openFile(filepath: String){
        val mimeType = FileUtils.getMimeType(filepath)
        val params = ExternalActivityParams.FilePresent(filepath, mimeType)
        try {
            host.launchExternalActivityForResult(params)
        }catch (e: ActivityNotFoundException){
            scene.showMessage(UIMessage(R.string.error_no_app_for_file))
        }
    }

    private val onMoveThreadsListener = object : OnMoveThreadsListener {

        override fun onMoveToInboxClicked() {
            moveEmailThread(Label.LABEL_INBOX)
        }

        override fun onMoveToSpamClicked() {
            moveEmailThread(Label.LABEL_SPAM)
        }

        override fun onMoveToTrashClicked() {
            moveEmailThread(Label.LABEL_TRASH)
        }
    }

    private val onDeleteThreadListener = object : OnDeleteThreadListener {
        override fun onDeleteConfirmed() {
            moveEmailThread(chosenLabel = null)
        }
    }

    private val onDeleteEmailListener = object : OnDeleteEmailListener {
        override fun onDeleteConfirmed(fullEmail: FullEmail) {
            moveEmail(fullEmail, null)
        }
    }

    private val emailHolderEventListener = object : FullEmailListAdapter.OnFullEmailEventListener{
        override fun onReportOptionSelected(fullEmail: FullEmail, position: Int) {
            moveEmail(fullEmail, Label.LABEL_SPAM, true)
        }

        override fun contextMenuRegister(view: View) {
            host.contextMenuRegister(view)
        }

        override fun onRetrySendOptionSelected(fullEmail: FullEmail, position: Int) {
            generalDataSource.submitRequest(GeneralRequest.ResendEmail(fullEmail.email.id, position))
        }

        override fun onResourceLoaded(cid: String) {
            val inlineFile = model.emails.map { email -> email.files }.map { files -> files.find { it.cid == cid } }.first()

            if(inlineFile != null) {
                model.inlineImages.add(inlineFile)
                    dataSource.submitRequest(EmailDetailRequest.DownloadFile(
                            fileName = inlineFile.name,
                            cid = inlineFile.cid,
                            fileToken = inlineFile.token,
                            fileSize = inlineFile.size,
                            fileKey = inlineFile.fileKey,
                            emailId = inlineFile.emailId
                    ))
            }
        }

        override fun onSourceOptionSelected(fullEmail: FullEmail) {
            if(fullEmail.headers != null && fullEmail.email.boundary != null)
                host.goToScene(EmailSourceParams(EmailUtils.getEmailSource(
                        headers = fullEmail.headers,
                        boundary = fullEmail.email.boundary!!,
                        content = fullEmail.email.content
                )), true)
        }

        override fun onCollapsedClicked() {
            scene.expandAllThread()
        }

        override fun onStarredButtonPressed(isStarred: Boolean) {
            model.threadPreview.isStarred = isStarred
            val selectedLabels = SelectedLabels()
            val labelsWithoutFilter = model.emails.flatMap { it.labels }.toMutableList()
            val labels = if(isStarred){
                labelsWithoutFilter.add(Label.defaultItems.starred)
                labelsWithoutFilter
            }
            else{
                labelsWithoutFilter.filter { it.id != Label.defaultItems.starred.id }
            }
            selectedLabels.addMultipleSelected(labels.toSet().map { LabelWrapper(it) })
            updateThreadLabelsRelation(selectedLabels)
        }

        override fun onAttachmentSelected(emailPosition: Int, attachmentPosition: Int) {
            if (!host.checkPermissions(BaseActivity.RequestCode.writeAccess.ordinal,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                model.fileToDownload = Pair(emailPosition, attachmentPosition)
                return
            }
            val email = model.emails[emailPosition]
            val attachment = email.files.filter { it.cid == null || it.cid == "" }[attachmentPosition]
            if(attachment.status != 0) {
                downloadFile(email.email.id, attachment.token, getFileKey(attachment, email), attachment.name,
                        attachment.size)
            }
        }

        fun getFileKey(attachment: CRFile, email: FullEmail): String?{
            return if (attachment.fileKey.isEmpty()) email.fileKey
            else attachment.fileKey
        }

        override fun onUnsendEmail(fullEmail: FullEmail, position: Int) {
            val req = EmailDetailRequest.UnsendFullEmailFromEmailId(
                    position = position,
                    emailId = fullEmail.email.id)
            fullEmail.isUnsending = true
            scene.notifyFullEmailChanged(position + 1)
            dataSource.submitRequest(req)
        }
        override fun onForwardBtnClicked() {
            val type = ComposerType.Forward(originalId = model.emails.last().email.id,
                    threadPreview = model.threadPreview, currentLabel = model.currentLabel,
                    template = host.getMailTemplate(CriptextMailTemplate.TemplateType.FW) as FWMailTemplate)
            host.goToScene(ComposerParams(type, model.currentLabel), false)
        }

        override fun onReplyBtnClicked() {
            val type = ComposerType.Reply(originalId = model.emails.last().email.id,
                    threadPreview = model.threadPreview, currentLabel = model.currentLabel,
                    template = host.getMailTemplate(CriptextMailTemplate.TemplateType.RE) as REMailTemplate)
            host.goToScene(ComposerParams(type, model.currentLabel), false)
        }

        override fun onReplyAllBtnClicked() {
            val type = ComposerType.ReplyAll(originalId = model.emails.last().email.id,
                    threadPreview = model.threadPreview, currentLabel = model.currentLabel,
                    template = host.getMailTemplate(CriptextMailTemplate.TemplateType.RE) as REMailTemplate)
            host.goToScene(ComposerParams(type, model.currentLabel), false)
        }

        override fun ontoggleViewOpen(fullEmail: FullEmail, position: Int, viewOpen: Boolean) {
            fullEmail.viewOpen = viewOpen
            scene.notifyFullEmailChanged(position)
        }

        override fun onReplyOptionSelected(fullEmail: FullEmail, position: Int, all: Boolean) {
            val type = ComposerType.Reply(originalId = fullEmail.email.id,
                    threadPreview = model.threadPreview, currentLabel = model.currentLabel,
                    template = host.getMailTemplate(CriptextMailTemplate.TemplateType.RE) as REMailTemplate)
            host.goToScene(ComposerParams(type, model.currentLabel), false)
        }

        override fun onReplyAllOptionSelected(fullEmail: FullEmail, position: Int, all: Boolean) {
            val type = ComposerType.ReplyAll(originalId = fullEmail.email.id,
                    threadPreview = model.threadPreview, currentLabel = model.currentLabel,
                    template = host.getMailTemplate(CriptextMailTemplate.TemplateType.RE) as REMailTemplate)
            host.goToScene(ComposerParams(type, model.currentLabel), false)
        }

        override fun onForwardOptionSelected(fullEmail: FullEmail, position: Int, all: Boolean) {
            val type = ComposerType.Forward(originalId = fullEmail.email.id,
                    threadPreview = model.threadPreview, currentLabel = model.currentLabel,
                    template = host.getMailTemplate(CriptextMailTemplate.TemplateType.FW) as FWMailTemplate)
            host.goToScene(ComposerParams(type, model.currentLabel), false)
        }

        override fun onToggleReadOption(fullEmail: FullEmail, position: Int, markAsRead: Boolean) {
            val emailKeys = model.emails.subList(position, model.emails.lastIndex).map { it.email.metadataKey }
            dataSource.submitRequest(EmailDetailRequest.MarkAsReadEmail(
                    metadataKeys = emailKeys,
                    unread = !markAsRead
            ))
        }

        override fun onDeleteOptionSelected(fullEmail: FullEmail, position: Int) {
            if(!(fullEmail.labels.contains(Label.defaultItems.trash) || fullEmail.labels.contains(Label.defaultItems.spam)))
                moveEmail(fullEmail, Label.LABEL_TRASH)
            else
                deleteSelectedEmail4Ever(fullEmail)
        }

        override fun onSpamOptionSelected(fullEmail: FullEmail, position: Int) {
            moveEmail(fullEmail, Label.LABEL_SPAM)
        }

        override fun onPrintOptionSelected(fullEmail: FullEmail) {
            val toList = fullEmail.to.map { it.toString() }.joinToString().replace("<", "&lt;").replace(">", "&gt;")
            val info = HTMLUtils.PrintHeaderInfo(subject = fullEmail.email.subject, toList = toList,
                    date = fullEmail.email.date, fromMail = fullEmail.from.email, fromName = fullEmail.from.name)
            scene.printFullEmail(info, fullEmail.email.content,fullEmail.email.subject + "-" + fullEmail.email.metadataKey,
                    EmailUtils.checkIfItsForward(fullEmail.email.subject))
        }

        override fun onContinueDraftOptionSelected(fullEmail: FullEmail) {
            val type = ComposerType.Draft(draftId = fullEmail.email.id,
                    threadPreview = model.threadPreview, currentLabel = model.currentLabel)
            host.goToScene(ComposerParams(type, model.currentLabel), false)
        }

        override fun onDeleteDraftOptionSelected(fullEmail: FullEmail) {
            dataSource.submitRequest(EmailDetailRequest.DeleteDraft(fullEmail.email.id))
        }

        override fun showStartGuideEmailIsRead(view: View) {
            if(storage.getBool(KeyValueStorage.StringKey.StartGuideShowEmailRead, true)){
                scene.showStartGuideEmailIsRead(view)
                storage.putBool(KeyValueStorage.StringKey.StartGuideShowEmailRead, false)
            }
        }

        override fun showStartGuideMenu(view: View) {
            if(storage.getBool(KeyValueStorage.StringKey.StartGuideShowOptions, true)){
                scene.showStartGuideMenu(view)
                storage.putBool(KeyValueStorage.StringKey.StartGuideShowOptions, false)
            }
        }
    }

    private fun readEmails(emails: List<FullEmail>) {
        val emailIds = emails.map { it.email.id }
        val metadataKeys = emails.map { it.email.metadataKey }

        dataSource.submitRequest(EmailDetailRequest.ReadEmails(
                emailIds = emailIds,
                metadataKeys = metadataKeys
        ))


    }

    private fun onGetEmailPreview(result: GeneralResult.GetEmailPreview){
        when(result){
            is GeneralResult.GetEmailPreview.Success -> {
                model.emails.clear()
                model.threadId = result.emailPreview.threadId
                model.currentLabel = Label.defaultItems.inbox
                model.threadPreview = result.emailPreview
                loadEmails(null)
            }
        }
    }

    private fun onFullEmailsLoaded(result: EmailDetailResult.LoadFullEmailsFromThreadId){
        when (result) {
            is EmailDetailResult.LoadFullEmailsFromThreadId.Success -> {
                if (result.fullEmailList.isEmpty()) {
                    host.exitToScene(
                            params = MailboxParams(),
                            activityMessage = ActivityMessage.UpdateMailBox(),
                            forceAnimation = false)
                } else {
                    if(model.emails.isEmpty()) {
                        val lastEmail = result.fullEmailList.last().email
                        model.threadPreview = model.threadPreview.copy(
                                unread = false,
                                count = result.fullEmailList.size,
                                bodyPreview = lastEmail.preview,
                                deliveryStatus = lastEmail.delivered)
                        model.emails.addAll(result.fullEmailList)
                        val fullEmailsList = VirtualEmailDetailList(model)
                        result.fullEmailList.forEach { fullEmail ->
                            model.fileDetails[fullEmail.email.id] = fullEmail.files.map { FileDetail(it) }
                        }

                        scene.attachView(
                                fullEmailList = fullEmailsList,
                                fullEmailEventListener = emailHolderEventListener,
                                fileDetailList = model.fileDetails,
                                observer = emailDetailUIObserver,
                                shouldOpenExpanded = (fullEmailsList.size < 4
                                || result.unreadEmails > 1 && fullEmailsList.size >= 4))

                        if(result.changeAccountMessage != null)
                            scene.showMessage(result.changeAccountMessage)
                        readEmails(result.fullEmailList)
                    }else{
                        val lastEmail = result.fullEmailList.last().email
                        model.threadPreview = model.threadPreview.copy(
                                unread = false,
                                count = result.fullEmailList.size,
                                bodyPreview = lastEmail.preview,
                                deliveryStatus = lastEmail.delivered)
                        val currentEmails = model.emails
                        val newEmails = result.fullEmailList.filter { it.email.id !in currentEmails.map { fullEmail -> fullEmail.email.id } }
                        val oldEmails = result.fullEmailList.filter { it.email.id in currentEmails.map { fullEmail -> fullEmail.email.id } }
                        model.emails.addAll(newEmails)
                        oldEmails.forEach {
                            val index = model.emails.indexOfFirst { mEmail -> mEmail.email.id == it.email.id }
                            if(it.email != model.emails[index].email || it.labels != model.emails[index].labels){
                                model.emails[index] = model.emails[index].copy(email = it.email, labels = it.labels)
                            }
                        }
                        model.emails.forEach { fullEmail ->
                            model.fileDetails[fullEmail.email.id] = fullEmail.files.map { FileDetail(it) }
                        }
                        if(newEmails.isNotEmpty())
                            scene.showMessage(UIMessage(R.string.new_email_snack, arrayOf(newEmails.size)))
                        scene.notifyFullEmailListChanged()
                        readEmails(model.emails)
                    }
                }
            }
            is EmailDetailResult.LoadFullEmailsFromThreadId.Failure -> {
                scene.showError(UIMessage(R.string.error_getting_email))
            }
        }
    }

    private fun loadEmails(message: UIMessage?) {
        val req = EmailDetailRequest.LoadFullEmailsFromThreadId(
                threadId = model.threadId, currentLabel = model.currentLabel,
                changeAccountMessage = message)

        dataSource.submitRequest(req)
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        PinLockUtils.enablePinLock()

        dataSource.listener = dataSourceListener
        generalDataSource.listener = remoteChangeDataSourceListener
        websocketEvents.setListener(webSocketEventListener)

        val extras = host.getIntentExtras()

        if(extras != null && extras.account.isNotEmpty()) {
            model.exitToMailbox = true
            if(extras.account == activeAccount.recipientId && extras.domain == activeAccount.domain)
                IntentUtils.handleIntentExtras(extras, generalDataSource, activeAccount, host, model.currentLabel)
            else {
                model.waitForAccountSwitch = true
                generalDataSource.submitRequest(GeneralRequest.SetActiveAccountFromPush(extras.account, extras.domain, extras))
            }
        } else {
            if (!model.waitForAccountSwitch && model.emails.isEmpty()) {
                val message = (activityMessage as? ActivityMessage.ShowUIMessage)?.message
                loadEmails(message)
            }
        }

        keyboard.hideKeyboard()
        return false
    }

    override fun onResume(activityMessage: ActivityMessage?): Boolean {
        websocketEvents.setListener(webSocketEventListener)
        return false
    }

    override fun onPause(){
        cleanup()
    }

    override fun onStop() {
        cleanup()
    }

    override fun onNeedToSendEvent(event: Int) {
        generalDataSource.submitRequest(GeneralRequest.UserEvent(event))
    }

    private fun cleanup(){
        websocketEvents.clearListener(webSocketEventListener)
    }

    override fun onBackPressed(): Boolean {
        emailDetailUIObserver.onBackButtonPressed()
        return true
    }

    override fun onMenuChanged(menu: IHostActivity.IActivityMenu) {}

    private fun removeCurrentLabelThread(exitAndReload: Boolean = false) {
        val req = EmailDetailRequest.UpdateEmailThreadsLabelsRelations(
                exitAndReload = exitAndReload,
                threadId = model.threadId,
                selectedLabels = SelectedLabels(),
                currentLabel = model.currentLabel,
                removeCurrentLabel = true)

        dataSource.submitRequest(req)
    }

    private fun deleteThread() {
        moveEmailThread(Label.LABEL_TRASH)
    }

    private fun updateUnreadStatusThread(){
        dataSource.submitRequest(EmailDetailRequest.UpdateUnreadStatus(
                threadId = model.threadId,
                updateUnreadStatus = true,
                currentLabel = model.currentLabel))
    }

    override fun onOptionsItemSelected(itemId: Int) {
        when(itemId) {
            R.id.mailbox_archive_selected_messages -> removeCurrentLabelThread(true)
            R.id.mailbox_delete_selected_messages -> deleteThread()
            R.id.mailbox_delete_selected_messages_4ever -> deleteSelectedEmailThreads4Ever()
            R.id.mailbox_not_spam -> removeCurrentLabelThread(true)
            R.id.mailbox_not_trash -> removeCurrentLabelThread(true)
            R.id.mailbox_spam -> moveEmailThread(Label.LABEL_SPAM)
            R.id.report_phishing -> moveEmailThread(Label.LABEL_SPAM, true)
            R.id.mailbox_message_toggle_read -> updateUnreadStatusThread()
            R.id.mailbox_move_to -> {
                scene.showDialogMoveTo(onMoveThreadsListener, model.currentLabel.text)
            }
            R.id.mailbox_add_labels -> {
                showLabelsDialog()
            }
            R.id.mailbox_print_all -> {
                printAllEmails()
            }
        }
    }

    override fun requestPermissionResult(requestCode: Int, permissions: Array<out String>,
                                         grantResults: IntArray) {
        if (requestCode != BaseActivity.RequestCode.writeAccess.ordinal) return
        val indexOfPermission = permissions.indexOfFirst { it == Manifest.permission.WRITE_EXTERNAL_STORAGE }
        if (indexOfPermission < 0) return
        if (grantResults[indexOfPermission] != PackageManager.PERMISSION_GRANTED) {
            scene.showError(UIMessage(R.string.permission_filepicker_rationale))
        }else{
            if(model.fileToDownload.first != -1){
                emailHolderEventListener.onAttachmentSelected(model.fileToDownload.first, model.fileToDownload.second)
            }
            if(model.hasTriedToSaveImage){
                model.hasTriedToSaveImage = false
                dataSource.submitRequest(EmailDetailRequest.CopyToDownloads(model.lastTouchedInlineSrc!!))
            }
        }
    }

    private fun showLabelsDialog() {
        val req = EmailDetailRequest.GetSelectedLabels(model.threadId)
        dataSource.submitRequest(req)
        scene.showDialogLabelsChooser(LabelDataHandler(this))
    }

    private fun printAllEmails(){
        if(model.emails.size == 1){
            emailHolderEventListener.onPrintOptionSelected(model.emails[0])
        }else {
            val toList = model.emails.map { it.to.map { it.toString() }.joinToString().replace("<", "&lt;").replace(">", "&gt;") }
            val info = model.emails.map {
                HTMLUtils.PrintHeaderInfo(subject = it.email.subject, toList = toList[model.emails.indexOf(it)],
                        date = it.email.date, fromMail = it.from.email, fromName = it.from.name)
            }
            scene.printAllFullEmail(info, model.emails.map { it.email.content },
                    model.emails[0].email.subject + "-" + model.emails[0].email.metadataKey,
                    EmailUtils.checkIfItsForward(model.emails.first().email.subject))
        }
    }

    fun moveEmail(fullEmail: FullEmail, chosenLabel: String?, isPhishing: Boolean = false){

        val req = EmailDetailRequest.MoveEmail(
                emailId = fullEmail.email.id,
                chosenLabel = chosenLabel,
                currentLabel = model.currentLabel,
                isPhishing = isPhishing)

        dataSource.submitRequest(req)
    }

    fun updateThreadLabelsRelation(selectedLabels: SelectedLabels) {

        val req = EmailDetailRequest.UpdateEmailThreadsLabelsRelations(
                exitAndReload = false,
                threadId = model.threadId,
                selectedLabels = selectedLabels,
                currentLabel = model.currentLabel,
                removeCurrentLabel = false)

        dataSource.submitRequest(req)

    }

    private fun moveEmailThread(chosenLabel: String?, isPhishing: Boolean = false) {
        val req = EmailDetailRequest.MoveEmailThread(
                threadId = model.threadId,
                chosenLabel = chosenLabel,
                currentLabel = model.currentLabel,
                isPhishing = isPhishing)

        dataSource.submitRequest(req)
    }

    private fun deleteSelectedEmailThreads4Ever() {
        scene.showDialogDeleteThread(onDeleteThreadListener)
    }

    private fun deleteSelectedEmail4Ever(fullEmail: FullEmail) {
        scene.showDialogDeleteEmail(onDeleteEmailListener, fullEmail)
    }

    override val menuResourceId: Int?
        get() = when {
            model.currentLabel == Label.defaultItems.draft -> R.menu.email_detail_menu_multi_mode_read_draft
            model.currentLabel == Label.defaultItems.spam -> R.menu.email_detail_menu_multi_mode_read_spam
            model.currentLabel == Label.defaultItems.trash -> R.menu.email_detail_menu_multi_mode_read_trash
            model.currentLabel.id < 0 -> R.menu.mailbox_menu_multi_mode_read_allmail
            else -> R.menu.email_detail_menu_multi_mode_read
        }

    private fun findEmailPositionByEmailId(emailId: Long): Int {
        return model.emails.indexOfFirst { it.email.id == emailId }
    }

    private fun markEmailAtPositionAsOpened(position: Int) {
        val fullEmail = model.emails[position]
        fullEmail.email.delivered = DeliveryTypes.READ
        scene.notifyFullEmailChanged(position)

        val latestEmailWasUpdated = position == model.emails.size - 1
        if (latestEmailWasUpdated)
            model.threadPreview = model.threadPreview.copy(
                    deliveryStatus = DeliveryTypes.READ)
    }

    fun onCreateContextMenu(inlineSrc: String?){
        model.lastTouchedInlineSrc =  URLDecoder.decode(inlineSrc, "UTF-8")
    }

    fun onContextItemSelected(itemId: Int) {
        when(itemId){
            R.id.view_image -> {
                if(model.lastTouchedInlineSrc != null)
                    openFile(PathUtil.getPathFromImgSrc(model.lastTouchedInlineSrc!!))
            }
            R.id.save_image -> {

                if (host.checkPermissions(BaseActivity.RequestCode.writeAccess.ordinal,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    if (model.lastTouchedInlineSrc != null)
                        dataSource.submitRequest(
                                EmailDetailRequest.CopyToDownloads(
                                        PathUtil.getPathFromImgSrc(model.lastTouchedInlineSrc!!)
                                )
                        )
                }else{
                    model.hasTriedToSaveImage = true
                }

            }
        }
    }

    private fun showSuspendedAccountDialog(){
        val jwtList = storage.getString(KeyValueStorage.StringKey.JWTS, "").split(",").map { it.trim() }
        val dialogType = if(jwtList.isNotEmpty() && jwtList.size > 1) DialogType.SwitchAccount()
        else DialogType.SignIn()
        scene.showAccountSuspendedDialog(emailDetailUIObserver, activeAccount.userEmail, dialogType)
    }

    private val webSocketEventListener = object : WebSocketEventListener {
        override fun onLinkDeviceDismiss(accountEmail: String) {
            host.runOnUiThread(Runnable {
                scene.dismissLinkDeviceDialog()
            })
        }

        override fun onSyncDeviceDismiss(accountEmail: String) {
            host.runOnUiThread(Runnable {
                scene.dismissSyncDeviceDialog()
            })
        }

        override fun onAccountSuspended(accountEmail: String) {
            host.runOnUiThread(Runnable {
                if (accountEmail == activeAccount.userEmail)
                    showSuspendedAccountDialog()
            })
        }

        override fun onAccountUnsuspended(accountEmail: String) {
            host.runOnUiThread(Runnable {
                if (accountEmail == activeAccount.userEmail)
                    scene.dismissAccountSuspendedDialog()
            })
        }

        override fun onSyncBeginRequest(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {
            host.runOnUiThread(Runnable {
                scene.showSyncDeviceAuthConfirmation(trustedDeviceInfo)
            })
        }

        override fun onSyncRequestAccept(syncStatusData: SyncStatusData) {

        }

        override fun onSyncRequestDeny() {

        }

        override fun onDeviceDataUploaded(key: String, dataAddress: String, authorizerId: Int) {

        }

        override fun onDeviceLinkAuthDeny() {

        }

        override fun onDeviceLinkAuthRequest(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {
            host.runOnUiThread(Runnable {
                scene.showLinkDeviceAuthConfirmation(untrustedDeviceInfo)
            })
        }

        override fun onDeviceLinkAuthAccept(linkStatusData: LinkStatusData) {

        }

        override fun onKeyBundleUploaded(deviceId: Int) {

        }

        override fun onNewEvent(recipientId: String, domain: String) {
            generalDataSource.submitRequest(
                    GeneralRequest.ActiveAccountUpdateMailbox(
                            model.currentLabel
                    ))
        }

        override fun onRecoveryEmailChanged(newEmail: String) {

        }

        override fun onRecoveryEmailConfirmed() {

        }

        override fun onDeviceLocked() {
            host.runOnUiThread(Runnable {
                scene.showConfirmPasswordDialog(emailDetailUIObserver)
            })
        }

        override fun onDeviceRemoved() {
            generalDataSource.submitRequest(GeneralRequest.DeviceRemoved(false))
        }

        override fun onError(uiMessage: UIMessage) {
            scene.showError(uiMessage)
        }
    }
}
