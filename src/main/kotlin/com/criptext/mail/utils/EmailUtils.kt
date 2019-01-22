package com.criptext.mail.utils

import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.Email
import com.github.kittinunf.result.Result
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.File
import java.lang.StringBuilder


object EmailUtils {

    const val RECIPIENT_LIMIT = 300
    const val ATTACHMENT_LIMIT = 5
    const val ATTACHMENT_SIZE_LIMIT = 25000000

    fun getMailRecipients(to: List<Contact>, cc: List<Contact>, bcc: List<Contact>,
                                  recipientId: String): MailRecipients {
        val toAddresses = to.map(Contact.toAddress)
        val ccAddresses = cc.map(Contact.toAddress)
        val bccAddresses = bcc.map(Contact.toAddress)

        val toCriptext = toAddresses.filter(EmailAddressUtils.isFromCriptextDomain)
                .map(EmailAddressUtils.extractRecipientIdFromCriptextAddress)
        val ccCriptext = ccAddresses.filter(EmailAddressUtils.isFromCriptextDomain)
                .map(EmailAddressUtils.extractRecipientIdFromCriptextAddress)
        val bccCriptext = bccAddresses.filter(EmailAddressUtils.isFromCriptextDomain)
                .map(EmailAddressUtils.extractRecipientIdFromCriptextAddress)

        return MailRecipients(toCriptext = toCriptext, ccCriptext = ccCriptext,
                bccCriptext = bccCriptext, peerCriptext = listOf(recipientId))
    }

    fun getMailRecipientsNonCriptext(to: List<Contact>, cc: List<Contact>, bcc: List<Contact>,
                                             recipientId: String): MailRecipients {
        val toAddresses = to.map(Contact.toAddress)
        val ccAddresses = cc.map(Contact.toAddress)
        val bccAddresses = bcc.map(Contact.toAddress)

        val toNonCriptext = toAddresses.filterNot(EmailAddressUtils.isFromCriptextDomain)
        val ccNonCriptext = ccAddresses.filterNot(EmailAddressUtils.isFromCriptextDomain)
        val bccNonCriptext = bccAddresses.filterNot(EmailAddressUtils.isFromCriptextDomain)

        return MailRecipients(toCriptext = toNonCriptext, ccCriptext = ccNonCriptext,
                bccCriptext = bccNonCriptext, peerCriptext = listOf(recipientId))
    }

    fun getThreadIdForSending(db: MailboxLocalDB, threadId: String?, emailId: Long): String?{
        val email = db.getEmailById(emailId)
        if(email != null){
            val threadIdToLong = Result.of {email.threadId.toLong()}
            when(threadIdToLong){
                is Result.Success -> if (threadIdToLong.value == email.metadataKey) return null
                is Result.Failure -> return email.threadId
            }
        }
        return threadId
    }

    fun getThreadIdForSending(email: Email): String?{
        val threadIdToLong = Result.of {email.threadId.toLong()}
        when(threadIdToLong){
            is Result.Success -> if (threadIdToLong.value == email.metadataKey) return null
            is Result.Failure -> return email.threadId
        }
        return email.threadId
    }

    fun deleteEmailInFileSystem(filesDir: File, recipientId: String, metadataKey: Long){
        Result.of {
            val emailDir = File(filesDir.path + "/$recipientId/emails/$metadataKey")
            if (emailDir.isDirectory) {
                val children = emailDir.list()
                for (i in children.indices) {
                    File(emailDir, children[i]).delete()
                }
            }
            emailDir.delete()
        }
    }

    fun deleteEmailsInFileSystem(filesDir: File, recipientId: String){
        Result.of {
            val emailDir = File(filesDir.path + "/$recipientId/emails/")
            if (emailDir.isDirectory) {
                val children = emailDir.list()
                for (i in children.indices) {
                    File(emailDir, children[i]).delete()
                }
            }
            emailDir.delete()
        }
    }

    fun saveEmailInFileSystem(filesDir: File, recipientId: String, metadataKey: Long, content: String, headers: String?){
        val dir = File(filesDir.path + "/$recipientId/emails/")

        if(!dir.exists())
            dir.mkdirs()

        val emailDir = File(filesDir.path + "/$recipientId/emails/$metadataKey")
        emailDir.mkdir()


        writeToFile(emailDir.path + "/body.txt", content)
        writeToFile(emailDir.path + "/headers.txt", headers)
    }

    fun getEmailContentFromFileSystem(filesDir: File, metadataKey: Long, dbContent: String,
                                              recipientId: String): Pair<String, String?> {
        val dir = File(filesDir.path + "/$recipientId/emails/$metadataKey")
        if(!dir.exists()) return Pair(dbContent, null)

        val content = File(dir.path + "/body.txt")
        val headers = File(dir.path + "/headers.txt")

        if(!content.exists()) return Pair(dbContent, null)

        val text =  content.readText()
        val textHeaders = if(headers.exists()) headers.readText() else null
        return Pair(text, textHeaders)
    }

    private fun writeToFile(filePath: String, data: String?){
        val file = File(filePath)
        file.createNewFile()

        val dataByteArray = (data ?: "").toByteArray()
        val inputStream = BufferedInputStream(ByteArrayInputStream(dataByteArray))
        val outputStream = BufferedOutputStream(file.outputStream())
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.flush()
        outputStream.close()
    }

    fun getEmailSource(headers: String, boundary: String, content: String): String {
        val builder = StringBuilder()
        builder.append("$headers\n")
        builder.append("--$boundary\n")
        builder.append("Content-Type: text/plain; charset=UTF-8\n")
        builder.append("Content-Transfer-Encoding: quoted-printable\n")
        builder.append("${HTMLUtils.html2text(content)}\n")
        builder.append("--$boundary\n")
        builder.append("Content-Type: text/html; charset=UTF-8\n")
        builder.append("Content-Transfer-Encoding: 7bit\n")
        builder.append("$content\n")
        builder.append("--$boundary\n")
        return builder.toString()
    }

    class MailRecipients(val toCriptext: List<String>, val ccCriptext: List<String>,
                         val bccCriptext: List<String>, val peerCriptext: List<String>) {
        val criptextRecipients = listOf(toCriptext, ccCriptext, bccCriptext, peerCriptext).flatten()
        val isEmpty = toCriptext.isEmpty() && ccCriptext.isEmpty() && bccCriptext.isEmpty()
    }
}