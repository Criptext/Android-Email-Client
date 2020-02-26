package com.criptext.mail.scenes.settings.custom_domain_entry.domainconfiguration

import com.criptext.mail.R
import com.criptext.mail.db.models.CustomDomain
import com.criptext.mail.scenes.SceneModel
import com.criptext.mail.scenes.settings.custom_domain_entry.data.DomainMXRecordsData

class DomainConfigurationModel(val domain: CustomDomain): SceneModel{
    enum class StepState{
        FIRST, SECOND, THIRD;
    }
    var state = Pair(StepState.FIRST, R.layout.activity_domain_configuration_step_1)
    var mxRecords: List<DomainMXRecordsData> = listOf()
    var validationSuccess: Boolean = false
    var validationError: Boolean = false
    var retryTimeValidateRecords = 0
}