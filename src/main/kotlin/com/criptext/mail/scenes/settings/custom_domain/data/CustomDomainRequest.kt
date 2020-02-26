package com.criptext.mail.scenes.settings.custom_domain.data

sealed class CustomDomainRequest{
    data class DeleteDomain(val domain: String, val position: Int): CustomDomainRequest()
    class LoadDomain: CustomDomainRequest()
}