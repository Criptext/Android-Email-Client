package com.criptext.mail.scenes.settings.aliases.data

sealed class AliasesRequest{
    data class AddAlias(val alias: String, val domain: String?): AliasesRequest()
    data class DeleteAlias(val alias: String, val domain: String?, val position: Int): AliasesRequest()
    data class EnableAlias(val alias: String, val domain: String?, val enable: Boolean, val position: Int): AliasesRequest()
    class LoadAliases: AliasesRequest()
}