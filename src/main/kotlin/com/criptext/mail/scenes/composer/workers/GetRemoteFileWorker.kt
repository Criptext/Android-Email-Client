package com.criptext.mail.scenes.composer.workers

import android.accounts.NetworkErrorException
import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.criptext.mail.R
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.scenes.composer.data.ComposerResult
import com.criptext.mail.utils.UIMessage
import org.json.JSONException
import java.io.File

class GetRemoteFileWorker(private val uris: List<String>,
                          private val contentResolver: ContentResolver,
                          override val publishFn: (ComposerResult.GetRemoteFile) -> Unit)
    : BackgroundWorker<ComposerResult.GetRemoteFile> {
    override val canBeParallelized = true

    override fun catchException(ex: Exception): ComposerResult.GetRemoteFile =
            ComposerResult.GetRemoteFile.Failure(createErrorMessage(ex), ex)

    override fun work(reporter: ProgressReporter<ComposerResult.GetRemoteFile>)
            : ComposerResult.GetRemoteFile? {

        val attachmentList = mutableListOf<Pair<String, Long>>()

        for (uri in uris) {
            val realUri = Uri.parse(uri)
            val extension = if (realUri.scheme == ContentResolver.SCHEME_CONTENT) {
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
            val file = createTempFile(prefix = name ?: "tmp", suffix = ".".plus(extension))

            val stream = contentResolver.openInputStream(realUri)
            stream.use { input ->
                File(file.absolutePath).outputStream().use { input.copyTo(it) }
            }

            attachmentList.add(Pair(file.absolutePath, file.length()))
        }
        return ComposerResult.GetRemoteFile.Success(attachmentList)
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        ex.printStackTrace()
        when (ex) { // these are not the real errors TODO fix!
            is JSONException -> UIMessage(resId = R.string.json_error_exception)
            is ServerErrorException -> UIMessage(resId = R.string.server_error_exception)
            is NetworkErrorException -> UIMessage(resId = R.string.network_error_exception)
            else -> UIMessage(resId = R.string.fail_register_try_again_error_exception)
        }
    }
    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    companion object {
        private const val chunkSize = 512 * 1024

    }

}