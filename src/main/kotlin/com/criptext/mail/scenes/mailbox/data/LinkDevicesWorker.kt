package com.criptext.mail.scenes.mailbox.data

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.dao.*
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.utils.UIMessage
import java.io.File


class LinkDevicesWorker(
        private val signalClient: SignalClient,
        private val emailDao: EmailDao,
        private val contactDao: ContactDao,
        private val fileDao: FileDao,
        private val fileKeyDao: FileKeyDao,
        private val labelDao: LabelDao,
        private val emailLabelDao: EmailLabelDao,
        private val emailContactJoinDao: EmailContactJoinDao,
        private val activeAccount: ActiveAccount,
        httpClient: HttpClient,
        override val publishFn: (
                MailboxResult.LinkDevice) -> Unit)
    : BackgroundWorker<MailboxResult.LinkDevice> {

    private val apiClient = MailboxAPIClient(httpClient, activeAccount.jwt)

    override val canBeParallelized = true

    private lateinit var filePath: String

    override fun catchException(ex: Exception): MailboxResult.LinkDevice {
        val message = UIMessage(resId = R.string.failed_to_create_link_device_file)
        return MailboxResult.LinkDevice.Failure(
                message = message)
    }

    override fun work(reporter: ProgressReporter<MailboxResult.LinkDevice>)
            : MailboxResult.LinkDevice? {
        val dataWriter = UserDataWriter(emailDao,contactDao,fileDao,labelDao, emailLabelDao, emailContactJoinDao, fileKeyDao)
        val getFileResult = dataWriter.createFile()
        return if(getFileResult != null){
            filePath = getFileResult
            val fileByChunks = signalClient.encryptFileByChunks(File(filePath), activeAccount.recipientId, activeAccount.deviceId,512000)
            MailboxResult.LinkDevice.Success(fileByChunks)
        }else
            MailboxResult.LinkDevice.Failure(UIMessage(resId = R.string.failed_to_create_link_device_file))

    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }
}