package com.criptext.mail.scenes.composer

import com.criptext.mail.db.models.Contact
import com.criptext.mail.scenes.composer.data.ComposerInputData
import com.criptext.mail.scenes.composer.data.ComposerType
import com.criptext.mail.utils.EmailAddressUtils
import com.criptext.mail.utils.EmailAddressUtils.isFromCriptextDomain
import com.criptext.mail.utils.compat.HtmlCompat

/**
 * Created by gabriel on 8/30/17.
 */

object Validator {

    fun validateContacts(data: ComposerInputData): AddressError? {
        val isValid = { contact: Contact -> contact is Contact.Invalid }
        val invalidToContact = data.to.find(isValid)
        if (invalidToContact != null)
            return AddressError(AddressError.Types.to, invalidToContact.email)

        val invalidCcContact = data.cc.find(isValid)
        if (invalidCcContact != null)
            return AddressError(AddressError.Types.cc, invalidCcContact.email)

        val invalidBccContact = data.bcc.find(isValid)
        if (invalidBccContact != null)
            return AddressError(AddressError.Types.bcc, invalidBccContact.email)

        return null
    }

    fun criptextOnlyContacts(data: ComposerInputData): Boolean {
        for(toContact in data.to){
            if(!toContact.isCriptextDomain) return false
        }
        for(ccContact in data.cc){
            if(!ccContact.isCriptextDomain) return false
        }
        for(bccContact in data.bcc){
            if(!bccContact.isCriptextDomain) return false
        }
        return true
    }

    fun mailHasMoreThanSignature(data: ComposerInputData, rawSignature: String,
                                 originalRawBody: String, type: ComposerType, attachmentsChanged: Boolean) : Boolean{

        val subject = data.subject
        val body = HtmlCompat.fromHtml(data.body).toString()
                .replace("\n", "").replace("\r", "")
        val signature = HtmlCompat.fromHtml(rawSignature).toString()
                .replace("\n", "").replace("\r", "")
        val originalBody = HtmlCompat.fromHtml(originalRawBody).toString()
                .replace("\n", "").replace("\r", "")

        if(body != signature && body != originalBody) {
            return true
        }

        if ((data.hasAtLeastOneRecipient || subject.isNotEmpty())
                && (type !is ComposerType.Reply && type !is ComposerType.ReplyAll
                        && type !is ComposerType.Support && type !is ComposerType.Report)) {
            return true
        }

        return attachmentsChanged
    }
}

