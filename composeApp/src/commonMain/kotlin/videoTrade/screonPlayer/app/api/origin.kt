/*
 * Copyright (c) LLC "Centr Distribyucii"
 * All rights reserved.
 */
package videoTrade.screonPlayer.app.api

import cafe.adriel.voyager.navigator.Navigator
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.InternalAPI
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class origin {
    val client =
        HttpClient()


    suspend inline fun get(url: String): String? {

        try {
            val token = getValueInStorage("accessToken")


            println("url ${PathConfig.SERVER_URL}$url")

            val response: HttpResponse = client.get("${PathConfig.SERVER_URL}$url") {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $token")
            }


            println("bodyAsTextbodyAsText ${response.bodyAsText()} ${response.status}")


            if (
                response.status == HttpStatusCode(401, "Unauthorized")
            ) {
                return null

            }


            if (response.status.isSuccess()) {

                return response.bodyAsText()
            } else {


                println("Failed to retrieve data: ${response.status.description} ${response.request}")
            }
        } catch (e: Exception) {
            println("Error22231:$url $e  ")
        } finally {
            client.close()
        }
        return null
    }


    suspend inline fun post(
        url: String,
        data: String,
        isNotNeedToken: Boolean = false
    ): String? {

        try {
            val token = getValueInStorage("accessToken")
            println("url ${PathConfig.SERVER_URL}$url")

            val response: HttpResponse =
                client.post("${PathConfig.SERVER_URL}$url") {
                    contentType(ContentType.Application.Json)
                    if (!isNotNeedToken) header(HttpHeaders.Authorization, "Bearer $token")
                    setBody(data)
                }

            println("Отправляем данные: $data")
            println("response.bodyAsText() ${response.bodyAsText()}")

            if (response.status.isSuccess()) {


                return response.bodyAsText()
            } else {
                println("Failed to retrieve data: ${response.status.description} ${response.request}")
            }
        } catch (e: Exception) {

            println("Error1111: $e")

            return null

        } finally {
            client.close()
        }

        return null
    }


    suspend inline fun put(
        url: String,
        data: String
    ): HttpResponse? {

        try {
            val token = getValueInStorage("accessToken")

            val response: HttpResponse =
                client.put("${PathConfig.SERVER_URL}$url") {
                    contentType(ContentType.Application.Json)
                    header(HttpHeaders.Authorization, "Bearer $token")
                    setBody(data)
                }


            println("response.bodyAsText() ${response.bodyAsText()}")

            if (response.status.isSuccess()) {

                return response

            } else {
                println("Failed to retrieve data: ${response.status.description} ${response.request}")

                return null

            }
        } catch (e: Exception) {
            println("Error: $e")
            return null

        } finally {
            client.close()
        }
    }


    suspend inline fun reloadTokens(navigator: Navigator): String? {
        val client = HttpClient()

        try {

            val refreshToken = getValueInStorage("refreshToken")

            val jsonContent = Json.encodeToString(
                buildJsonObject {
                    put("refreshToken", refreshToken)
                }
            )

            println("response11 $jsonContent")

            val response: HttpResponse =
                client.post("${PathConfig.SERVER_URL}auth/refresh-token") {
                    contentType(ContentType.Application.Json)
                    setBody(jsonContent)
                }

            println("response11 ${response.bodyAsText()}")


            if (response.status.isSuccess()) {
                return ""

            } else {
                println("Failed to retrieve data: ${response.status.description} ${response.request}")
                return null

            }
        } catch (e: Exception) {
            println("Error2: $e")
        } finally {
            client.close()
        }

        return null
    }
}