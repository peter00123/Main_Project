// utils/QrUtils.kt
// Utility functions for generating and decoding QR codes using ZXing library.
// Used by: ui/receive/ReceiveFragment (generate QR bitmap for display),
//          ui/send/SendActivity (decode scanned QR from CameraX)

package com.atezhare.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix

object QrUtils {

    /**
     * Generates a QR code Bitmap from the given string content.
     * Used in ReceiveFragment to display the pairing QR code.
     *
     * @param content   The string to encode (usually qrData from ReceiverQrResponse)
     * @param size      Width and height of the output bitmap in pixels
     * @return          Bitmap of the QR code, or null if encoding fails
     */
    fun generateQrBitmap(content: String, size: Int = 600): Bitmap? {
        return try {
            val hints = mapOf(
                EncodeHintType.MARGIN to 2,
                EncodeHintType.CHARACTER_SET to "UTF-8"
            )
            val writer = MultiFormatWriter()
            val bitMatrix: BitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)

            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Validates that a 6-digit code string contains only digits and is exactly 6 chars.
     * Used in ReceiveFragment before submitting code to backend.
     */
    fun isValidCode(code: String): Boolean = code.length == 6 && code.all { it.isDigit() }
}
