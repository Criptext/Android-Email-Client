package com.criptext.mail.utils

import org.amshove.kluent.shouldEqual
import org.junit.Test

class HTMLUtilTest {

    @Test
    fun `Should clear unauthorized tags`() {
        val unsanitizedHtml = "<div><script>alert(\"I am a malicious script\")</script><p>And I'm just a paragraph</p></div>"
        val expectedHtml = "<html>\n" +
                " <head></head>\n" +
                " <body>\n" +
                "  <div> \n" +
                "   <p>And I'm just a paragraph</p> \n" +
                "  </div>\n" +
                " </body>\n" +
                "</html>"

        val cleanedHtml = HTMLUtils.sanitizeHtml(unsanitizedHtml)

        cleanedHtml shouldEqual expectedHtml
    }

    @Test
    fun `Should not clear authorized schemes on attributes`() {
        val unsanitizedHtml = "<img src=\"http://www.domain.com/path/to/image.png\" />"
        val expectedHtml = "<html>\n" +
                " <head></head>\n" +
                " <body>\n" +
                "  <img src=\"http://www.domain.com/path/to/image.png\">\n" +
                " </body>\n" +
                "</html>"

        val cleanedHtml = HTMLUtils.sanitizeHtml(unsanitizedHtml)

        cleanedHtml shouldEqual expectedHtml
    }

    @Test
    fun `Should clear unauthorized schemes on attribute`() {
        val unsanitizedHtml = "<img src=\"unknownScheme:data/format\" alt=\"filename.jpg\" />"
        val expectedHtml = "<html>\n" +
                " <head></head>\n" +
                " <body>\n" +
                "  <img alt=\"filename.jpg\">\n" +
                " </body>\n" +
                "</html>"

        val cleanedHtml = HTMLUtils.sanitizeHtml(unsanitizedHtml)

        cleanedHtml shouldEqual expectedHtml
    }

    @Test
    fun `Should clear unauthorized attributes`() {
        val unsanitizedHtml = "<a href=\"http://www.criptext.com\" onclick=\"execMaliciousScript()\"> Link </a>"
        val expectedHtml = "<html>\n" +
                " <head></head>\n" +
                " <body>\n" +
                "  <a href=\"http://www.criptext.com\"> Link </a>\n" +
                " </body>\n" +
                "</html>"

        val cleanedHtml = HTMLUtils.sanitizeHtml(unsanitizedHtml)

        cleanedHtml shouldEqual expectedHtml
    }
}