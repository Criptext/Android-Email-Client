package com.criptext.mail.scenes.settings.aliases

import com.criptext.mail.utils.uiobserver.UIObserver

interface AliasesUIObserver: UIObserver {
    fun onBackButtonPressed()
    fun onRemoveAlias(aliasName: String, domainName: String?, position: Int)
    fun onRemoveAliasConfirmed(aliasName: String, domainName: String?, position: Int)
    fun onRemoveAliasCancel()
    fun onAddAliasButtonPressed()
    fun onAddAliasOkPressed(newAlias: String, domain: String)
    fun onAddAliasSpinnerChangeSelection(domain: String)
    fun onAddAliasTextChanged(newAlias: String)
}