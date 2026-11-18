package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.core.AppLang
import com.example.core.DesiStrings
import com.example.core.InputValidators
import com.example.ui.components.DemoModeNoticeBanner
import com.example.ui.components.DesiLogoMark
import com.example.ui.components.LanguageTogglePill

@Composable
fun ProfileSetupScreen(
    lang: AppLang,
    initialName: String,
    email: String,
    isBusy: Boolean,
    onToggleLang: () -> Unit,
    onSubmitProfile: (fullName: String, mobile: String, referredBy: String, password: String) -> Unit,
    onSignOut: () -> Unit
) {
    var fullName by remember { mutableStateOf(initialName) }
    var mobileNumber by remember { mutableStateOf("") }
    var referredByCode by remember { mutableStateOf("") }
    var securityPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.widthIn(max = 520.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DesiLogoMark()
                    Text(
                        text = DesiStrings.tr(lang, "Complete Profile", "प्रोफ़ाइल पूरी करें"),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                LanguageTogglePill(lang = lang, onToggle = onToggleLang)
            }

            DemoModeNoticeBanner(lang = lang)

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = DesiStrings.tr(
                            lang,
                            "Register your Desi Paisa demo wallet profile & encrypted security password.",
                            "अपनी देसी पैसा डेमो वॉलेट प्रोफ़ाइल और एन्क्रिप्टेड सुरक्षा पासवर्ड सेट करें।"
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (email.isNotBlank()) {
                        Text(
                            text = "${DesiStrings.tr(lang, "Google Account", "Google खाता")}: $email",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it; validationError = null },
                        label = { Text(DesiStrings.tr(lang, "Full Name", "पूरा नाम")) },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setup_name_input")
                    )

                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = { input ->
                            mobileNumber = input.filter { it.isDigit() }.take(10)
                            validationError = null
                        },
                        label = { Text(DesiStrings.tr(lang, "10-Digit Mobile Number", "10-अंकीय मोबाइल नंबर")) },
                        leadingIcon = { Icon(Icons.Filled.PhoneAndroid, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setup_mobile_input")
                    )

                    OutlinedTextField(
                        value = referredByCode,
                        onValueChange = { referredByCode = it.uppercase().take(16); validationError = null },
                        label = { Text(DesiStrings.tr(lang, "Referral Code (Optional)", "रेफरल कोड (वैकल्पिक)")) },
                        leadingIcon = { Icon(Icons.Filled.Redeem, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setup_referral_input")
                    )

                    OutlinedTextField(
                        value = securityPassword,
                        onValueChange = { securityPassword = it; validationError = null },
                        label = { Text(DesiStrings.tr(lang, "Security Password (min 6 chars)", "सुरक्षा पासवर्ड (कम से कम 6 अक्षर)")) },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        singleLine = true,
                        visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPass = !showPass }) {
                                Icon(
                                    imageVector = if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = "Toggle password"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setup_password_input")
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; validationError = null },
                        label = { Text(DesiStrings.tr(lang, "Confirm Security Password", "पासवर्ड की पुष्टि करें")) },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        singleLine = true,
                        visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setup_confirm_password_input")
                    )

                    if (validationError != null) {
                        Text(
                            text = validationError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Button(
                        onClick = {
                            validationError = when {
                                !InputValidators.isValidFullName(fullName) ->
                                    DesiStrings.tr(lang, "Enter a valid name (2–60 chars).", "कृपया मान्य नाम (2–60 अक्षर) दर्ज करें।")
                                !InputValidators.isValidMobile(mobileNumber) ->
                                    DesiStrings.tr(lang, "Enter a valid 10-digit mobile number.", "कृपया 10 अंकों का मान्य मोबाइल नंबर दर्ज करें।")
                                !InputValidators.isValidReferralCodeOptional(referredByCode) ->
                                    DesiStrings.tr(lang, "Referral code must be alphanumeric (4–16 chars).", "रेफरल कोड 4–16 अक्षरों का होना चाहिए।")
                                !InputValidators.isValidPassword(securityPassword) ->
                                    DesiStrings.tr(lang, "Security password must be at least 6 characters.", "सुरक्षा पासवर्ड कम से कम 6 अक्षरों का होना चाहिए।")
                                securityPassword != confirmPassword ->
                                    DesiStrings.tr(lang, "Passwords do not match.", "दोनों पासवर्ड मेल नहीं खाते।")
                                else -> null
                            }
                            if (validationError == null) {
                                onSubmitProfile(fullName, mobileNumber, referredByCode, securityPassword)
                            }
                        },
                        enabled = !isBusy,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("setup_submit_button")
                    ) {
                        if (isBusy) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text(DesiStrings.tr(lang, "Create Secure Demo Account", "सुरक्षित डेमो खाता बनाएं"))
                        }
                    }

                    OutlinedButton(
                        onClick = onSignOut,
                        enabled = !isBusy,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("setup_sign_out_button")
                    ) {
                        Text(DesiStrings.tr(lang, "Use Different Google Account", "दूसरा Google खाता चुनें"))
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
