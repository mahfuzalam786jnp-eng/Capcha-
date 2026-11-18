package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.AppLang
import com.example.core.DesiStrings
import com.example.core.formatTimestamp
import com.example.data.model.SupportTicket
import com.example.ui.components.StatusChip
import com.example.ui.components.SubScreenTopBar

@Composable
fun SupportScreen(
    tickets: List<SupportTicket>,
    lang: AppLang,
    isBusy: Boolean,
    onBack: () -> Unit,
    onToggleLang: () -> Unit,
    onNotify: (String) -> Unit,
    onSubmitTicket: (category: String, subject: String, message: String, onSuccess: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val supportEmail = stringResource(id = R.string.support_email)

    var expandedFaqIndex by remember { mutableStateOf<Int?>(0) }
    var category by remember { mutableStateOf("GENERAL") }
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }

    val faqs = remember(lang) { DesiStrings.faqs(lang) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        SubScreenTopBar(
            title = DesiStrings.tr(lang, "Customer Support", "ग्राहक सहायता"),
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
                // Contact Support Card
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(18.dp)
                        ) {
                            Text(
                                text = DesiStrings.tr(lang, "Contact Desi Paisa Help Desk", "देसी पैसा सहायता डेस्क से संपर्क करें"),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "$supportEmail • ${DesiStrings.tr(lang, "Mon–Sat, 9 AM – 7 PM IST", "सोम–शनि, सुबह 9 – शाम 7 बजे")}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = {
                                        val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("mailto:$supportEmail")
                                            putExtra(Intent.EXTRA_SUBJECT, "Desi Paisa Support Request")
                                        }
                                        runCatching {
                                            context.startActivity(mailIntent)
                                        }.onFailure {
                                            clipboard.setText(AnnotatedString(supportEmail))
                                            onNotify(
                                                DesiStrings.tr(
                                                    lang,
                                                    "No email app found. Copied $supportEmail",
                                                    "ईमेल ऐप नहीं मिला। $supportEmail कॉपी किया गया"
                                                )
                                            )
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("support_email_button")
                                ) {
                                    Icon(Icons.Filled.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(DesiStrings.tr(lang, "Email Us", "ईमेल करें"))
                                }
                                OutlinedButton(
                                    onClick = {
                                        clipboard.setText(AnnotatedString(supportEmail))
                                        onNotify(
                                            DesiStrings.tr(lang, "Support email copied!", "सहायता ईमेल कॉपी हो गया!")
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("support_copy_email_button")
                                ) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(DesiStrings.tr(lang, "Copy Email", "ईमेल कॉपी"))
                                }
                            }
                        }
                    }
                }

                // FAQ Section
                item {
                    Text(
                        text = DesiStrings.tr(lang, "Frequently Asked Questions", "अक्सर पूछे जाने वाले प्रश्न (FAQ)"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                itemsIndexed(faqs) { index, faq ->
                    val expanded = expandedFaqIndex == index
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .clickable { expandedFaqIndex = if (expanded) null else index }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = faq.question,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = if (expanded) "Collapse" else "Expand"
                                )
                            }
                            AnimatedVisibility(visible = expanded) {
                                Text(
                                    text = faq.answer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Report a Problem Form
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.ReportProblem, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = DesiStrings.tr(lang, "Report a Problem", "समस्या की रिपोर्ट करें"),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val cats = listOf(
                                    "GENERAL" to DesiStrings.tr(lang, "General", "सामान्य"),
                                    "DEPOSIT_ISSUE" to DesiStrings.tr(lang, "Deposit", "डिपॉजिट"),
                                    "WITHDRAWAL_ISSUE" to DesiStrings.tr(lang, "Withdrawal", "निकासी"),
                                    "BUG_REPORT" to DesiStrings.tr(lang, "Bug", "बग"),
                                    "ACCOUNT" to DesiStrings.tr(lang, "Account", "खाता")
                                )
                                items(cats, key = { it.first }) { (key, label) ->
                                    FilterChip(
                                        selected = category == key,
                                        onClick = { category = key },
                                        label = { Text(label) }
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = subject,
                                onValueChange = { subject = it.take(100); formError = null },
                                label = { Text(DesiStrings.tr(lang, "Subject", "विषय")) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("support_subject_input")
                            )

                            OutlinedTextField(
                                value = message,
                                onValueChange = { message = it.take(1000); formError = null },
                                label = { Text(DesiStrings.tr(lang, "Describe your issue", "अपनी समस्या लिखें")) },
                                minLines = 3,
                                maxLines = 5,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("support_message_input")
                            )

                            if (formError != null) {
                                Text(formError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }

                            Button(
                                onClick = {
                                    formError = when {
                                        subject.trim().length < 3 ->
                                            DesiStrings.tr(lang, "Enter a subject (3+ chars).", "कृपया विषय (3+ अक्षर) दर्ज करें।")
                                        message.trim().length < 5 ->
                                            DesiStrings.tr(lang, "Describe your issue (5+ chars).", "कृपया समस्या का विवरण (5+ अक्षर) लिखें।")
                                        else -> null
                                    }
                                    if (formError == null) {
                                        onSubmitTicket(category, subject, message) {
                                            subject = ""
                                            message = ""
                                        }
                                    }
                                },
                                enabled = !isBusy,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("support_submit_button")
                            ) {
                                Text(DesiStrings.tr(lang, "Submit Support Ticket", "सहायता टिकट सबमिट करें"))
                            }
                        }
                    }
                }

                if (tickets.isNotEmpty()) {
                    item {
                        Text(
                            text = DesiStrings.tr(lang, "Your Submitted Tickets", "आपके सबमिट किए गए टिकट"),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(tickets, key = { it.id.ifBlank { it.subject + it.createdAt?.seconds } }) { ticket ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = ticket.subject,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    StatusChip(status = ticket.status, lang = lang)
                                }
                                Text(
                                    text = ticket.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${ticket.category.replace("_", " ")} • ${formatTimestamp(ticket.createdAt)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}
