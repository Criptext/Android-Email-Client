package com.criptext.mail.validation

import com.criptext.mail.R
import com.criptext.mail.utils.UIMessage
import java.util.regex.Pattern

/**
 * Created by gabriel on 5/15/18.
 */

object AccountDataValidator {

    // matches "99.99% of all email addresses in actual use today"
    // https://stackoverflow.com/a/1373724/5207721
    private val validEmailAddressPattern = Pattern.compile("[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")
    // for the criptext server the only allowed non-alphanumeric characters are: ._-
    // dots cant be at the beginning.
    private val validCriptextUserPattern = Pattern.compile("(?=^([a-z0-9]([._-]{0,2}[a-z0-9])+)\$)(?:^.{3,64}\$)\$")

    fun validateUsername(username: String): FormData<String> {
        val sanitizedValue = username.trim().toLowerCase()

        return if (sanitizedValue.length < 3)
            FormData.Error(UIMessage(R.string.username_length_error))
        else if (! (validCriptextUserPattern.matcher(sanitizedValue).matches() ||
                        validEmailAddressPattern.matcher(sanitizedValue).matches()))
            FormData.Error(UIMessage(R.string.username_invalid_error))
        else
            FormData.Valid(sanitizedValue)
    }

    fun validateEmailAddress(emailAddress: String): FormData<String> {
        val sanitizedValue = emailAddress.trim()
        return if (sanitizedValue.isNotEmpty()
                && ! validEmailAddressPattern.matcher(emailAddress).matches())
            FormData.Error(UIMessage(R.string.email_invalid_error))
        else
            FormData.Valid(sanitizedValue)
    }

    fun validateFullName(fullName: String): FormData<String> {
        val sanitizedValue = fullName.trim()

        return if (sanitizedValue.length >= 255)
            FormData.Error(UIMessage(R.string.fullname_length_error))
        else if (sanitizedValue.isEmpty())
            FormData.Error(UIMessage(R.string.fullname_empty_error))
        else
            FormData.Valid(sanitizedValue)
    }
}