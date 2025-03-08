package com.vpnforms.github

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vpnforms.models.Form
import com.vpnforms.utils.Constants
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

object GitHubService {
    private const val TAG = "GitHubService"

    fun fetchForms(callback: (List<Form>?, Exception?) -> Unit) {
        thread {
            try {
                // Get decrypted URL from Constants
                val url = URL(Constants.GITHUB_RAW_URL)
                
                val connection = url.openConnection() as HttpURLConnection
                connection.apply {
                    requestMethod = "GET"
                    connectTimeout = 15000
                    readTimeout = 15000
                    setRequestProperty("Accept", "application/json")
                }

                when (connection.responseCode) {
                    HttpURLConnection.HTTP_OK -> {
                        BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                            val response = reader.readText()
                            try {
                                val gson = Gson()
                                val type = object : TypeToken<Map<String, List<Form>>>() {}.type
                                val data: Map<String, List<Form>> = gson.fromJson(response, type)
                                callback(data["forms"], null)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing forms JSON", e)
                                callback(null, Exception("Invalid forms data format"))
                            }
                        }
                    }
                    HttpURLConnection.HTTP_NOT_FOUND -> {
                        Log.e(TAG, "Forms configuration not found")
                        callback(null, Exception("Forms configuration not found"))
                    }
                    else -> {
                        Log.e(TAG, "Failed to fetch forms: ${connection.responseCode}")
                        callback(null, Exception("Failed to fetch forms: ${connection.responseCode}"))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching forms", e)
                callback(null, e)
            }
        }
    }

    // Helper method to encrypt a new URL (for development use)
    fun getEncryptedUrl(url: String): String {
        return try {
            com.vpnforms.utils.CryptoUtil.encrypt(url)
        } catch (e: Exception) {
            Log.e(TAG, "Error encrypting URL", e)
            ""
        }
    }
}
