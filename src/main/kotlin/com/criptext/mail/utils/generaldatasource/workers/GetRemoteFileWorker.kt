package com.criptext.mail.utils.generaldatasource.workers

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.criptext.mail.R
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.utils.EventHelper
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.file.FileUtils
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.github.kittinunf.result.Result
import java.io.File

class GetRemoteFileWorker(private val uris: List<String>,
                          private val contentResolver: ContentResolver,
                          override val publishFn: (GeneralResult.GetRemoteFile) -> Unit)
    : BackgroundWorker<GeneralResult.GetRemoteFile> {
    override val canBeParallelized = true

    override fun catchException(ex: Exception): GeneralResult.GetRemoteFile =
            GeneralResult.GetRemoteFile.Failure(createErrorMessage(ex), ex)

    override fun work(reporter: ProgressReporter<GeneralResult.GetRemoteFile>)
            : GeneralResult.GetRemoteFile? {
        val attachmentList = mutableListOf<Pair<String, Long>>()

        val operation = Result.of {
            for (uri in uris) {
                val realUri = Uri.parse(uri)
                var extension = if (realUri.scheme == ContentResolver.SCHEME_CONTENT) {
                    val mime = MimeTypeMap.getSingleton()
                    mime.getExtensionFromMimeType(contentResolver.getType(realUri))
                } else {
                    MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(uri)).toString())
                }
                val name = contentResolver.query(realUri, null, null, null, null)?.use {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    it.moveToFirst()
                    it.getString(nameIndex)
                }

                var cleanedName = if(name == null) null
                else {
                    val baseAndExtension = FileUtils.getBasenameAndExtension(name)
                    extension = if(extension?.isEmpty() != false) baseAndExtension.second else extension
                    baseAndExtension.first
                }
                if(cleanedName != null && cleanedName.length < 3) cleanedName = cleanedName.plus("_tmp")
                val file = createTempFile(prefix = cleanedName ?: "tmp", suffix = ".".plus(extension))

                val stream = contentResolver.openInputStream(realUri)
                stream.use { input ->
                    if(input != null)
                        File(file.absolutePath).outputStream().use { input.copyTo(it) }
                }

                attachmentList.add(Pair(file.absolutePath, file.length()))
            }
        }
        return when(operation){
            is Result.Success -> {
                if(attachmentList.isNotEmpty()) {
                    GeneralResult.GetRemoteFile.Success(attachmentList)
                } else {
                    catchException(EventHelper.NothingNewException())
                }
            }
            is Result.Failure -> {
                catchException(operation.error)
            }
        }

    }
    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        ex.printStackTrace()
        when (ex) {
            is ServerErrorException -> UIMessage(resId = R.string.server_bad_status, args = arrayOf(ex.errorCode))
            else -> UIMessage(resId = R.string.error_downloading_file)
        }
    }
    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

}