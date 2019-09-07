package com.wmontgom85.flickrfindr.api

import android.util.Log
import com.wmontgom85.flickrfindr.sealed.Result
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * API connection class used to establish an http connection from a given APIRequest and APITask
 */
class APIConnection(
    private val request : APIRequest,
    private val errorMessage : String? = null
) {
    /**
     * Makes an HTTP request with a provided APIRequest object
     */
    fun <T: Any> makeRequest(): Result<T> {
        var response = ""

        var connection : HttpURLConnection? = null

        try {
            val url = URL(request.restUrl)

            connection = url.openConnection() as HttpURLConnection

            connection.apply {
                connectTimeout = request.timeout
                readTimeout = request.timeout
                requestMethod = request.requestType.value
                doInput = true
                doOutput = request.requestType == RequestType.POST // set connection output boolean based on request type
                setRequestProperty("charset", "utf-8")

                if (request.requestType == RequestType.POST) { // if we're posting data, we need to build the query string
                    // send POST data only if query string was successfully generated
                    request.buildQuery()?.let {
                        val os = outputStream
                        val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8") as Writer)
                        writer.write(it)
                        writer.flush()
                        writer.close()
                        os.close()
                    }
                }
            }

            return when {
                connection.responseCode == HttpsURLConnection.HTTP_OK -> {
                    val br = BufferedReader(InputStreamReader(connection.inputStream))
                    br.readLine().forEach {
                        response += it
                    }
                    Result.Success(response)
                }
                else -> Result.Error(IOException("${errorMessage?:"An error has occurred."} Error Code ACP001"))
            }
        } catch (tx: Throwable) {
            tx.printStackTrace()

            // set the connection to null so no further execution can occur
            return Result.Error(IOException("${errorMessage?:"An error has occurred."} Error Code ACP002"))
        } finally {
            connection?.disconnect()
        }
    }
}