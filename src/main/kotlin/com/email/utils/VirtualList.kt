package com.email.utils

/**
 * Created by gabriel on 2/9/18.
 */

interface VirtualList<out T> {
    operator fun get(i: Int): T

    val size: Int
}