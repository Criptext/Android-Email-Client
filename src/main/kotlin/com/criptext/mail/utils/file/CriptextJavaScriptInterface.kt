package com.criptext.mail.utils.file

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.appcompat.app.AppCompatActivity
import android.util.Base64
import android.webkit.JavascriptInterface
import android.widget.Toast
import com.criptext.mail.BuildConfig
import com.criptext.mail.R
import com.criptext.mail.scenes.WebViewActivity
import org.json.JSONObject

/**
 * Created by hirobreak on 02/08/17.
 */
class CriptextJavaScriptInterface(val mContext: Context, val filename: String) {

    @JavascriptInterface
    fun retrieveData(data: String) {
        //val notificator = CriptextNotification(mContext)
        val bytesData = Base64.decode(data, 0)
        val file = AndroidFs.getFileFromDownloadsDir(filename)
        AndroidFs.writeByteArraysToFile(file, bytesData)

        val intent = Intent()
        intent.action = android.content.Intent.ACTION_VIEW
        intent.setDataAndType(FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID +
                ".provider", file), FileUtils.getMimeType(file.name))
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        //notificator.showNotificationFileDownloaded(file.name, intent)

        (mContext as AppCompatActivity).runOnUiThread {
            Toast.makeText(mContext, "Downloading file $filename",
                    Toast.LENGTH_LONG).show()
        }
    }

    @JavascriptInterface
    fun postMessage(jsonString: String) {
        val json = JSONObject(jsonString)
        val activity = mContext as WebViewActivity
        when(json.getString("type")){
            "close" -> {
                activity.onBackPressed()
            }
            "share" -> {
                val share = Intent(Intent.ACTION_SEND)
                share.type = "text/plain"
                share.putExtra(Intent.EXTRA_SUBJECT, "Invite a Friend")
                share.putExtra(Intent.EXTRA_TEXT, mContext.getString(R.string.invite_text))
                activity.startActivity(Intent.createChooser(share, mContext.getString(R.string.invite_title)))
            }
        }

    }
}
