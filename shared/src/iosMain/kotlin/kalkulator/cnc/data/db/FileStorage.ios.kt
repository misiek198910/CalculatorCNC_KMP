package kalkulator.cnc.data.db

import platform.Foundation.*
import kotlinx.cinterop.*

@OptIn(ExperimentalForeignApi::class)
actual fun saveFileToDisk(bytes: ByteArray, fileName: String) {
    val fileManager = NSFileManager.defaultManager
    val data = bytes.usePinned { pinned ->
        NSData.dataWithBytes(pinned.addressOf(0), bytes.size.toULong())
    }

    val path = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true).first() as String
    val fullPath = "$path/$fileName"

    fileManager.createFileAtPath(fullPath, data, null)
}