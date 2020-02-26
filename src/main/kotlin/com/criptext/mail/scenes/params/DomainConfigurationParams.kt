package com.criptext.mail.scenes.params

import com.criptext.mail.db.models.CustomDomain
import com.criptext.mail.scenes.settings.custom_domain_entry.domainconfiguration.DomainConfigurationActivity

class DomainConfigurationParams(val domain: CustomDomain): SceneParams() {
    override val activityClass = DomainConfigurationActivity::class.java
}