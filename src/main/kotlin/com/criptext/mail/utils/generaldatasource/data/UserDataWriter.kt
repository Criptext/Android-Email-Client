package com.criptext.mail.utils.generaldatasource.data

import android.arch.persistence.db.SupportSQLiteQuery
import android.database.sqlite.SQLiteQuery
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.LabelTypes
import com.criptext.mail.db.dao.*
import com.criptext.mail.db.models.*
import com.criptext.mail.utils.DateUtils
import com.criptext.mail.utils.batch
import droidninja.filepicker.utils.FileUtils
import org.json.JSONObject
import java.io.File

class UserDataWriter(private val db: AppDatabase)
{

    fun createFile():String? {
        try {


            val tmpFileLinkData = createTempFile()

            addContactsToFile(db.contactDao().getAll(), tmpFileLinkData)
            addLabelsToFile(db.labelDao().getAll(), tmpFileLinkData)
            addMailsToFile(db.emailDao().getAllForLinkFile(), tmpFileLinkData)
            addFilesToFile(db.fileDao().getAllForLinkFile(), tmpFileLinkData)
            addMailsAndLabelsRelationToFile(db.emailLabelDao().getAllForLinkFile(), tmpFileLinkData)
            addMailsAndContactsRelationToFile(db.emailContactDao().getAllForLinkFile(), tmpFileLinkData)
            addMailsFileKey(db.fileKeyDao().getAllForLinkFile(), tmpFileLinkData)

            return tmpFileLinkData.absolutePath
        }catch (ex:Exception){
            return null
        }
    }

    fun createDBFromFile(file: File) {
        val contactWriter = ContactDataWriter(db.contactDao())
        val labelWriter = LabelDataWriter(db.labelDao())
        val emailWriter = EmailDataWriter(db.emailDao())
        val fileWriter = FileDataWriter(db.fileDao(), listOf(emailWriter))
        val emailLabelWriter = EmailLabelDataWriter(db.emailLabelDao(), listOf(labelWriter, emailWriter))
        val emailContactWriter = EmailContactDataWriter(db.emailContactDao(), listOf(contactWriter, emailWriter))
        val fileKeyWriter = FileKeyDataWriter(db.fileKeyDao(), listOf(emailWriter, fileWriter))
        val data = file.bufferedReader()
        var line = data.readLine()
        db.beginTransaction()
        while(line != null && line.isNotEmpty()) {
            val json = JSONObject(line)
            when (json.getString("table")) {
                "contact" -> contactWriter.insert(json.get("object").toString())
                "label" -> labelWriter.insert(json.get("object").toString())
                "email" -> emailWriter.insert(json.get("object").toString())
                "file" ->  fileWriter.insert(json.get("object").toString())
                "email_label" -> emailLabelWriter.insert(json.get("object").toString())
                "email_contact" -> emailContactWriter.insert(json.get("object").toString())
                "filekey" -> fileKeyWriter.insert(json.get("object").toString())
            }
            line = data.readLine()
        }
        contactWriter.flush()
        labelWriter.flush()
        emailWriter.flush()
        fileWriter.flush()
        emailLabelWriter.flush()
        emailContactWriter.flush()
        fileKeyWriter.flush()


        db.setTransactionSuccessful()
        db.endTransaction()
    }

    private fun addContactsToFile(allContacts: List<Contact>, tmpFile: File)
    {
        val jsonArrayAllContacts = mutableListOf<String>()
        for (contact in allContacts){
            val jsonObject = JSONObject()
            jsonObject.put("id", contact.id)
            jsonObject.put("email", contact.email)
            jsonObject.put("name", contact.name)
            jsonArrayAllContacts.add(jsonObject.toString())
            tmpFile.appendText("${JSONObject("{table: contact, object: $jsonObject}")}\n")
        }
    }

    private fun addLabelsToFile(allLabels: List<Label>, tmpFile: File)
    {
        val jsonArrayAllLabels = mutableListOf<String>()
        for (label in allLabels){
            if(label.type == LabelTypes.CUSTOM) {
                val jsonObject = JSONObject()
                jsonObject.put("id", label.id)
                jsonObject.put("color", label.color)
                jsonObject.put("text", label.text)
                jsonObject.put("type", label.type.name.toLowerCase())
                jsonObject.put("visible", label.visible)
                jsonArrayAllLabels.add(jsonObject.toString())
                tmpFile.appendText("${JSONObject("{table: label, object: $jsonObject}")}\n")
            }
        }
    }

    private fun addFilesToFile(allFiles: List<CRFile>, tmpFile: File)
    {
        val jsonArrayAllFiles = mutableListOf<String>()
        for (file in allFiles){
            val jsonObject = JSONObject()
            jsonObject.put("id", file.id)
            jsonObject.put("token", file.token)
            jsonObject.put("name", file.name)
            jsonObject.put("size", file.size)
            jsonObject.put("status", file.status)
            jsonObject.put("date", DateUtils.printDateWithServerFormat(file.date))
            jsonObject.put("readOnly", file.readOnly)
            jsonObject.put("emailId", file.emailId)
            jsonObject.put("mimeType", com.criptext.mail.utils.file.FileUtils.getMimeType(file.name))
            jsonArrayAllFiles.add(jsonObject.toString())
            tmpFile.appendText("${JSONObject("{table: file, object: $jsonObject}")}\n")
        }
    }

    private fun addMailsToFile(allMails: List<Email>, tmpFile: File)
    {
        val jsonArrayAllMails = mutableListOf<String>()
        for (mail in allMails){
            val jsonObject = JSONObject()
            jsonObject.put("id", mail.id)
            jsonObject.put("messageId", mail.messageId)
            jsonObject.put("threadId", mail.threadId)
            jsonObject.put("unread", mail.unread)
            jsonObject.put("secure", mail.secure)
            jsonObject.put("content", mail.content)
            jsonObject.put("preview", mail.preview)
            jsonObject.put("subject", mail.subject)
            jsonObject.put("status", DeliveryTypes.getTrueOrdinal(mail.delivered))
            jsonObject.put("date", DateUtils.printDateWithServerFormat(mail.date))
            jsonObject.put("key", mail.metadataKey)
            jsonObject.put("isMuted", mail.isMuted)
            if(mail.unsentDate != null)
                jsonObject.put("unsentDate",DateUtils.printDateWithServerFormat(mail.unsentDate!!))
            if(mail.trashDate != null)
                jsonObject.put("trashDate", DateUtils.printDateWithServerFormat(mail.trashDate!!))
            jsonArrayAllMails.add(jsonObject.toString())
            tmpFile.appendText("${JSONObject("{table: email, object: $jsonObject}")}\n")
        }
    }

    private fun addMailsAndLabelsRelationToFile(allMailsAndLabelsRelation: List<EmailLabel>, tmpFile: File)
    {
        val jsonArrayAllMails = mutableListOf<String>()
        for (mail_label in allMailsAndLabelsRelation){
            val jsonObject = JSONObject()
            jsonObject.put("emailId", mail_label.emailId)
            jsonObject.put("labelId", mail_label.labelId)
            jsonArrayAllMails.add(jsonObject.toString())
            tmpFile.appendText("${JSONObject("{table: email_label, object: $jsonObject}")}\n")
        }
    }

    private fun addMailsAndContactsRelationToFile(allMails: List<EmailContact>, tmpFile: File)
    {
        val jsonArrayAllMails = mutableListOf<String>()
        for (mail_contact in allMails){
            val jsonObject = JSONObject()
            jsonObject.put("id", mail_contact.id)
            jsonObject.put("emailId", mail_contact.emailId)
            jsonObject.put("contactId", mail_contact.contactId)
            jsonObject.put("type", mail_contact.type.name.toLowerCase())
            jsonArrayAllMails.add(jsonObject.toString())
            tmpFile.appendText("${JSONObject("{table: email_contact, object: $jsonObject}")}\n")
        }
    }

    private fun addMailsFileKey(allFileKeys: List<FileKey>, tmpFile: File)
    {
        val jsonArrayAllMails = mutableListOf<String>()
        for (file_key in allFileKeys){
            val jsonObject = JSONObject()
            val key = if(file_key.key != null)
                file_key.key
            else
                null
            if(key != null && key.contains(":")) {
                jsonObject.put("id", file_key.id)
                jsonObject.put("key", key.substringBefore(":"))
                jsonObject.put("iv", key.substringAfter(":"))
                jsonObject.put("emailId", file_key.emailId)
                jsonArrayAllMails.add(jsonObject.toString())
                tmpFile.appendText("${JSONObject("{table: filekey, object: $jsonObject}")}\n")
            }
        }
    }

    companion object {
        const val DEFAULT_BATCH_SIZE = 50
        const val EMAIL_BATCH_SIZE = 10
        const val RELATIONS_BATCH_SIZE = 100

    }
}