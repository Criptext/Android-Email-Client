package com.email.scenes.composer

import com.email.db.models.Contact
import com.email.scenes.composer.data.ComposerInputData
import com.email.utils.EmailAddressUtils
import com.email.utils.EmailAddressUtils.isFromCriptextDomain
import com.email.utils.compat.HtmlCompat

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
            if(!isFromCriptextDomain(toContact.email)) return false
        }
        for(ccContact in data.cc){
            if(!isFromCriptextDomain(ccContact.email)) return false
        }
        for(bccContact in data.bcc){
            if(!isFromCriptextDomain(bccContact.email)) return false
        }
        return true
    }

    fun mailHasMoreThanSignature(data: ComposerInputData, rawSignature: String) : Boolean{

        val subject = data.subject
        val body = HtmlCompat.fromHtml(data.body).toString()
                .replace("\n", "").replace("\r", "")
        val signature = HtmlCompat.fromHtml(rawSignature).toString()
                .replace("\n", "").replace("\r", "")

        if(body != signature) {
            return true
        }

        if (data.hasAtLeastOneRecipient || subject.isNotEmpty()) {
            return true
        }

        return false
    }
}

