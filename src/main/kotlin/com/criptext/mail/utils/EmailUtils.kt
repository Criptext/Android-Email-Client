package com.criptext.mail.utils

import com.criptext.mail.db.models.Contact

object EmailUtils {

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

    class MailRecipients(val toCriptext: List<String>, val ccCriptext: List<String>,
                         val bccCriptext: List<String>, val peerCriptext: List<String>) {
        val criptextRecipients = listOf(toCriptext, ccCriptext, bccCriptext, peerCriptext).flatten()
        val isEmpty = toCriptext.isEmpty() && ccCriptext.isEmpty() && bccCriptext.isEmpty()
    }
}