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

    override fun equals(other: Any?): Boolean {
        // I only need to check equality in testing
        return other is UIMessage && other.resId == this.resId
    }

    override fun hashCode(): Int = resId
}

fun Context.getLocalizedUIMessage(message: UIMessage): String {
    val expandedArgs = message.args.map {
        if ( it is UIMessage) this.getLocalizedUIMessage(it)
        else it
    }.toTypedArray()

    return this.getString(message.resId, *expandedArgs)
}