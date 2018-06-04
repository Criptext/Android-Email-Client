package com.email.api.models

import java.io.File
import java.util.*


sealed class MultipartFormItem {
    data class StringItem(val value: String): MultipartFormItem()
    data class FileItem(val name: String, val value: File): MultipartFormItem()
    data class ByteArrayItem(val name: String, val value: ByteArray): MultipartFormItem() {
        override fun equals(other: Any?): Boolean {
            return if (other is ByteArrayItem) {
                this.name == other.name && Arrays.equals(this.value, other.value)
            } else false
        }

        override fun hashCode(): Int {
            val h1 = name.hashCode()
            val h2 = Arrays.hashCode(value)

            return h1 * 31 + h2
        }
    }
}