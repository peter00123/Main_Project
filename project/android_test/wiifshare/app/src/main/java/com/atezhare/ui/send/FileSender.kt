package com.atezhare.ui.send


import android.content.Context
import android.net.Uri
import java.net.InetSocketAddress
import java.net.Socket

class FileSender {

    fun sendFile(
        host: String,
        fileUri: Uri,
        context: Context,
        fileSize: Long,
        onProgress: (Int) -> Unit
    ) {
        Thread {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(host, 8888), 5000)

                val output = socket.getOutputStream()
                val input = context.contentResolver.openInputStream(fileUri)

                val buffer = ByteArray(8192)
                var bytes: Int
                var total = 0L

                while (input!!.read(buffer).also { bytes = it } != -1) {
                    output.write(buffer, 0, bytes)
                    total += bytes

                    val progress = ((total * 100) / fileSize).toInt()
                    onProgress(progress)
                }

                output.flush()
                output.close()
                input.close()
                socket.close()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}