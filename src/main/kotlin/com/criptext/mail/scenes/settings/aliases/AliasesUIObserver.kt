package com.criptext.mail.scenes.settings.aliases

import com.criptext.mail.IHostActivity
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.uiobserver.UIObserver

abstract class AliasesUIObserver(generalDataSource: GeneralDataSource, host: IHostActivity): UIObserver(generalDataSource, host) {
    abstract fun onBackButtonPressed()
    abstract fun onRemoveAlias(aliasName: String, domainName: String?, position: Int)
    abstract fun onRemoveAliasConfirmed(aliasName: String, domainName: String?, position: Int)
    abstract fun onRemoveAliasCancel()
    abstract fun onAddAliasButtonPressed()
    abstract fun onAddAliasOkPressed(newAlias: String, domain: String)
    abstract fun onAddAliasSpinnerChangeSelection(domain: String)
    abstract fun onAddAliasTextChanged(newAlias: String)
}