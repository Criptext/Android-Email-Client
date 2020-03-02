package com.criptext.mail.scenes.settings.aliases.data

import com.criptext.mail.db.models.Alias
import com.criptext.mail.db.models.CustomDomain
import com.criptext.mail.utils.UIMessage

sealed class AliasesResult{

    sealed class AddAlias: AliasesResult() {
        data class Success(val alias: Alias): AddAlias()
        data class Failure(val message: UIMessage): AddAlias()
    }

    sealed class DeleteAlias: AliasesResult() {
        data class Success(val aliasName: String, val domain: String?, val position: Int): DeleteAlias()
        data class Failure(val message: UIMessage): DeleteAlias()
    }

    sealed class EnableAlias: AliasesResult() {
        data class Success(val aliasName: String, val domain: String?, val position: Int, val enable: Boolean): EnableAlias()
        data class Failure(val aliasName: String, val domain: String?, val position: Int, val enable: Boolean,
                           val message: UIMessage): EnableAlias()
    }

    sealed class LoadAliases: AliasesResult() {
        data class Success(val aliases: List<Alias>, val domains: List<CustomDomain>): LoadAliases()
        data class Failure(val message: UIMessage): LoadAliases()
    }

}