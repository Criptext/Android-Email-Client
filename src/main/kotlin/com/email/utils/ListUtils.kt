package com.email.utils

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

fun <T> List<T>.findFromPosition(lastKnownPosition: Int, discriminantFunc: (T) -> Boolean): Int {
    val startPosition = Math.min(lastKnownPosition, this.size - 1)
    if (discriminantFunc(this[startPosition]))
        return startPosition

    val size = this.size
    var reachedStart = false
    var reachedEnd = false
    var steps = 1

    while (!reachedEnd && !reachedStart) {
        val lowerPosition = startPosition - steps
        val higherPosition = startPosition + steps

        if (lowerPosition < 0)
            reachedStart = true
        else if (discriminantFunc(this[lowerPosition]))
            return lowerPosition

        if (higherPosition >= size)
            reachedEnd = true
        else if (discriminantFunc(this[higherPosition]))
            return higherPosition

        steps += 1
    }

    return -1
}

fun <T> MutableList<T>.addWhere(item: T, conditionFn: (T) -> Boolean): Int {
    val index = this.indexOfFirst(conditionFn)
    return if (index > 0) {
        this.add(index, item)
        index
    } else {
        val result = this.size
        this.add(item)
        result
    }

}
