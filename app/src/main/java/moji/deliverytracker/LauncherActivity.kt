package moji.deliverytracker

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

/**
 * Mandatory global authentication gate for the entire application.
 * This Activity is the entry point and enforces authentication before any other Activity can be accessed.
 * No user can bypass this authentication to access any feature of the app.
 */
class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize security system
        SecurityHelper.migrateIfNeeded(this)

        // Check if user is already authenticated in this session
        if (SecurityHelper.isAuthenticated(this)) {
            if (SecurityHelper.needsPasswordChange(this)) {
                showChangePasswordDialog()
            } else {
                navigateToHome()
            }
        } else {
            // User not authenticated, show auth dialog
            showAuthDialog()
        }
    }

    private fun showAuthDialog() {
        val input = EditText(this)
        input.hint = getString(R.string.auth_hint)
        input.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.auth_title))
            .setMessage(getString(R.string.auth_message))
            .setView(input)
            .setCancelable(false)
            .setPositiveButton(getString(R.string.action_yes)) { _, _ ->
                val password = input.text.toString()
                if (SecurityHelper.verifyPassword(this, password)) {
                    SecurityHelper.clearFailedAttempts(this)
                    SecurityHelper.setAuthenticated(this, true)
                    if (SecurityHelper.needsPasswordChange(this)) {
                        showChangePasswordDialog()
                    } else {
                        navigateToHome()
                    }
                } else {
                    SecurityHelper.registerFailedAttempt(this)
                    if (SecurityHelper.isLockedOut(this)) {
                        Toast.makeText(this, getString(R.string.auth_locked), Toast.LENGTH_LONG).show()
                        finish()
                        return@setPositiveButton
                    }
                    Toast.makeText(this, getString(R.string.auth_wrong), Toast.LENGTH_SHORT).show()
                    showAuthDialog()
                }
            }
            .setNegativeButton(getString(R.string.action_cancel)) { _, _ ->
                finish()
            }
            .show()
    }

    private fun showChangePasswordDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val etNew = view.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirm = view.findViewById<TextInputEditText>(R.id.etConfirmPassword)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.auth_change_title))
            .setMessage(getString(R.string.auth_change_message))
            .setView(view)
            .setCancelable(false)
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
                navigateToHome()
            }
            .show()
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
