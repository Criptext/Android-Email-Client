package com.criptext.mail.utils

import org.amshove.kluent.`should equal`
import org.junit.Test
import java.util.*

class DateUtilsTest {
    @Test
    fun `printDateWithServerFormat should print the correct format`() {
        val date = GregorianCalendar(2018, Calendar.MAY, 30).time

        val result = DateUtils.printDateWithServerFormat(date)

        result `should equal` "2018-05-30 00:00:00"
    }

    @Test
    fun `parseDateWithServerFormat should reverse the result of printDateWithServerFormat`() {

        val input = "2018-05-30 16:14:48"
        val output = DateUtils.printDateWithServerFormat(
                DateUtils.parseDateWithServerFormat(input, isUTC = false))

        output `should equal` input

    }
}