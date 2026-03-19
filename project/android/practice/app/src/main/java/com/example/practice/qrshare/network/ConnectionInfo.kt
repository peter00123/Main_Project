package com.qrshare.network

import com.google.gson.Gson

data class ConnectionInfo(
    val ipAddress: String,
    val port: Int,
    val deviceName: String,
    val sessionId: String
) {
    fun toJson(): String = Gson().toJson(this)

    companion object {
        fun fromJson(json: String): ConnectionInfo = Gson().fromJson(json, ConnectionInfo::class.java)
    }
}
