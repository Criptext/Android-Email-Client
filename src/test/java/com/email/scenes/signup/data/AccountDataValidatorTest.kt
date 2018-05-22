package com.email.scenes.signup.data

import com.email.validation.FormData
import com.email.validation.AccountDataValidator
import org.amshove.kluent.`should be instance of`
import org.amshove.kluent.`should equal`
import org.junit.Test

/**
 * Created by gabriel on 5/15/18.
 */
class AccountDataValidatorTest {

    @Test
    fun `validateUsername should return valid if receives a valid username`() {
        val myUsername = "tester"
        AccountDataValidator.validateUsername(myUsername) `should equal` FormData.Valid("tester")
    }

    @Test
    fun `validateUsername should return valid if receives less than 3 characters`() {
        val myUsername = "t"
        AccountDataValidator.validateUsername(myUsername) `should be instance of` FormData.Error::class.java
    }

    @Test
    fun `validateUsername should return error if receives spaces in the middle of the username`() {
        val myUsername = "anew tester"
        AccountDataValidator.validateUsername(myUsername) `should be instance of` FormData.Error::class.java
    }

    @Test
    fun `validateUsername should return error if receives invalid characters in username`() {
        val myUsername = "test#"
        AccountDataValidator.validateUsername(myUsername) `should be instance of` FormData.Error::class.java
    }

    @Test
    fun `validateUsername should return error if username starts with dot`() {
        val myUsername = ".tester"
        AccountDataValidator.validateUsername(myUsername) `should be instance of` FormData.Error::class.java
    }

    @Test
    fun `validateUsername should return valid if receives dots, dashes or lowdashes in username`() {
        val myUsername = "test.t_est-er05"
        AccountDataValidator.validateUsername(myUsername) `should be instance of` FormData.Valid::class.java
    }

    @Test
    fun `validateEmailAddress should return valid if receives a valid address`() {
        val address = "somebody@gmail.com"
        AccountDataValidator.validateRecoveryEmailAddress(address) `should equal` FormData.Valid(address)
    }

    @Test
    fun `validateEmailAddress should return error if receives angular brackets and spaces`() {
        val address = "Some User <somebody@gmail.com>"
        AccountDataValidator.validateRecoveryEmailAddress(address) `should be instance of` FormData.Error::class.java
    }
}