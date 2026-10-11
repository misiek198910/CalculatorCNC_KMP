package kalkulator.cnc.data.db

import java.io.File

actual fun saveFileToDisk(bytes: ByteArray, fileName: String) {
    val file = File(fileName)
    file.writeBytes(bytes)
}