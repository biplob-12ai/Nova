package com.example.nova.messages

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Patterns
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class MessageValidationResult(
    val isValid: Boolean,
    val sanitizedPhone: String,
    val sanitizedMessage: String,
    val privacyWarning: String?
)

class MessageManager(private val context: Context) {

    /**
     * Validates contact number and checks message body against sensitive leaks (e.g. credit card or SSN patterns).
     */
    fun validateMessage(rawPhone: String, rawMessage: String): MessageValidationResult {
        // Strip non-digits except leading +
        val digitsOnly = rawPhone.replace(Regex("[^0-9+]"), "")

        if (digitsOnly.length < 7) {
            return MessageValidationResult(
                isValid = false,
                sanitizedPhone = rawPhone,
                sanitizedMessage = rawMessage,
                privacyWarning = "Phone number is too short or invalid."
            )
        }

        // Privacy check: Warn if message appears to contain 16-digit credit card or 9-digit SSN
        var warning: String? = null
        if (rawMessage.contains(Regex("\\b\\d{4}[ -]?\\d{4}[ -]?\\d{4}[ -]?\\d{4}\\b"))) {
            warning = "WARNING: Potential payment card number detected in text."
        } else if (rawMessage.contains(Regex("\\b\\d{3}-\\d{2}-\\d{4}\\b"))) {
            warning = "WARNING: Potential government ID / SSN pattern detected in text."
        }

        return MessageValidationResult(
            isValid = true,
            sanitizedPhone = digitsOnly,
            sanitizedMessage = rawMessage.trim(),
            privacyWarning = warning
        )
    }

    /**
     * Launches WhatsApp bridge with pre-filled message, with graceful fallback to SMS.
     */
    fun launchWhatsAppBridge(phoneNumber: String, messageText: String): Boolean {
        val validation = validateMessage(phoneNumber, messageText)
        if (!validation.isValid) return false

        val encodedMessage = URLEncoder.encode(validation.sanitizedMessage, StandardCharsets.UTF_8.toString())
        val cleanPhone = validation.sanitizedPhone.replace("+", "")

        val whatsappUri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMessage")
        val whatsappIntent = Intent(Intent.ACTION_VIEW, whatsappUri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        return try {
            context.startActivity(whatsappIntent)
            true
        } catch (e: Exception) {
            // Fallback: SMS Intent
            try {
                val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("smsto:${validation.sanitizedPhone}")
                    putExtra("sms_body", validation.sanitizedMessage)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(smsIntent)
                true
            } catch (e2: Exception) {
                false
            }
        }
    }
}
