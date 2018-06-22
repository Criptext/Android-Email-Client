package com.email.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.email.utils.file.FileUtils

/**
 * Created by hirobreak on 08/06/17.
 */

class DownloadHelper {

    companion object{
        fun createDownloader(mContext: Context, url: String, filename : String) : Long{
            val uri = Uri.parse(url)
            return downloadData(mContext, uri, filename)
        }

        fun downloadData(mContext : Context, uri : Uri, filename : String) : Long{
            val downloadManager = mContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(uri)
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
            request.setTitle(filename)
            request.setDescription("Criptext Secure CRFile")
            request.setMimeType(FileUtils.getMimeType(filename))
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
            return downloadManager.enqueue(request)
        }
    }
}
