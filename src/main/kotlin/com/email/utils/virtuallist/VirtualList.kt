package com.email.utils.virtuallist

/**
 * Created by gabriel on 2/9/18.
 */

interface VirtualList<out U>: Iterable<U> {
    operator fun get(i: Int): U

    val size: Int

    val hasReachedEnd: Boolean

    override fun iterator(): Iterator<U> {
            return VirtualIterator(this)
    }

    private class VirtualIterator<out U>(private val iteratedList: VirtualList<U>): Iterator<U> {
        private var currentIndex = -1
        override fun hasNext(): Boolean {
            return currentIndex + 1 < iteratedList.size
        }

        override fun next(): U {
            currentIndex += 1
            return iteratedList[currentIndex]
        }

    }

    class Map<T, out U> (private val originalItems: List<T>,
                         private val mapFn: (T) -> U): VirtualList<U> {

        override val hasReachedEnd = true

        override operator fun get(i: Int): U = mapFn(originalItems[i])

        override val size: Int
            get () = originalItems.size
    }
}