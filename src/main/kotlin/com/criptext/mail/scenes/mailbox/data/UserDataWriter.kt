package com.criptext.mail.scenes.mailbox.data

import com.criptext.mail.db.dao.*
import com.criptext.mail.db.models.*
import org.json.JSONObject
import java.io.File

class UserDataWriter(private val emailDao: EmailDao,
                     private val contactDao: ContactDao,
                     private val fileDao: FileDao,
                     private val labelDao: LabelDao,
                     private val emailLabelDao: EmailLabelDao,
                     private val emailContactDao: EmailContactJoinDao,
                     private val fileKeyDao: FileKeyDao)
{

    fun createFile():String? {
        try {


            val tmpFileLinkData = createTempFile()

            addContactsToFile(contactDao.getAll(), tmpFileLinkData)
            addLabelsToFile(labelDao.getAll(), tmpFileLinkData)
            addFilesToFile(fileDao.getAll(), tmpFileLinkData)
            addMailsToFile(emailDao.getAll(), tmpFileLinkData)
            addMailsAndLabelsRelationToFile(emailLabelDao.getAll(), tmpFileLinkData)
            addMailsAndContactsRelationToFile(emailContactDao.getAll(), tmpFileLinkData)
            addMailsFileKey(fileKeyDao.getAll(), tmpFileLinkData)


            return tmpFileLinkData.absolutePath
        }catch (ex:Exception){
            return null
        }
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
            val jsonObject = JSONObject()
            jsonObject.put("id", label.id)
            jsonObject.put("color", label.color)
            jsonObject.put("text", label.text)
            jsonObject.put("type", label.type)
            jsonObject.put("visible", label.visible)
            jsonArrayAllLabels.add(jsonObject.toString())
            tmpFile.appendText("${JSONObject("{table: label, object: $jsonObject}")}\n")
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
            jsonObject.put("date", file.date)
            jsonObject.put("readOnly", file.readOnly)
            jsonObject.put("emailId", file.emailId)
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
            jsonObject.put("delivered", mail.delivered)
            jsonObject.put("date", mail.date)
            jsonObject.put("metadataKey", mail.metadataKey)
            jsonObject.put("isMuted", mail.isMuted)
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
            jsonObject.put("type", mail_contact.type)
            jsonArrayAllMails.add(jsonObject.toString())
            tmpFile.appendText("${JSONObject("{table: email_contact, object: $jsonObject}")}\n")
        }
    }

    private fun addMailsFileKey(allFileKeys: List<FileKey>, tmpFile: File)
    {
        val jsonArrayAllMails = mutableListOf<String>()
        for (file_key in allFileKeys){
            val jsonObject = JSONObject()
            jsonObject.put("id", file_key.id)
            jsonObject.put("key", file_key.key)
            jsonObject.put("emailId", file_key.emailId)
            jsonArrayAllMails.add(jsonObject.toString())
            tmpFile.appendText("${JSONObject("{table: file_key, object: $jsonObject}")}\n")
        }
    }
}