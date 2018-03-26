package com.email.utils

/**
 * Created by gabriel on 3/23/18.
 */

object EmailAddressUtils {
    private val CRIPTEXT_DOMAIN_SUFFIX = "@jigl.com"
    val isFromCriptextDomain:(String) -> Boolean =
            { address -> address.endsWith(CRIPTEXT_DOMAIN_SUFFIX) }
    val extractRecipientIdFromCriptextAddress: (String) -> String =
            { address -> address.substring(0, address.length - CRIPTEXT_DOMAIN_SUFFIX.length) }
}