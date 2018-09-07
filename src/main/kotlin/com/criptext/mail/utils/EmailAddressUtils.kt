package com.criptext.mail.utils

/**
 * Created by gabriel on 3/23/18.
 */

object EmailAddressUtils {
    private val CRIPTEXT_DOMAIN_SUFFIX = "@criptext.com"
    val isFromCriptextDomain:(String) -> Boolean =
            { address -> address.endsWith(CRIPTEXT_DOMAIN_SUFFIX) }
    val extractRecipientIdFromCriptextAddress: (String) -> String =
            { address -> address.substring(0, address.length - CRIPTEXT_DOMAIN_SUFFIX.length) }

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

    fun extractName(contactAddress: String): String{
        val leftBracket = contactAddress.lastIndexOf("<")
        val rightBracket = contactAddress.lastIndexOf(">")
        if(rightBracket == -1 || leftBracket == -1) return contactAddress
        val realName =
                when {
                    leftBracket > 0 -> contactAddress.substring(0, leftBracket - 1)
                    contactAddress.contains("@") -> contactAddress.split("@")[0]
                    else -> contactAddress
                }
        return realName.replace("\"", "")
                .replace("<", "")
                .replace(">", "")
    }
}