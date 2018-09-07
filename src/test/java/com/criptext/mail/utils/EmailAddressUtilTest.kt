package com.criptext.mail.utils

import org.amshove.kluent.`should equal`
import org.junit.Before
import org.junit.Test
import java.util.*

class EmailAddressUtilTest {
    lateinit var contactAddressList: List<String>

    @Before
    fun setUp() {
        contactAddressList = listOf("Jorge Blacio <jorge@criptext.com>",
                "<someone@criptext.com>", "JB <jb@criptext.com>")
    }

    @Test
    fun `extract valid email from contact address`() {
        val extractedEmailList = contactAddressList.map {
            EmailAddressUtils.extractEmailAddress(it) }


        extractedEmailList[0] `should equal` "jorge@criptext.com"
        extractedEmailList[1] `should equal` "someone@criptext.com"
        extractedEmailList[2] `should equal` "jb@criptext.com"
    }

    @Test
    fun `extract name from contact address`() {

        val extractedNameList = contactAddressList.map {
            EmailAddressUtils.extractName(it) }


        extractedNameList[0] `should equal` "Jorge Blacio"
        extractedNameList[1] `should equal` "someone"
        extractedNameList[2] `should equal` "JB"

    }
}