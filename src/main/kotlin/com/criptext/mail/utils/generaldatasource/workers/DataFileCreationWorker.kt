package com.criptext.mail.utils.generaldatasource.workers

import com.criptext.mail.R
import com.criptext.mail.aes.AESUtil
import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.dao.*
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralAPIClient
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.generaldatasource.data.UserDataWriter
import java.io.File


class DataFileCreationWorker(
        private val db: AppDatabase,
        override val publishFn: (
                GeneralResult.DataFileCreation) -> Unit)
    : BackgroundWorker<GeneralResult.DataFileCreation> {

    override val canBeParallelized = true

    private lateinit var filePath: String

    override fun catchException(ex: Exception): GeneralResult.DataFileCreation {
        val message = UIMessage(resId = R.string.failed_to_create_link_device_file)
        return GeneralResult.DataFileCreation.Failure(
                message = message)
    }

    override fun work(reporter: ProgressReporter<GeneralResult.DataFileCreation>)
            : GeneralResult.DataFileCreation? {
        val dataWriter = UserDataWriter(db)
        val getFileResult = dataWriter.createFile()
        return if(getFileResult != null){
            filePath = getFileResult
            val fileByChunks = AESUtil.encryptFileByChunks(File(filePath))
            GeneralResult.DataFileCreation.Success(fileByChunks.first, fileByChunks.second)
        }else
            GeneralResult.DataFileCreation.Failure(UIMessage(resId = R.string.failed_to_create_link_device_file))

    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }
}