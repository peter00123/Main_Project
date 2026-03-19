package com.qrshare.network

import android.content.Context
import android.net.wifi.WifiManager
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.UUID

object NetworkUtils {

    fun getDeviceIpAddress(context: Context): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if (networkInterface.isLoopback || !networkInterface.isUp) continue
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (address is Inet4Address && !address.isLoopbackAddress) {
                        return address.hostAddress ?: "0.0.0.0"
                    }
                }
            }
        } catch (_: Exception) {}
        return "0.0.0.0"
    }

    fun getDeviceName(): String {
        val manufacturer = android.os.Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val model = android.os.Build.MODEL
        return if (model.startsWith(manufacturer, ignoreCase = true)) model
        else "$manufacturer $model"
    }

    fun generateSessionId(): String = UUID.randomUUID().toString().take(8)

    fun findAvailablePort(): Int {
        val socket = java.net.ServerSocket(0)
        val port = socket.localPort
        socket.close()
        return port
    }
}
