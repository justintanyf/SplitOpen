package com.example.splitwise.data

import android.content.Context
import android.os.Environment
import java.io.File

class DbHelper(private val context: Context) {

    fun importDb(uri: android.net.Uri) {
        val dbFile = context.getDatabasePath("splitwise.db")
        val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        }
        if (fileName?.endsWith(".db") == true) {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                dbFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
    }
}
