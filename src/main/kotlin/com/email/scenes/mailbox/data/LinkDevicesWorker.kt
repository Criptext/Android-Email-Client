package com.email.scenes.mailbox.data

import com.email.R
import com.email.api.HttpClient
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.dao.*
import com.email.db.models.ActiveAccount
import com.email.signal.SignalClient
import com.email.utils.UIMessage
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