package com.qrshare.sharing

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.qrshare.network.SessionInfo

/**
 * Utility object for generating and decoding QR codes that contain SessionInfo.
 * Uses ZXing under the hood.
 */
object QRCodeHelper {

    private const val QR_SIZE = 512
    private const val QUIET_ZONE = 2

    /**
     * Encode a SessionInfo into a QR code Bitmap.
     * The QR content is the JSON representation of SessionInfo.
     */
    fun generateQRBitmap(sessionInfo: SessionInfo, size: Int = QR_SIZE): Bitmap? {
        return try {
            val content = sessionInfo.toJson()
            val hints = mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.MARGIN to QUIET_ZONE,
                EncodeHintType.ERROR_CORRECTION to com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M
            )
            val writer = MultiFormatWriter()
            val bitMatrix: BitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)
            bitMatrixToBitmap(bitMatrix)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Decode raw QR text back into a SessionInfo object.
     * Returns null if the text is not valid session JSON.
     */
    fun decodeSessionInfo(qrText: String): SessionInfo? {
        return SessionInfo.fromJson(qrText)
    }

    private fun bitMatrixToBitmap(matrix: BitMatrix): Bitmap {
        val width = matrix.width
        val height = matrix.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (matrix[x, y]) Color.BLACK else Color.WHITE
            }
        }
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also {
            it.setPixels(pixels, 0, width, 0, 0, width, height)
        }
    }
}
