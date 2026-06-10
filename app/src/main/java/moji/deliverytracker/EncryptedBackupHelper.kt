package moji.deliverytracker

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStreamWriter
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec

/**
 * Production-grade encrypted backup helper using AES-256-GCM.
 * Ensures data confidentiality and integrity during export.
 */
object EncryptedBackupHelper {

    private const val ALGORITHM = "AES"
    private const val CIPHER_ALGORITHM = "AES/GCM/NoPadding"
    private const val KEY_SIZE = 256
    private const val GCM_TAG_LENGTH = 128
    private const val GCM_IV_LENGTH = 12

    fun hasStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true // Scoped storage handles Downloads directory
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    suspend fun exportSecureBackup(context: Context, db: AppDatabase): Pair<Boolean, String> {
        if (!hasStoragePermission(context)) {
            return Pair(false, context.getString(R.string.backup_permission_error))
        }

        return withContext(Dispatchers.IO) {
            try {
                // Generate encryption key
                val keyGenerator = KeyGenerator.getInstance(ALGORITHM)
                keyGenerator.init(KEY_SIZE)
                val secretKey = keyGenerator.generateKey()

                // Generate random IV for GCM
                val iv = ByteArray(GCM_IV_LENGTH)
                SecureRandom().nextBytes(iv)

                // Initialize cipher in ENCRYPT mode
                val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
                val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

                // Fetch all orders with names
                val orders = db.orderDao().getAllWithNamesOnce()

                // Create backup file
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val fileName = "mahan_backup_${timestamp}.enc"

                val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    exportViaMediaStore(context, orders, fileName, cipher, iv, secretKey)
                } else {
                    exportViaLegacy(context, orders, fileName, cipher, iv, secretKey)
                }

                result
            } catch (e: Exception) {
                Pair(false, "Backup error: ${e.message}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun exportViaMediaStore(
        context: Context,
        orders: List<OrderWithNames>,
        fileName: String,
        cipher: Cipher,
        iv: ByteArray,
        secretKey: javax.crypto.SecretKey
    ): Pair<Boolean, String> {
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
            put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/MahanBackup")
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: return Pair(false, "Failed to create backup file")

        return try {
            resolver.openOutputStream(uri)?.use { outputStream ->
                // Write header: IV + encrypted CSV
                outputStream.write(iv)
                
                // Create CSV content and encrypt it
                val csvContent = buildCsvContent(orders)
                val encryptedData = cipher.doFinal(csvContent.toByteArray(Charsets.UTF_8))
                outputStream.write(encryptedData)
            }
            Pair(true, "Backup saved to Downloads/MahanBackup/$fileName")
        } catch (e: Exception) {
            Pair(false, "Failed to write backup: ${e.message}")
        }
    }

    @Suppress("DEPRECATION")
    private fun exportViaLegacy(
        context: Context,
        orders: List<OrderWithNames>,
        fileName: String,
        cipher: Cipher,
        iv: ByteArray,
        secretKey: javax.crypto.SecretKey
    ): Pair<Boolean, String> {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "MahanBackup"
        )
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, fileName)
        return try {
            file.outputStream().use { stream ->
                // Write IV
                stream.write(iv)
                
                // Create and encrypt CSV
                val csvContent = buildCsvContent(orders)
                val encryptedData = cipher.doFinal(csvContent.toByteArray(Charsets.UTF_8))
                stream.write(encryptedData)
            }
            Pair(true, "Backup saved to ${file.absolutePath}")
        } catch (e: Exception) {
            Pair(false, "Failed to write backup: ${e.message}")
        }
    }

    private fun buildCsvContent(orders: List<OrderWithNames>): String {
        val sb = StringBuilder()
        sb.append("ID,Customer,Driver,Neighborhood,Amount,Description,DateTime,Settled\n")
        orders.forEach { order ->
            sb.append(
                listOf(
                    order.id.toString(),
                    csvSafe(order.customerName),
                    csvSafe(order.driverName),
                    csvSafe(order.neighborhoodName),
                    order.amount.toString(),
                    csvSafe(order.description),
                    csvSafe(order.dateTime),
                    order.settled.toString()
                ).joinToString(",")
            )
            sb.append("\n")
        }
        return sb.toString()
    }

    private fun csvSafe(value: String): String {
        var safe = value
        // Prevent CSV injection
        if (safe.isNotEmpty() && safe[0] in charArrayOf('=', '+', '-', '@', '|', '%')) {
            safe = "'$safe"
        }
        // Standard CSV escaping
        val escaped = safe.replace("\"", "\"\"")
        return "\"$escaped\""
    }
}
