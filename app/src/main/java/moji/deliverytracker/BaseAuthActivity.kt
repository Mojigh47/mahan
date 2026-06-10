package moji.deliverytracker

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

/**
 * Base activity that enforces authentication before any sensitive operation.
 * All activities that handle financial data should extend this class.
 */
abstract class BaseAuthActivity : AppCompatActivity() {

    protected lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize database early
        db = AppDatabase.getInstance(this)
        
        // Run security checks before loading content
        performSecurityChecks()
    }

    private fun performSecurityChecks() {
        SecurityHelper.migrateIfNeeded(this)

        if (SecurityHelper.isLockedOut(this)) {
            Toast.makeText(this, getString(R.string.auth_locked), Toast.LENGTH_LONG).show()
            finish()
            return
        }

        if (!SecurityHelper.isAuthenticated(this)) {
            showAuthDialog()
            return
        }

        if (SecurityHelper.needsPasswordChange(this)) {
            showChangePasswordDialog()
        } else {
            onAuthenticationSuccess()
        }
    }

    /**
     * Override this method to initialize UI after authentication succeeds.
     */
    protected open fun onAuthenticationSuccess() {
        // Subclasses should override this
    }

    private fun showAuthDialog() {
        val input = EditText(this)
        input.hint = getString(R.string.auth_hint)
        input.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.auth_title))
            .setMessage(getString(R.string.auth_message))
            .setView(input)
            .setPositiveButton(getString(R.string.action_yes)) { _, _ ->
                val password = input.text.toString()
                if (SecurityHelper.verifyPassword(this, password)) {
                    SecurityHelper.clearFailedAttempts(this)
                    SecurityHelper.setAuthenticated(this, true)
                    if (SecurityHelper.needsPasswordChange(this)) {
                        showChangePasswordDialog()
                    } else {
                        onAuthenticationSuccess()
                    }
                } else {
                    SecurityHelper.registerFailedAttempt(this)
                    if (SecurityHelper.isLockedOut(this)) {
                        Toast.makeText(this, getString(R.string.auth_locked), Toast.LENGTH_LONG).show()
                        finish()
                        return@setPositiveButton
                    }
                    Toast.makeText(this, getString(R.string.auth_wrong), Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .setNegativeButton(getString(R.string.action_cancel)) { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun showChangePasswordDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val etNew = view.findViewById<android.widget.TextInputEditText>(R.id.etNewPassword)
        val etConfirm = view.findViewById<android.widget.TextInputEditText>(R.id.etConfirmPassword)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.auth_change_title))
            .setMessage(getString(R.string.auth_change_message))
            .setView(view)
            .setPositiveButton(getString(R.string.action_save)) { _, _ ->
                val newPass = etNew.text?.toString()?.trim().orEmpty()
                val confirm = etConfirm.text?.toString()?.trim().orEmpty()
                if (newPass.length < 4) {
                    Toast.makeText(this, getString(R.string.auth_change_short), Toast.LENGTH_SHORT).show()
                    showChangePasswordDialog()
                    return@setPositiveButton
                }
                if (newPass != confirm) {
                    Toast.makeText(this, getString(R.string.auth_change_mismatch), Toast.LENGTH_SHORT).show()
                    showChangePasswordDialog()
                    return@setPositiveButton
                }
                SecurityHelper.setPassword(this, newPass)
                SecurityHelper.markPasswordChanged(this)
                onAuthenticationSuccess()
            }
            .setCancelable(false)
            .show()
    }
}
