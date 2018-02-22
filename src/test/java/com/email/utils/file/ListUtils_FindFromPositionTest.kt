package com.email.utils.file

import org.amshove.kluent.shouldEqual
import org.junit.Test

import org.junit.Assert.*

/**
 * Created by gabriel on 2/21/18.
 */
class ListUtils_FindFromPositionTest {

    @Test
    fun `should immediately return the start position if the list has not changed`() {
        val list = listOf("Guayas", "Santa Elena", "Pichincha", "Esmeraldas", "Los Rios", "Azuay")
        val lastKnownPosition = 2

        val result = list.findFromPosition(lastKnownPosition, { item -> item == "Pichincha"})

        result `shouldEqual` lastKnownPosition
    }

    @Test
    fun `should find the correct item even if elements before it are removed, updating indexes`() {
        val list = arrayListOf("Guayas", "Santa Elena", "Pichincha", "Esmeraldas", "Los Rios", "Azuay")
        val lastKnownPosition = 2

        list.removeAt(0)
        list.removeAt(0)

        val result = list.findFromPosition(lastKnownPosition, { item -> item == "Pichincha"})

        list[result] `shouldEqual` "Pichincha"
        result `shouldEqual` 0
    }

    @Test
    fun `should find the correct item even if the last known index no longer exists`() {
        val list = arrayListOf("Guayas", "Santa Elena", "Pichincha", "Esmeraldas", "Los Rios", "Azuay")
        val lastKnownPosition = 5

        list.removeAt(0)
        list.removeAt(0)

        val result = list.findFromPosition(lastKnownPosition, { item -> item == "Azuay"})

        list[result] `shouldEqual` "Azuay"
        result `shouldEqual` 3
    }

    @Test
    fun `should find the correct item even if new elements are inserted before it, updating indexes`() {
        val list = arrayListOf("Guayas", "Santa Elena", "Pichincha", "Esmeraldas", "Los Rios", "Azuay")
        val lastKnownPosition = 2

        list.add(0, "Napo")
        list.add(0, "El Oro")

        val result = list.findFromPosition(lastKnownPosition, { item -> item == "Pichincha"})

        list[result] `shouldEqual` "Pichincha"
        result `shouldEqual` 4
    }
}