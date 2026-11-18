package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Outbox
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.core.AppLang
import com.example.core.DesiStrings
import com.example.core.InputValidators
import com.example.data.model.UserProfile
import com.example.ui.components.LanguageTogglePill

@Composable
fun ProfileScreen(
    profile: UserProfile,
    lang: AppLang,
    isBusy: Boolean,
    onToggleLang: () -> Unit,
    onUpdateProfile: (fullName: String, mobile: String, onSuccess: () -> Unit) -> Unit,
    onChangePassword: (currentPass: String, newPass: String, onSuccess: () -> Unit) -> Unit,
    onOpenDeposits: () -> Unit,
    onOpenWithdrawals: () -> Unit,
    onOpenTransactions: () -> Unit,
    onOpenSupport: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenTerms: () -> Unit,
    onLogout: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = DesiStrings.tr(lang, "Profile", "प्रोफ़ाइल"),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    LanguageTogglePill(lang = lang, onToggle = onToggleLang)
                }
            }

            // User Card
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            ) {
                                Text(
                                    text = profile.fullName.trim().take(2).uppercase().ifBlank { "DP" },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = profile.fullName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "+91 ${profile.mobileNumber}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (profile.email.isNotBlank()) {
                                    Text(
                                        text = profile.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { showEditDialog = true },
                                enabled = !isBusy,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("profile_edit_button")
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(DesiStrings.tr(lang, "Edit Profile", "प्रोफ़ाइल संपादित"))
                            }

                            OutlinedButton(
                                onClick = { showPasswordDialog = true },
                                enabled = !isBusy,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("profile_change_password_button")
                            ) {
                                Icon(Icons.Filled.LockReset, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(DesiStrings.tr(lang, "Password", "पासवर्ड बदलें"))
                            }
                        }
                    }
                }
            }

            // Financial History Links
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        ProfileMenuRow(
                            icon = Icons.Filled.AccountBalanceWallet,
                            title = DesiStrings.tr(lang, "Deposit History & Request", "डिपॉजिट इतिहास और अनुरोध"),
                            testTag = "profile_deposits_row",
                            onClick = onOpenDeposits
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
                        ProfileMenuRow(
                            icon = Icons.Filled.Outbox,
                            title = DesiStrings.tr(lang, "Withdrawal History & Request", "निकासी इतिहास और अनुरोध"),
                            testTag = "profile_withdrawals_row",
                            onClick = onOpenWithdrawals
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
                        ProfileMenuRow(
                            icon = Icons.AutoMirrored.Filled.ReceiptLong,
                            title = DesiStrings.tr(lang, "All Transaction History", "सभी लेन-देन इतिहास"),
                            testTag = "profile_transactions_row",
                            onClick = onOpenTransactions
                        )
                    }
                }
            }

            // Support & Legal Links
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        ProfileMenuRow(
                            icon = Icons.Filled.HeadsetMic,
                            title = DesiStrings.tr(lang, "Customer Support & FAQ", "ग्राहक सहायता और FAQ"),
                            testTag = "profile_support_row",
                            onClick = onOpenSupport
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
                        ProfileMenuRow(
                            icon = Icons.Filled.PrivacyTip,
                            title = DesiStrings.tr(lang, "Privacy Policy", "गोपनीयता नीति"),
                            testTag = "profile_privacy_row",
                            onClick = onOpenPrivacyPolicy
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
                        ProfileMenuRow(
                            icon = Icons.Filled.Description,
                            title = DesiStrings.tr(lang, "Terms & Conditions", "नियम और शर्तें"),
                            testTag = "profile_terms_row",
                            onClick = onOpenTerms
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = { showLogoutDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("profile_logout_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = DesiStrings.tr(lang, "Logout", "लॉगआउट करें"),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (showEditDialog) {
        var name by remember { mutableStateOf(profile.fullName) }
        var mobile by remember { mutableStateOf(profile.mobileNumber) }
        var err by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { if (!isBusy) showEditDialog = false },
            title = { Text(DesiStrings.tr(lang, "Edit Profile", "प्रोफ़ाइल संपादित करें")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; err = null },
                        label = { Text(DesiStrings.tr(lang, "Full Name", "पूरा नाम")) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_profile_name_input")
                    )
                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { input ->
                            mobile = input.filter { it.isDigit() }.take(10)
                            err = null
                        },
                        label = { Text(DesiStrings.tr(lang, "Mobile Number", "मोबाइल नंबर")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_profile_mobile_input")
                    )
                    if (err != null) {
                        Text(err!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        err = when {
                            !InputValidators.isValidFullName(name) ->
                                DesiStrings.tr(lang, "Enter valid name (2–60 chars).", "मान्य नाम दर्ज करें।")
                            !InputValidators.isValidMobile(mobile) ->
                                DesiStrings.tr(lang, "Enter valid 10-digit mobile.", "10 अंकों का मान्य मोबाइल दर्ज करें।")
                            else -> null
                        }
                        if (err == null) {
                            onUpdateProfile(name, mobile) { showEditDialog = false }
                        }
                    },
                    enabled = !isBusy,
                    modifier = Modifier.testTag("edit_profile_save_button")
                ) {
                    Text(DesiStrings.tr(lang, "Save", "सहेजें"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }, enabled = !isBusy) {
                    Text(DesiStrings.tr(lang, "Cancel", "रद्द करें"))
                }
            }
        )
    }

    if (showPasswordDialog) {
        var currentPass by remember { mutableStateOf("") }
        var newPass by remember { mutableStateOf("") }
        var confirmPass by remember { mutableStateOf("") }
        var err by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { if (!isBusy) showPasswordDialog = false },
            title = { Text(DesiStrings.tr(lang, "Change Security Password", "सुरक्षा पासवर्ड बदलें")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = currentPass,
                        onValueChange = { currentPass = it; err = null },
                        label = { Text(DesiStrings.tr(lang, "Current Password", "वर्तमान पासवर्ड")) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("change_pass_current_input")
                    )
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it; err = null },
                        label = { Text(DesiStrings.tr(lang, "New Password (min 6 chars)", "नया पासवर्ड (6+ अक्षर)")) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("change_pass_new_input")
                    )
                    OutlinedTextField(
                        value = confirmPass,
                        onValueChange = { confirmPass = it; err = null },
                        label = { Text(DesiStrings.tr(lang, "Confirm New Password", "नए पासवर्ड की पुष्टि करें")) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("change_pass_confirm_input")
                    )
                    if (err != null) {
                        Text(err!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        err = when {
                            currentPass.isBlank() -> DesiStrings.tr(lang, "Enter current password.", "वर्तमान पासवर्ड दर्ज करें।")
                            !InputValidators.isValidPassword(newPass) ->
                                DesiStrings.tr(lang, "New password must be 6+ characters.", "नया पासवर्ड कम से कम 6 अक्षरों का हो।")
                            newPass != confirmPass -> DesiStrings.tr(lang, "New passwords do not match.", "नया पासवर्ड मेल नहीं खाता।")
                            else -> null
                        }
                        if (err == null) {
                            onChangePassword(currentPass, newPass) { showPasswordDialog = false }
                        }
                    },
                    enabled = !isBusy,
                    modifier = Modifier.testTag("change_pass_save_button")
                ) {
                    Text(DesiStrings.tr(lang, "Update", "अपडेट करें"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }, enabled = !isBusy) {
                    Text(DesiStrings.tr(lang, "Cancel", "रद्द करें"))
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(DesiStrings.tr(lang, "Confirm Logout", "लॉगआउट की पुष्टि करें")) },
            text = {
                Text(
                    DesiStrings.tr(
                        lang,
                        "Are you sure you want to log out of Desi Paisa?",
                        "क्या आप वाकई देसी पैसा से लॉगआउट करना चाहते हैं?"
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_logout_button")
                ) {
                    Text(DesiStrings.tr(lang, "Logout", "लॉगआउट"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(DesiStrings.tr(lang, "Cancel", "रद्द करें"))
                }
            }
        )
    }
}

@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
