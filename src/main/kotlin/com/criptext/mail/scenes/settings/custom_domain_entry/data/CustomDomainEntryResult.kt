package com.criptext.mail.scenes.settings.custom_domain_entry.data

import com.criptext.mail.db.models.CustomDomain
import com.criptext.mail.utils.UIMessage

sealed class CustomDomainEntryResult{

    sealed class CheckDomainAvailability: CustomDomainEntryResult() {
        data class Success(val domain: String): CheckDomainAvailability()
        data class Failure(val message: UIMessage): CheckDomainAvailability()
    }

    sealed class RegisterDomain: CustomDomainEntryResult() {
        data class Success(val domain: CustomDomain): RegisterDomain()
        data class Failure(val message: UIMessage): RegisterDomain()
    }

}