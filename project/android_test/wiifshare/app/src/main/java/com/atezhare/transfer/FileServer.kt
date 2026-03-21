package com.atezhare.transfer


import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.net.ServerSocket

class FileServer(private val context: Context) {

    fun startServer(onProgress: (Int) -> Unit) {
        Thread {
            try {
                val serverSocket = ServerSocket(8888)
                val client = serverSocket.accept()

                val input = client.getInputStream()
                val file = File(context.getExternalFilesDir(null), "received_file")

                val output = FileOutputStream(file)

                val buffer = ByteArray(8192)
                var bytes: Int
                var total = 0

                while (input.read(buffer).also { bytes = it } != -1) {
                    output.write(buffer, 0, bytes)
                    total += bytes

                    onProgress(total / 1024)
                }

                output.close()
                input.close()
                client.close()
                serverSocket.close()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}