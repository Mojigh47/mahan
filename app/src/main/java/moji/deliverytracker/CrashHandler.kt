package moji.deliverytracker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AlertDialog
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Global crash handler to prevent black screens and ensure graceful error handling.
 * Catches uncaught exceptions and provides user-friendly error dialogs.
 */
class CrashHandler(private val context: Context) : Thread.UncaughtExceptionHandler {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            // Log the exception
            Log.e("CrashHandler", "Uncaught exception in thread ${thread.name}", throwable)

            // Save crash info for debugging
            saveCrashInfo(throwable)

            // Show user-friendly error dialog
            if (context is Activity && !context.isDestroyed) {
                showErrorDialog(context, throwable)
            }

            // Give the user time to see the dialog before crashing
            Thread.sleep(1000)
        } catch (e: Exception) {
            Log.e("CrashHandler", "Error in crash handler", e)
        }

        // Call the default handler to ensure proper crash reporting
        defaultHandler?.uncaughtException(thread, throwable)
    }

    private fun showErrorDialog(activity: Activity, throwable: Throwable) {
        try {
            AlertDialog.Builder(activity)
                .setTitle("خطا در اپلیکیشن")
                .setMessage("متأسفانه اپلیکیشن با مشکلی روبرو شد. لطفاً دوباره تلاش کنید.")
                .setPositiveButton("بازگشت به صفحه اصلی") { _, _ ->
                    // Restart the app
                    val intent = Intent(activity, LauncherActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    activity.startActivity(intent)
                    activity.finish()
                }
                .setNegativeButton("خروج") { _, _ ->
                    activity.finish()
                }
                .setCancelable(false)
                .show()
        } catch (e: Exception) {
            Log.e("CrashHandler", "Could not show error dialog", e)
        }
    }

    private fun saveCrashInfo(throwable: Throwable) {
        try {
            val stackTrace = StringWriter()
            throwable.printStackTrace(PrintWriter(stackTrace))

            val crashInfo = """
                Device: ${Build.DEVICE}
                Model: ${Build.MODEL}
                Android Version: ${Build.VERSION.RELEASE}
                App Version: ${BuildConfig.VERSION_NAME}
                
                Exception:
                ${throwable.javaClass.simpleName}: ${throwable.message}
                
                Stack Trace:
                $stackTrace
            """.trimIndent()

            // Save to internal storage for later debugging
            val file = context.getFileStreamPath("crash_log.txt")
            file.writeText(crashInfo)
            Log.d("CrashHandler", "Crash info saved to ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("CrashHandler", "Could not save crash info", e)
        }
    }

    companion object {
        fun install(context: Context) {
            Thread.setDefaultUncaughtExceptionHandler(CrashHandler(context))
        }
    }
}
