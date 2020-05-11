package com.criptext.mail.utils.generaldatasource.data

import androidx.appcompat.app.AppCompatDelegate
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.Account
import com.criptext.mail.utils.AccountUtils
import org.json.JSONObject
import java.util.*

data class BackupFileMetadata(val fileVersion: Int, val recipientId: String, val domain: String,
                              val language: String, val darkTheme: Boolean, val signature: String,
                              val showPreview: Boolean, val hasCriptextFooter: Boolean){
    companion object {
        fun fromJSON(jsonString: String, account: Account, storage: KeyValueStorage): BackupFileMetadata{
            val json = JSONObject(jsonString)
            val showFooter = AccountUtils.hasCriptextFooter(account, storage)
            return BackupFileMetadata(
                    fileVersion = json.getInt("fileVersion"),
                    recipientId = json.getString("recipientId"),
                    domain = json.getString("domain"),
                    signature = if(json.has("signature")) json.getString("signature")
                    else account.signature,
                    darkTheme = if(json.has("darkTheme")) json.getBoolean("darkTheme")
                    else AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES,
                    hasCriptextFooter = if(json.has("hasCriptextFooter")) json.getBoolean("hasCriptextFooter")
                    else showFooter,
                    language = if(json.has("language")) json.getString("language")
                    else Locale.getDefault().language,
                    showPreview = if(json.has("showPreview")) json.getBoolean("showPreview")
                    else storage.getBool(KeyValueStorage.StringKey.ShowEmailPreview, true)
            )
        }

        fun toJSON(data: BackupFileMetadata): JSONObject {
            val json = JSONObject()
            json.put("fileVersion", data.fileVersion)
            json.put("recipientId", data.recipientId)
            json.put("domain", data.domain)
            json.put("signature", data.signature)
            json.put("darkTheme", data.darkTheme)
            json.put("hasCriptextFooter", data.hasCriptextFooter)
            json.put("language", data.language)
            json.put("showPreview", data.showPreview)
            return json
        }
    }
}