package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import com.example.core.AppLang
import com.example.core.DesiStrings
import com.example.core.InputValidators
import com.example.core.formatInr
import com.example.core.formatTimestamp
import com.example.data.model.WithdrawalRequest
import com.example.ui.components.DemoModeNoticeBanner
import com.example.ui.components.DemoTagBadge
import com.example.ui.components.PasswordConfirmDialog
import com.example.ui.components.StatusChip
import com.example.ui.components.SubScreenTopBar
import com.example.ui.theme.StatusApproved
import java.util.UUID

@Composable
fun WithdrawalScreen(
    availableBalance: Double,
    withdrawals: List<WithdrawalRequest>,
    lang: AppLang,
    isBusy: Boolean,
    onBack: () -> Unit,
    onToggleLang: () -> Unit,
    onSubmitWithdrawal: (
        amount: Double,
        payoutMethod: String,
        payoutDestination: String,
        accountHolderName: String,
        securityPassword: String,
        idempotencyKey: String,
        onSuccess: () -> Unit
    ) -> Unit,
    onReviewWithdrawal: (withdrawalId: String, approve: Boolean, remark: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var payoutMethod by remember { mutableStateOf("UPI") }
    var holderName by remember { mutableStateOf("") }
    var upiId by remember { mutableStateOf("") }
    var bankAcc by remember { mutableStateOf("") }
    var bankIfsc by remember { mutableStateOf("") }
    var idempotencyKey by remember { mutableStateOf(UUID.randomUUID().toString()) }
    var formError by remember { mutableStateOf<String?>(null) }
    var pendingVerifiedAmount by remember { mutableStateOf<Double?>(null) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        SubScreenTopBar(
            title = DesiStrings.tr(lang, "Demo Withdrawal", "डेमो निकासी"),
            lang = lang,
            onBack = onBack,
            onToggleLang = onToggleLang
        )

        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp)
            ) {
                item { DemoModeNoticeBanner(lang = lang) }

                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(18.dp)
                        ) {
                            Text(
                                text = DesiStrings.tr(
                                    lang,
                                    "Available Demo Balance: ${formatInr(availableBalance)}",
                                    "उपलब्ध डेमो बैलेंस: ${formatInr(availableBalance)}"
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = DesiStrings.tr(
                                    lang,
                                    "Withdrawal requests require your security password and Admin Approval. Payment details are masked for safety.",
                                    "निकासी अनुरोध के लिए सुरक्षा पासवर्ड और एडमिन अनुमोदन आवश्यक है।"
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedTextField(
                                value = amountText,
                                onValueChange = { input ->
                                    amountText = input.filter { it.isDigit() || it == '.' }
                                    formError = null
                                },
                                label = { Text(DesiStrings.tr(lang, "Withdrawal Amount (₹)", "निकासी राशि (₹)")) },
                                leadingIcon = { Icon(Icons.Filled.CurrencyRupee, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("withdrawal_amount_input")
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = payoutMethod == "UPI",
                                    onClick = { payoutMethod = "UPI"; formError = null },
                                    label = { Text("Demo UPI") }
                                )
                                FilterChip(
                                    selected = payoutMethod == "BANK_TRANSFER",
                                    onClick = { payoutMethod = "BANK_TRANSFER"; formError = null },
                                    label = { Text(DesiStrings.tr(lang, "Demo Bank Transfer", "डेमो बैंक ट्रांसफर")) }
                                )
                            }

                            OutlinedTextField(
                                value = holderName,
                                onValueChange = { holderName = it.take(60); formError = null },
                                label = { Text(DesiStrings.tr(lang, "Account Holder Name", "खाताधारक का नाम")) },
                                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("withdrawal_holder_input")
                            )

                            if (payoutMethod == "UPI") {
                                OutlinedTextField(
                                    value = upiId,
                                    onValueChange = { upiId = it.trim(); formError = null },
                                    label = { Text(DesiStrings.tr(lang, "Demo UPI ID (e.g. name@upi)", "डेमो UPI आईडी")) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("withdrawal_upi_input")
                                )
                            } else {
                                OutlinedTextField(
                                    value = bankAcc,
                                    onValueChange = { input ->
                                        bankAcc = input.filter { it.isDigit() }.take(18)
                                        formError = null
                                    },
                                    label = { Text(DesiStrings.tr(lang, "Demo Bank Account (9–18 digits)", "डेमो बैंक खाता संख्या")) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("withdrawal_bank_input")
                                )
                                OutlinedTextField(
                                    value = bankIfsc,
                                    onValueChange = { bankIfsc = it.uppercase().take(11); formError = null },
                                    label = { Text("IFSC Code (e.g. SBIN0001234)") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("withdrawal_ifsc_input")
                                )
                            }

                            if (formError != null) {
                                Text(
                                    text = formError!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            Button(
                                onClick = {
                                    val validAmt = InputValidators.isValidAmount(amountText)
                                    formError = when {
                                        validAmt == null ->
                                            DesiStrings.tr(lang, "Enter a valid amount (₹10 – ₹1,00,000).", "कृपया मान्य राशि (₹10 – ₹1,00,000) दर्ज करें।")
                                        validAmt > availableBalance ->
                                            DesiStrings.tr(lang, "Amount exceeds available balance.", "राशि उपलब्ध बैलेंस से अधिक है।")
                                        !InputValidators.isValidFullName(holderName) ->
                                            DesiStrings.tr(lang, "Enter valid account holder name.", "मान्य खाताधारक नाम दर्ज करें।")
                                        payoutMethod == "UPI" && !InputValidators.isValidUpiId(upiId) ->
                                            DesiStrings.tr(lang, "Enter valid UPI ID (e.g. name@bank).", "मान्य UPI आईडी दर्ज करें।")
                                        payoutMethod == "BANK_TRANSFER" && !InputValidators.isValidBankDetails(bankAcc, bankIfsc) ->
                                            DesiStrings.tr(lang, "Enter valid 9–18 digit account and 11-char IFSC.", "मान्य खाता संख्या और 11-अक्षरीय IFSC दर्ज करें।")
                                        else -> null
                                    }
                                    if (formError == null && validAmt != null) {
                                        pendingVerifiedAmount = validAmt
                                    }
                                },
                                enabled = !isBusy,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("withdrawal_submit_button")
                            ) {
                                Text(DesiStrings.tr(lang, "Continue to Security Verification", "सुरक्षा सत्यापन पर जाएं"))
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = DesiStrings.tr(lang, "Withdrawal History", "निकासी इतिहास"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (withdrawals.isEmpty()) {
                    item {
                        Text(
                            text = DesiStrings.tr(
                                lang,
                                "No demo withdrawal requests yet.",
                                "अभी तक कोई डेमो निकासी अनुरोध नहीं है।"
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(withdrawals, key = { it.id.ifBlank { it.idempotencyKey } }) { wd ->
                        val docId = wd.id.ifBlank { wd.idempotencyKey }
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = formatInr(wd.amount),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        DemoTagBadge(lang)
                                    }
                                    StatusChip(status = wd.status, lang = lang)
                                }
                                Text(
                                    text = "${wd.accountHolderName} • ${wd.payoutMethod} (${wd.payoutDestination})",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = formatTimestamp(wd.createdAt),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (wd.adminRemark.isNotBlank()) {
                                    Text(
                                        text = "${DesiStrings.tr(lang, "Admin Remark:", "एडमिन टिप्पणी:")} ${wd.adminRemark}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                if (wd.status == "PENDING") {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.padding(10.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Filled.AdminPanelSettings,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = DesiStrings.tr(
                                                        lang,
                                                        "Demo Admin Approval Sandbox",
                                                        "डेमो एडमिन अनुमोदन सैंडबॉक्स"
                                                    ),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Button(
                                                    onClick = {
                                                        onReviewWithdrawal(
                                                            docId,
                                                            true,
                                                            "Approved in Demo Admin Sandbox"
                                                        )
                                                    },
                                                    enabled = !isBusy,
                                                    colors = ButtonDefaults.buttonColors(containerColor = StatusApproved),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(48.dp)
                                                        .testTag("withdrawal_admin_approve_$docId")
                                                ) {
                                                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(DesiStrings.tr(lang, "Approve", "स्वीकृत करें"))
                                                }
                                                OutlinedButton(
                                                    onClick = {
                                                        onReviewWithdrawal(
                                                            docId,
                                                            false,
                                                            "Rejected in Demo Admin Sandbox"
                                                        )
                                                    },
                                                    enabled = !isBusy,
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(48.dp)
                                                        .testTag("withdrawal_admin_reject_$docId")
                                                ) {
                                                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(DesiStrings.tr(lang, "Reject", "अस्वीकृत"))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }

    pendingVerifiedAmount?.let { amount ->
        val destination = if (payoutMethod == "UPI") upiId else "$bankAcc ($bankIfsc)"
        PasswordConfirmDialog(
            lang = lang,
            title = DesiStrings.tr(lang, "Authorize Demo Withdrawal", "डेमो निकासी की पुष्टि करें"),
            subtitle = DesiStrings.tr(
                lang,
                "Verify your security password to submit a ${formatInr(amount)} demo withdrawal request.",
                "${formatInr(amount)} का डेमो निकासी अनुरोध भेजने के लिए अपना सुरक्षा पासवर्ड दर्ज करें।"
            ),
            confirmLabel = DesiStrings.tr(lang, "Submit Request", "अनुरोध भेजें"),
            isBusy = isBusy,
            onDismiss = { pendingVerifiedAmount = null },
            onConfirm = { password ->
                onSubmitWithdrawal(
                    amount,
                    payoutMethod,
                    destination,
                    holderName,
                    password,
                    idempotencyKey
                ) {
                    amountText = ""
                    upiId = ""
                    bankAcc = ""
                    bankIfsc = ""
                    idempotencyKey = UUID.randomUUID().toString()
                    pendingVerifiedAmount = null
                }
            }
        )
    }
}
