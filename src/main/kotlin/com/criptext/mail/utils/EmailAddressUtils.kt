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
}