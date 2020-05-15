package com.criptext.mail.scenes.settings.custom_domain_entry.domainconfiguration.data

sealed class DomainConfigurationRequest{
    data class GetMXRecords(val domain: String): DomainConfigurationRequest()
    data class ValidateDomain(val domain: String): DomainConfigurationRequest()
}