package com.criptext.mail.scenes.settings.custom_domain.data

import com.criptext.mail.db.models.CustomDomain
import com.criptext.mail.utils.UIMessage

sealed class CustomDomainResult{

    sealed class DeleteDomain: CustomDomainResult() {
        data class Success(val domain: String, val position: Int): DeleteDomain()
        data class Failure(val message: UIMessage): DeleteDomain()
    }

    sealed class LoadDomain: CustomDomainResult() {
        data class Success(val domains: List<CustomDomain>): LoadDomain()
        data class Failure(val message: UIMessage): LoadDomain()
    }

}