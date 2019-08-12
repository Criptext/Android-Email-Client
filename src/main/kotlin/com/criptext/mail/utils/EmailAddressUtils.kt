package com.criptext.mail.utils

import com.criptext.mail.BuildConfig
import com.criptext.mail.validation.AccountDataValidator
import com.criptext.mail.validation.FormData

/**
 * Created by gabriel on 3/23/18.
 */

object EmailAddressUtils {

    val CRIPTEXT_DOMAIN_SUFFIX = "@criptext.com"

    val isFromCriptextDomain:(String) -> Boolean =
            { address -> address.endsWith(CRIPTEXT_DOMAIN_SUFFIX) }
    val extractRecipientIdFromCriptextAddress: (String) -> String =
            { address ->
                if(address.isEmpty() || (AccountDataValidator.validateEmailAddress(address) is FormData.Error)) {
                    address
                } else {
                    address.substring(0, address.length - CRIPTEXT_DOMAIN_SUFFIX.length)
                }
            }

    val extractRecipientIdFromAddress: (String, String) -> String =
            { address, domain ->
                if (address.isEmpty() || domain.isEmpty() || (AccountDataValidator.validateEmailAddress(address) is FormData.Error)) {
                    address
                } else {
                    address.substring(0, address.length - (domain.length + 1))
                }
            }

    fun checkIfOnlyHasEmail(contactAddress: String): Boolean{
        if(contactAddress.contains("<") && contactAddress.lastIndexOf("<") == 0)
            return true
        if(!contactAddress.contains("<") && !contactAddress.contains(">"))
            return true
        if(contactAddress.contains("@") && !contactAddress.contains(" "))
            return true
        return false
    }

    fun extractEmailAddress(contactAddress: String): String{
        val leftBracket = contactAddress.lastIndexOf("<")
        val rightBracket = contactAddress.lastIndexOf(">")
        if(rightBracket == -1 || leftBracket == -1) return contactAddress
        val realEmail = contactAddress.substring(leftBracket + 1, rightBracket)
        return realEmail.replace("<", "")
                .replace(">", "")
                .replace("\"", "")
                .toLowerCase()
    }

    fun extractEmailAddressDomain(contactAddress: String): String{
        return contactAddress.substring(contactAddress.lastIndexOf("@") + 1)
    }

    fun extractName(contactAddress: String): String{
        val leftBracket = contactAddress.lastIndexOf("<")
        val rightBracket = contactAddress.lastIndexOf(">")
        if(rightBracket == -1 || leftBracket == -1) return contactAddress
        val realName =
                when {
                    leftBracket > 0 -> contactAddress.substring(0, leftBracket - 1)
                    contactAddress.contains("@") -> contactAddress.split("@")[0]
                    else -> contactAddress
                }.removeSurrounding("'")
        return realName.replace("\"", "")
                .replace("<", "")
                .replace(">", "")
    }

    fun hideEmailAddress(emailAddress: String): String{
        val listOfNonReplace = listOf(
                emailAddress.lastIndexOf("."),
                emailAddress.lastIndexOf(".")-1,
                emailAddress.indexOf("@"),
                emailAddress.indexOf("@") + 1
        )
        var hiddenEmail = ""
        for (i in 0 until emailAddress.length){
            hiddenEmail += if(i == 0 || i in listOfNonReplace || i > listOfNonReplace[0])
                emailAddress[i]
            else
                "*"
        }
        return hiddenEmail
    }
}