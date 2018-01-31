package com.email.utils.file

/**
 * Created by gabriel on 8/24/17.
 */

fun <T> MutableList<T>.removeWithDiscrimination(discriminantFunc: (T) -> Boolean) {
    val iterator = this.iterator()
    while(iterator.hasNext()) {
        val item = iterator.next()
        val shouldBeRemoved = discriminantFunc(item)
        if (shouldBeRemoved) iterator.remove()
    }
}
