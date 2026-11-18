package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.InputValidators
import com.example.core.PasswordCrypto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `app name matches Desi Paisa and firestore database id exists`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        val dbId = context.getString(R.string.firestore_database_id)
        assertEquals("Desi Paisa", appName)
        assertTrue(dbId.isNotBlank())
    }

    @Test
    fun `password hashing uses unique random salt and verifies securely`() {
        val first = PasswordCrypto.hashPassword("DesiSecret#2026")
        val second = PasswordCrypto.hashPassword("DesiSecret#2026")

        assertNotEquals(first.saltBase64, second.saltBase64)
        assertNotEquals(first.hashBase64, second.hashBase64)
        assertTrue(PasswordCrypto.verifyPassword("DesiSecret#2026", first.hashBase64, first.saltBase64))
        assertFalse(PasswordCrypto.verifyPassword("WrongPassword", first.hashBase64, first.saltBase64))
    }

    @Test
    fun `input validators enforce mobile, amount, upi and bank formats`() {
        assertTrue(InputValidators.isValidMobile("9876543210"))
        assertFalse(InputValidators.isValidMobile("98765"))
        assertNotNull(InputValidators.isValidAmount("500"))
        assertNull(InputValidators.isValidAmount("5"))
        assertTrue(InputValidators.isValidUpiId("aarav.pay@okaxis"))
        assertFalse(InputValidators.isValidUpiId("invalid-upi"))
        assertTrue(InputValidators.isValidBankDetails("123456789012", "SBIN0001234"))
        assertFalse(InputValidators.isValidBankDetails("123", "INVALID"))
    }
}
