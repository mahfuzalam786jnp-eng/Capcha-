package com.example.core

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class HashedPassword(
    val hashBase64: String,
    val saltBase64: String
)

object PasswordCrypto {
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 65_536
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_BYTES = 16

    fun hashPassword(plainText: String): HashedPassword {
        require(plainText.length >= 6) { "Password must be at least 6 characters" }
        val salt = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(salt)
        val hash = pbkdf2(plainText.toCharArray(), salt)
        return HashedPassword(
            hashBase64 = Base64.encodeToString(hash, Base64.NO_WRAP),
            saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        )
    }

    fun verifyPassword(plainText: String, storedHashBase64: String, storedSaltBase64: String): Boolean {
        if (plainText.isBlank() || storedHashBase64.isBlank() || storedSaltBase64.isBlank()) return false
        return try {
            val salt = Base64.decode(storedSaltBase64, Base64.NO_WRAP)
            val expectedHash = Base64.decode(storedHashBase64, Base64.NO_WRAP)
            val candidateHash = pbkdf2(plainText.toCharArray(), salt)
            MessageDigest.isEqual(candidateHash, expectedHash)
        } catch (_: Exception) {
            false
        }
    }

    private fun pbkdf2(password: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH_BITS)
        return try {
            SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }
}

object InputValidators {
    private val MOBILE_REGEX = Regex("^[0-9]{10}$")
    private val UPI_REGEX = Regex("^[a-zA-Z0-9._\\-]{2,}@[a-zA-Z0-9]{2,}$")
    private val REFERRAL_REGEX = Regex("^[A-Z0-9]{4,16}$")

    fun isValidFullName(name: String): Boolean = name.trim().length in 2..60

    fun isValidMobile(mobile: String): Boolean = MOBILE_REGEX.matches(mobile.trim())

    fun isValidPassword(password: String): Boolean = password.length in 6..64

    fun isValidAmount(amountText: String, min: Double = 10.0, max: Double = 100_000.0): Double? {
        val parsed = amountText.trim().toDoubleOrNull() ?: return null
        if (parsed.isNaN() || parsed.isInfinite()) return null
        return if (parsed in min..max) parsed else null
    }

    fun isValidUpiId(upi: String): Boolean = UPI_REGEX.matches(upi.trim())

    fun isValidBankDetails(accountNumber: String, ifsc: String): Boolean {
        val cleanAcc = accountNumber.trim()
        val cleanIfsc = ifsc.trim().uppercase()
        return cleanAcc.length in 9..18 &&
            cleanAcc.all { it.isDigit() } &&
            Regex("^[A-Z]{4}0[A-Z0-9]{6}$").matches(cleanIfsc)
    }

    fun isValidReferralCodeOptional(code: String): Boolean {
        val trimmed = code.trim().uppercase()
        return trimmed.isEmpty() || REFERRAL_REGEX.matches(trimmed)
    }

    fun maskPayoutDestination(method: String, raw: String): String {
        val trimmed = raw.trim()
        return if (method == "UPI") {
            val parts = trimmed.split("@")
            if (parts.size == 2 && parts[0].length > 3) {
                "${parts[0].take(2)}***@${parts[1]}"
            } else {
                trimmed
            }
        } else {
            if (trimmed.length > 6) "****${trimmed.takeLast(4)}" else trimmed
        }
    }
}
