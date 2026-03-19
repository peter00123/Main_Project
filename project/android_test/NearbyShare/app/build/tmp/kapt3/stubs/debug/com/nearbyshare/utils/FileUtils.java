package com.nearbyshare.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;
import com.nearbyshare.R;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bJ\u000e\u0010\t\u001a\u00020\b2\u0006\u0010\n\u001a\u00020\u000bJ\u0016\u0010\f\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\r\u001a\u00020\u000eJ\u0016\u0010\u000f\u001a\u00020\u000b2\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\r\u001a\u00020\u000eJ\u000e\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\bJ\u0016\u0010\u0013\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\r\u001a\u00020\u000e\u00a8\u0006\u0014"}, d2 = {"Lcom/nearbyshare/utils/FileUtils;", "", "()V", "createReceiveFile", "Ljava/io/File;", "context", "Landroid/content/Context;", "fileName", "", "formatFileSize", "bytes", "", "getFileName", "uri", "Landroid/net/Uri;", "getFileSize", "getFileTypeIcon", "", "mimeType", "getMimeType", "app_debug"})
public final class FileUtils {
    @org.jetbrains.annotations.NotNull()
    public static final com.nearbyshare.utils.FileUtils INSTANCE = null;
    
    private FileUtils() {
        super();
    }
    
    /**
     * Resolves the MIME type of a content URI.
     * First tries ContentResolver (most accurate for content:// URIs),
     * then falls back to the file extension.
     *
     * @param context  Any context for ContentResolver access.
     * @param uri      The content URI to inspect.
     * @return MIME type string (e.g. "image/jpeg"), or *" as fallback.
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getMimeType(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.net.Uri uri) {
        return null;
    }
    
    /**
     * Retrieves the display name of a file from its content URI.
     * Queries OpenableColumns.DISPLAY_NAME via ContentResolver.
     *
     * @param context  Any context for ContentResolver access.
     * @param uri      The content URI to query.
     * @return The filename string, or a generic fallback.
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getFileName(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.net.Uri uri) {
        return null;
    }
    
    /**
     * Returns the byte size of a file pointed to by a content URI.
     *
     * @param context  Any context for ContentResolver access.
     * @param uri      The content URI to query.
     * @return File size in bytes, or 0 if not determinable.
     */
    public final long getFileSize(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.net.Uri uri) {
        return 0L;
    }
    
    /**
     * Creates a File object in the appropriate Downloads directory for
     * storing a received file. On API 29+ uses the scoped
     * Environment.DIRECTORY_DOWNLOADS; on older devices uses the
     * public Downloads folder.
     *
     * @param context      Any context (for getExternalFilesDir fallback).
     * @param fileName     Desired filename (sanitised for safety).
     * @return A File object pointing to the target path (not yet written).
     */
    @org.jetbrains.annotations.NotNull()
    public final java.io.File createReceiveFile(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.lang.String fileName) {
        return null;
    }
    
    /**
     * Converts a raw byte count into a human-readable file size string.
     *
     * Examples:
     *  512          → "512 B"
     *  1_536        → "1.5 KB"
     *  3_145_728    → "3.0 MB"
     *  1_073_741_824 → "1.00 GB"
     *
     * @param bytes File size in bytes.
     * @return Formatted string with appropriate unit.
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String formatFileSize(long bytes) {
        return null;
    }
    
    /**
     * Maps a MIME type string to an appropriate drawable resource ID.
     * Used by the UI to show a contextual icon next to file names.
     *
     * @param mimeType MIME type string (e.g. "image/jpeg", "application/pdf").
     * @return A drawable resource ID from R.drawable.
     */
    public final int getFileTypeIcon(@org.jetbrains.annotations.NotNull()
    java.lang.String mimeType) {
        return 0;
    }
}