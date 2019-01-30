package com.criptext.mail.scenes.settings.profile.data

import com.criptext.mail.api.CriptextAPIClient
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpResponseData
import org.json.JSONObject
import java.io.File


class ProfileAPIClient(private val httpClient: HttpClient, var token: String): CriptextAPIClient(httpClient) {

    fun postProfilePicture(image: File): HttpResponseData {
        return httpClient.putFileStream(path = "/user/avatar", authToken = token, filePath = image.path)
    }

    fun deleteProfilePicture(): HttpResponseData{
        return httpClient.delete(path = "/user/avatar", authToken = token, body = JSONObject())
    }
}
