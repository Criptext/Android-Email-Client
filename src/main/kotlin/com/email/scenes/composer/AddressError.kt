package com.email.scenes.composer

import com.email.R
import com.email.utils.UIMessage

/**
 * Created by gabriel on 8/17/17.
 */

class AddressError(val type: Types, val token: String) {
    enum class Types {
        to, cc, bcc;
    }

    fun toUIMessage(): UIMessage = UIMessage(R.string.invalid_address_error, arrayOf(token, type))

    override fun toString(): String =
            "\"$token\" is not a valid address. Please check input field \"$type\""
}