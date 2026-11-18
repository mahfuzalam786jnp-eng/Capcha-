package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.AppLang
import com.example.core.DesiStrings
import com.example.core.formatTimestamp
import com.example.data.model.ReferralRecord
import com.example.data.model.UserProfile
import com.example.ui.components.DemoTagBadge
import com.example.ui.components.LanguageTogglePill
import com.example.ui.components.StatusChip

@Composable
fun InviteScreen(
    profile: UserProfile,
    referrals: List<ReferralRecord>,
    lang: AppLang,
    isBusy: Boolean,
    onToggleLang: () -> Unit,
    onCopiedCode: (String) -> Unit,
    onAddDemoReferral: (name: String, contact: String, onSuccess: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var showDemoInviteDialog by remember { mutableStateOf(false) }

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
                        text = DesiStrings.tr(lang, "Invite & Referrals", "आमंत्रण और रेफरल"),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    LanguageTogglePill(lang = lang, onToggle = onToggleLang)
                }
            }

            // Unique Referral Code Card
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = DesiStrings.tr(lang, "Your Unique Referral Code", "आपका अद्वितीय रेफरल कोड"),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = profile.referralCode,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.testTag("referral_code_text")
                                )
                                DemoTagBadge(lang)
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(profile.referralCode))
                                    onCopiedCode(
                                        DesiStrings.tr(
                                            lang,
                                            "Referral code ${profile.referralCode} copied!",
                                            "रेफरल कोड ${profile.referralCode} कॉपी किया गया!"
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("copy_referral_button")
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(DesiStrings.tr(lang, "Copy Code", "कोड कॉपी करें"))
                            }

                            Button(
                                onClick = {
                                    val shareMsg = DesiStrings.tr(
                                        lang,
                                        "Join me on Desi Paisa (Demo Wallet)! Use my referral code ${profile.referralCode} during onboarding.",
                                        "देसी पैसा (डेमो वॉलेट) से जुड़ें! साइन-अप के समय मेरा रेफरल कोड ${profile.referralCode} उपयोग करें।"
                                    )
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "Desi Paisa Invitation")
                                        putExtra(Intent.EXTRA_TEXT, shareMsg)
                                    }
                                    context.startActivity(
                                        Intent.createChooser(
                                            intent,
                                            DesiStrings.tr(lang, "Share Desi Paisa Invite", "देसी पैसा निमंत्रण साझा करें")
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("share_invite_button")
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(DesiStrings.tr(lang, "Share Invite", "निमंत्रण भेजें"))
                            }
                        }
                    }
                }
            }

            // Stats Row + Demo Referral Test Button
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.People, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = DesiStrings.tr(lang, "Total Invited Users", "कुल आमंत्रित उपयोगकर्ता"),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${referrals.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.GroupAdd, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = DesiStrings.tr(lang, "Demo Rewards", "डेमो बोनस"),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "₹${referrals.sumOf { it.rewardPoints }}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = { showDemoInviteDialog = true },
                    enabled = !isBusy,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("add_demo_referral_button")
                ) {
                    Icon(Icons.Filled.PersonAddAlt1, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        DesiStrings.tr(
                            lang,
                            "Test Demo Referral Entry (+₹50 Demo Bonus)",
                            "डेमो रेफरल प्रविष्टि जोड़ें (+₹50 डेमो बोनस)"
                        )
                    )
                }
            }

            item {
                Text(
                    text = DesiStrings.tr(lang, "Referral History", "रेफरल इतिहास"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (referrals.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.People,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = DesiStrings.tr(
                                    lang,
                                    "No referrals recorded yet. Share your code or add a Demo Referral entry above.",
                                    "अभी तक कोई रेफरल नहीं है। अपना कोड साझा करें या ऊपर डेमो रेफरल जोड़ें।"
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(referrals, key = { it.id.ifBlank { it.inviteeName + it.createdAt?.seconds } }) { item ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = item.inviteeName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (item.demoMode) DemoTagBadge(lang)
                                }
                                Text(
                                    text = "${item.inviteeCodeOrPhone} • ${formatTimestamp(item.createdAt)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "+₹${item.rewardPoints}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                StatusChip(status = item.status, lang = lang)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (showDemoInviteDialog) {
        var inviteeName by remember { mutableStateOf("") }
        var inviteeContact by remember { mutableStateOf("") }
        var err by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { if (!isBusy) showDemoInviteDialog = false },
            title = { Text(DesiStrings.tr(lang, "Add Demo Referral", "डेमो रेफरल जोड़ें")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = DesiStrings.tr(
                            lang,
                            "Simulates a verified demo referral under your code ${profile.referralCode}.",
                            "आपके कोड ${profile.referralCode} के अंतर्गत एक सत्यापित डेमो रेफरल जोड़ता है।"
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = inviteeName,
                        onValueChange = { inviteeName = it; err = null },
                        label = { Text(DesiStrings.tr(lang, "Friend Name", "मित्र का नाम")) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("demo_referral_name_input")
                    )
                    OutlinedTextField(
                        value = inviteeContact,
                        onValueChange = { inviteeContact = it; err = null },
                        label = { Text(DesiStrings.tr(lang, "Masked Phone or Code", "फ़ोन या कोड")) },
                        placeholder = { Text("98****3210") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("demo_referral_contact_input")
                    )
                    if (err != null) {
                        Text(err!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inviteeName.trim().length < 2) {
                            err = DesiStrings.tr(lang, "Enter friend's name", "मित्र का नाम दर्ज करें")
                        } else if (inviteeContact.trim().length < 4) {
                            err = DesiStrings.tr(lang, "Enter at least 4 characters", "कम से कम 4 अक्षर दर्ज करें")
                        } else {
                            onAddDemoReferral(inviteeName, inviteeContact) {
                                showDemoInviteDialog = false
                            }
                        }
                    },
                    enabled = !isBusy,
                    modifier = Modifier.testTag("demo_referral_confirm_button")
                ) {
                    Text(DesiStrings.tr(lang, "Record Demo Referral", "डेमो रेफरल दर्ज करें"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDemoInviteDialog = false }, enabled = !isBusy) {
                    Text(DesiStrings.tr(lang, "Cancel", "रद्द करें"))
                }
            }
        )
    }
}
