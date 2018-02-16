package com.email.utils

/**
 * Created by gabriel on 2/9/18.
 */

interface VirtualList<out U> {
    operator fun get(i: Int): U

    val size: Int

    class Map<T, out U> (private val originalItems: List<T>,
                         private val mapFn: (T) -> U): VirtualList<U> {
        override operator fun get(i: Int): U = mapFn(originalItems[i])

        override val size: Int
            get () = originalItems.size
}
}