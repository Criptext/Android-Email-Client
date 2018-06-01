package com.email.utils

import com.email.utils.addWhere
import org.amshove.kluent.shouldEqual
import org.junit.Test

/**
 * Created by gabriel on 2/21/18.
 */

class ListUtils_AddWhere {

    @Test
    fun `should add at a position in which the list remains sorted`() {
        val list = mutableListOf(32, 31, 25, 22, 19, 17, 12, 11, 9, 7, 3, 2, 1)

        val newItem = 15
        val insertPosition = list.addWhere(newItem, { item ->  newItem > item})

        insertPosition `shouldEqual` 6
        list[insertPosition] `shouldEqual` newItem
    }
}