package com.email.utils

import android.content.Context

/**
 * Created by gabriel on 2/27/18.
 */

class UIMessage(val resId: Int, val args: Array<Any>) {
    constructor(resId: Int): this(resId, emptyArray())

    override fun toString(): String {
        val sb = StringBuilder()
        args.forEach { sb.append("$it, ") }
        sb.append("]")
        return sb.toString()
    }
}

fun Context.getLocalizedUIMessage(message: UIMessage): String {
    return this.getString(message.resId, *message.args)
}