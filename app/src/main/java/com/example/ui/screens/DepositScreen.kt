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
import com.example.data.model.DepositRequest
import com.example.ui.components.DemoModeNoticeBanner
import com.example.ui.components.DemoTagBadge
import com.example.ui.components.StatusChip
import com.example.ui.components.SubScreenTopBar
import com.example.ui.theme.StatusApproved
import java.util.UUID

@Composable
fun DepositScreen(
    deposits: List<DepositRequest>,
    lang: AppLang,
    isBusy: Boolean,
    onBack: () -> Unit,
    onToggleLang: () -> Unit,
    onSubmitDeposit: (amount: Double, method: String, note: String, idempotencyKey: String, onSuccess: () -> Unit) -> Unit,
    onReviewDeposit: (depositId: String, approve: Boolean, remark: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("DEMO_UPI") }
    var referenceNote by remember { mutableStateOf("") }
    var idempotencyKey by remember { mutableStateOf(UUID.randomUUID().toString()) }
    var formError by remember { mutableStateOf<String?>(null) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        SubScreenTopBar(
            title = DesiStrings.tr(lang, "Demo Deposit", "डेमो डिपॉजिट"),
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
                                    "Request Virtual Demo Deposit",
                                    "वर्चुअल डेमो डिपॉजिट अनुरोध"
                                ),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = DesiStrings.tr(
                                    lang,
                                    "Requests start in PENDING status. Admin verification is required before wallet balance updates.",
                                    "अनुरोध 'लंबित' स्थिति में बनते हैं। बैलेंस अपडेट से पहले एडमिन सत्यापन आवश्यक है।"
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
                                label = { Text(DesiStrings.tr(lang, "Demo Amount (₹10 – ₹1,00,000)", "डेमो राशि (₹10 – ₹1,00,000)")) },
                                leadingIcon = { Icon(Icons.Filled.CurrencyRupee, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("deposit_amount_input")
                            )

                            // Quick presets
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                listOf("100", "500", "1000", "2500").forEach { preset ->
                                    FilterChip(
                                        selected = amountText == preset,
                                        onClick = { amountText = preset; formError = null },
                                        label = { Text("₹$preset") }
                                    )
                                }
                            }

                            Text(
                                text = DesiStrings.tr(lang, "Demo Payment Method", "डेमो भुगतान विधि"),
                                style = MaterialTheme.typography.labelLarge
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                listOf(
                                    "DEMO_UPI" to "Demo UPI",
                                    "DEMO_BANK" to "Demo Bank",
                                    "DEMO_CARD" to "Demo Card"
                                ).forEach { (code, label) ->
                                    FilterChip(
                                        selected = selectedMethod == code,
                                        onClick = { selectedMethod = code },
                                        label = { Text(label) }
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = referenceNote,
                                onValueChange = { referenceNote = it.take(120) },
                                label = { Text(DesiStrings.tr(lang, "Demo Reference Note (Optional)", "संदर्भ नोट (वैकल्पिक)")) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("deposit_note_input")
                            )

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
                                    if (validAmt == null) {
                                        formError = DesiStrings.tr(
                                            lang,
                                            "Enter a valid amount between ₹10 and ₹1,00,000.",
                                            "कृपया ₹10 से ₹1,00,000 के बीच मान्य राशि दर्ज करें।"
                                        )
                                    } else {
                                        onSubmitDeposit(validAmt, selectedMethod, referenceNote, idempotencyKey) {
                                            amountText = ""
                                            referenceNote = ""
                                            idempotencyKey = UUID.randomUUID().toString()
                                        }
                                    }
                                },
                                enabled = !isBusy,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("deposit_submit_button")
                            ) {
                                Text(DesiStrings.tr(lang, "Submit Demo Deposit Request", "डेमो डिपॉजिट अनुरोध भेजें"))
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = DesiStrings.tr(lang, "Deposit History", "डिपॉजिट इतिहास"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (deposits.isEmpty()) {
                    item {
                        Text(
                            text = DesiStrings.tr(
                                lang,
                                "No demo deposit requests yet.",
                                "अभी तक कोई डेमो डिपॉजिट अनुरोध नहीं है।"
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(deposits, key = { it.id.ifBlank { it.idempotencyKey } }) { dep ->
                        val docId = dep.id.ifBlank { dep.idempotencyKey }
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
                                            text = formatInr(dep.amount),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        DemoTagBadge(lang)
                                    }
                                    StatusChip(status = dep.status, lang = lang)
                                }
                                Text(
                                    text = "${dep.paymentMethod.replace("_", " ")} • ${formatTimestamp(dep.createdAt)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (dep.referenceNote.isNotBlank()) {
                                    Text(
                                        text = dep.referenceNote,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                if (dep.adminRemark.isNotBlank()) {
                                    Text(
                                        text = "${DesiStrings.tr(lang, "Admin Remark:", "एडमिन टिप्पणी:")} ${dep.adminRemark}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                // Demo Admin Verification Sandbox for Pending Requests
                                if (dep.status == "PENDING") {
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
                                                        "Demo Admin Verification Sandbox",
                                                        "डेमो एडमिन सत्यापन सैंडबॉक्स"
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
                                                        onReviewDeposit(
                                                            docId,
                                                            true,
                                                            "Verified in Demo Admin Sandbox"
                                                        )
                                                    },
                                                    enabled = !isBusy,
                                                    colors = ButtonDefaults.buttonColors(containerColor = StatusApproved),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(48.dp)
                                                        .testTag("deposit_admin_approve_$docId")
                                                ) {
                                                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(DesiStrings.tr(lang, "Approve", "स्वीकृत करें"))
                                                }
                                                OutlinedButton(
                                                    onClick = {
                                                        onReviewDeposit(
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
                                                        .testTag("deposit_admin_reject_$docId")
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
}
