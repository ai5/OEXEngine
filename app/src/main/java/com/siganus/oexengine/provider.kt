package com.siganus.oexengine

import android.content.ContentProvider
import android.content.ContentValues
import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

// OEXのプロバイダを将棋用にしてもらったものだけど、実際には未使用。一応残しておく
class ShogiEngineProvider : ContentProvider() {

    companion object {
        private const val MIME_TYPE = "application/x-shogi-engine"
        private const val UNSUPPORTED = "Not supported by this provider"
        private val TAG: String = ShogiEngineProvider::class.java.simpleName
    }

    override fun onCreate(): Boolean = true

    @Throws(FileNotFoundException::class)
    override fun openAssetFile(uri: Uri, mode: String): AssetFileDescriptor? {
        val manager: AssetManager = context?.assets ?: throw FileNotFoundException()
        val fileName = uri.lastPathSegment ?: throw FileNotFoundException()
        return try {
            manager.openFd(fileName)
        } catch (e: IOException) {
            Log.d(
                TAG,
                "Engine file <$fileName> was not found in assets, trying to load from libraries."
            )
            val libFileName = getNativeLibraryDir() + File.separator + fileName
            try {
                AssetFileDescriptor(openLibFile(File(libFileName)), 0, AssetFileDescriptor.UNKNOWN_LENGTH)
            } catch (ex: IOException) {
                val msg = "Error opening file <$libFileName>."
                Log.e(TAG, msg, ex)
                throw FileNotFoundException(msg + "\n" + ex.localizedMessage)
            }
        }
    }

    private fun getNativeLibraryDir(): String = context!!.applicationInfo.nativeLibraryDir

    @Throws(FileNotFoundException::class)
    fun openLibFile(f: File): ParcelFileDescriptor {
        return ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    override fun getType(uri: Uri): String? = MIME_TYPE

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        throw UnsupportedOperationException(UNSUPPORTED)
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        throw UnsupportedOperationException(UNSUPPORTED)
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException(UNSUPPORTED)
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        throw UnsupportedOperationException(UNSUPPORTED)
    }
}

