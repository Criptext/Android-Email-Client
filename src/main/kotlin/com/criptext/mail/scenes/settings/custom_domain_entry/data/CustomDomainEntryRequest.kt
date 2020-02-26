package com.criptext.mail.scenes.settings.custom_domain_entry.data

sealed class CustomDomainEntryRequest{
    data class CheckDomainAvailability(val domain: String): CustomDomainEntryRequest()
    data class RegisterDomain(val domain: String): CustomDomainEntryRequest()
}