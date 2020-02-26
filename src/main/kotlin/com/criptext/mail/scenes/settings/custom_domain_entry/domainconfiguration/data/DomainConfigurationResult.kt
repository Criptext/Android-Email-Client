package com.criptext.mail.scenes.settings.custom_domain_entry.domainconfiguration.data

import com.criptext.mail.scenes.settings.custom_domain_entry.data.DomainMXRecordsData
import com.criptext.mail.utils.UIMessage

sealed class DomainConfigurationResult{

    sealed class GetMXRecords: DomainConfigurationResult() {
        data class Success(val mxRecords: List<DomainMXRecordsData>): GetMXRecords()
        data class Failure(val message: UIMessage): GetMXRecords()
        data class NotFound(val message: UIMessage): GetMXRecords()
    }

    sealed class ValidateDomain: DomainConfigurationResult() {
        class Success: ValidateDomain()
        data class Failure(val errorCode: Int?, val message: UIMessage): ValidateDomain()
    }

}