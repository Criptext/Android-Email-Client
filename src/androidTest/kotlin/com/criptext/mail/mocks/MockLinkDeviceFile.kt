package com.criptext.mail.mocks

import java.io.File

object MockLinkDeviceFile {

    private val deviceLinkFileExpectedContent = listOf("{\"table\":\"contact\",\"object\":{\"id\":1,\"email\":\"bob@criptext.com\",\"name\":\"Bob\"}}",
            "{\"table\":\"contact\",\"object\":{\"id\":2,\"email\":\"joe@criptext.com\",\"name\":\"Joe\"}}",
            "{\"table\":\"label\",\"object\":{\"id\":1,\"color\":\"red\",\"text\":\"ALL MAIL\",\"type\":\"SYSTEM\",\"visible\":true}}",
            "{\"table\":\"label\",\"object\":{\"id\":2,\"color\":\"blue\",\"text\":\"INBOX\",\"type\":\"SYSTEM\",\"visible\":true}}",
            "{\"table\":\"file\",\"object\":{\"id\":1,\"token\":\"txt\",\"name\":\"this.txt\",\"size\":12,\"status\":0,\"date\":\"Fri Dec 21 00:00:00 GMT-05:00 2012\",\"readOnly\":true,\"emailId\":1}}",
            "{\"table\":\"file\",\"object\":{\"id\":2,\"token\":\"txt\",\"name\":\"that.txt\",\"size\":14,\"status\":0,\"date\":\"Fri Dec 21 00:00:00 GMT-05:00 2012\",\"readOnly\":true,\"emailId\":2}}",
            "{\"table\":\"email\",\"object\":{\"id\":1,\"messageId\":\"id_1\",\"threadId\":\"\",\"unread\":true,\"secure\":true,\"content\":\"contents 1\",\"preview\":\"cont\",\"subject\":\"subject 1\",\"delivered\":\"DELIVERED\",\"date\":\"Fri Dec 21 00:00:00 GMT-05:00 2012\",\"metadataKey\":123,\"isMuted\":false,\"unsentDate\":\"Fri Dec 21 00:00:00 GMT-05:00 2012\",\"trashDate\":\"Fri Dec 21 00:00:00 GMT-05:00 2012\"}}",
            "{\"table\":\"email\",\"object\":{\"id\":2,\"messageId\":\"id_2\",\"threadId\":\"\",\"unread\":true,\"secure\":true,\"content\":\"contents 2\",\"preview\":\"cont\",\"subject\":\"subject 2\",\"delivered\":\"DELIVERED\",\"date\":\"Fri Dec 21 00:00:00 GMT-05:00 2012\",\"metadataKey\":456,\"isMuted\":false,\"unsentDate\":\"Fri Dec 21 00:00:00 GMT-05:00 2012\",\"trashDate\":\"Fri Dec 21 00:00:00 GMT-05:00 2012\"}}",
            "{\"table\":\"email_label\",\"object\":{\"emailId\":1,\"labelId\":1}}",
            "{\"table\":\"email_label\",\"object\":{\"emailId\":2,\"labelId\":2}}",
            "{\"table\":\"email_contact\",\"object\":{\"id\":1,\"emailId\":1,\"contactId\":1,\"type\":\"TO\"}}",
            "{\"table\":\"email_contact\",\"object\":{\"id\":2,\"emailId\":2,\"contactId\":2,\"type\":\"FROM\"}}",
            "{\"table\":\"file_key\",\"object\":{\"id\":1,\"key\":\"test_key_16bytes:test_iv_16_bytes\",\"emailId\":1}}",
            "{\"table\":\"file_key\",\"object\":{\"id\":2,\"key\":\"test_key_16bytes:test_iv_16_bytes\",\"emailId\":2}}")

    fun getMockedFile(): File{
        val file = createTempFile("mocked_link_device_file")
        deviceLinkFileExpectedContent.forEach { file.appendText(it) }
        return file
    }
}