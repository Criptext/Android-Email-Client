package com.criptext.mail.utils

import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.push.data.IntentExtrasData
import com.criptext.mail.push.services.LinkDeviceActionService
import com.criptext.mail.push.services.NewMailActionService
import com.criptext.mail.push.services.SyncDeviceActionService
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.composer.data.ComposerType
import com.criptext.mail.scenes.params.ComposerParams
import com.criptext.mail.utils.file.FileUtils
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.UserDataWriter
import java.io.File

/**
 * Created by gabriel on 8/25/17.
 */

class IntentUtils {
    companion object {
        fun createIntentToOpenFileInExternalApp(ctx: Context, file: File): Intent {
            val intent = Intent(Intent.ACTION_VIEW)
            val uri = FileProvider.getUriForFile(ctx, "com.criptext.mail.fileProvider", file)
            val type = FileUtils.getMimeType(file.name)
            intent.setDataAndType(uri, type)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            return intent
        }
        fun createIntentToOpenCamera(ctx: Context, file: File): Intent {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val uri = FileProvider.getUriForFile(ctx, "com.criptext.mail.fileProvider", file)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            return intent
        }

        fun handleIntentExtras(extras: IntentExtrasData, generalDataSource: GeneralDataSource,
                                       activeAccount: ActiveAccount, host: IHostActivity,
                                       currentLabel: Label = Label.defaultItems.inbox,
                                       hasChangedAccount: Boolean = false): UIMessage? {
            when(extras.action){
                Intent.ACTION_MAIN -> {
                    val activityMessage = if(hasChangedAccount)
                        ActivityMessage.ShowUIMessage(
                                UIMessage(R.string.snack_bar_active_account, arrayOf(activeAccount.userEmail))
                        )
                    else null
                    val extrasMail = extras as IntentExtrasData.IntentExtrasDataMail
                    generalDataSource.submitRequest(GeneralRequest.GetEmailPreview(threadId = extrasMail.threadId,
                            userEmail = activeAccount.userEmail, activityMessage = activityMessage))
                }
                LinkDeviceActionService.APPROVE -> {
                    val extrasDevice = extras as IntentExtrasData.IntentExtrasDataDevice
                    val untrustedDeviceInfo = DeviceInfo.UntrustedDeviceInfo(extrasDevice.deviceId, activeAccount.recipientId, activeAccount.domain,
                            "", "", extrasDevice.deviceType, extrasDevice.syncFileVersion)
                    if(untrustedDeviceInfo.syncFileVersion == UserDataWriter.FILE_SYNC_VERSION)
                        generalDataSource.submitRequest(GeneralRequest.LinkAccept(untrustedDeviceInfo))
                    else
                        return UIMessage(R.string.sync_version_incorrect)
                }
                SyncDeviceActionService.APPROVE -> {
                    val extrasDevice = extras as IntentExtrasData.IntentExtrasSyncDevice
                    val trustedDeviceInfo = DeviceInfo.TrustedDeviceInfo(extrasDevice.account, activeAccount.domain, extrasDevice.deviceId, extrasDevice.deviceName,
                            extrasDevice.deviceType, extrasDevice.randomId, extrasDevice.syncFileVersion)
                    if(trustedDeviceInfo.syncFileVersion == UserDataWriter.FILE_SYNC_VERSION)
                        generalDataSource.submitRequest(GeneralRequest.SyncAccept(trustedDeviceInfo))
                    else
                        return UIMessage(R.string.sync_version_incorrect)
                }
                NewMailActionService.REPLY -> {
                    val extrasMail = extras as IntentExtrasData.IntentExtrasReply
                    generalDataSource.submitRequest(GeneralRequest.GetEmailPreview(threadId = extrasMail.threadId,
                            userEmail = activeAccount.userEmail, doReply = true))
                }
                Intent.ACTION_SENDTO,
                Intent.ACTION_VIEW -> {
                    val extrasMail = extras as IntentExtrasData.IntentExtrasMailTo
                    host.exitToScene(ComposerParams(type = ComposerType.MailTo(extrasMail.mailTo), currentLabel = currentLabel), null, false, true)
                }
                Intent.ACTION_APP_ERROR -> {
                    val extrasMail = extras as IntentExtrasData.IntentErrorMessage
                    return extrasMail.uiMessage
                }
                Intent.ACTION_SEND_MULTIPLE,
                Intent.ACTION_SEND -> {
                    val extrasMail = extras as IntentExtrasData.IntentExtrasSend
                    val composerMessage = if(extrasMail.files.isNotEmpty()) ActivityMessage.AddAttachments(extrasMail.files, true)
                    else ActivityMessage.AddUrls(extrasMail.urls, true)
                    host.exitToScene(ComposerParams(type = ComposerType.Empty(), currentLabel = currentLabel), composerMessage, false, true)
                }
            }
            return null
        }
    }
}