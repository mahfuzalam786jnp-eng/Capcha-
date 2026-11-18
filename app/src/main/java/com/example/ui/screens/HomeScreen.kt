package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Outbox
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.AppLang
import com.example.core.DesiStrings
import com.example.core.currentEpochDay
import com.example.core.formatInr
import com.example.data.model.UserProfile
import com.example.data.model.WalletTransaction
import com.example.ui.components.DemoModeNoticeBanner
import com.example.ui.components.DesiLogoMark
import com.example.ui.components.LanguageTogglePill
import com.example.ui.components.TransactionRowCard
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SaffronBright
import com.example.ui.theme.StatusApproved

@Composable
fun HomeScreen(
    profile: UserProfile,
    availableBalance: Double,
    transactions: List<WalletTransaction>,
    lang: AppLang,
    isBusy: Boolean,
    onToggleLang: () -> Unit,
    onClaimDailyActivity: () -> Unit,
    onOpenDeposit: () -> Unit,
    onOpenWithdrawal: () -> Unit,
    onOpenTransactions: () -> Unit,
    onOpenSupport: () -> Unit,
    onOpenProducts: () -> Unit
) {
    val alreadyClaimedToday = profile.lastCheckInEpochDay >= currentEpochDay()

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        DesiLogoMark(sizeDp = 42)
                        Column {
                            Text(
                                text = DesiStrings.tr(lang, "Desi Paisa", "देसी पैसा"),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = DesiStrings.tr(
                                    lang,
                                    "Namaste, ${profile.fullName.split(" ").firstOrNull() ?: profile.fullName}",
                                    "नमस्ते, ${profile.fullName.split(" ").firstOrNull() ?: profile.fullName}"
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = onOpenSupport,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("home_support_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.HeadsetMic,
                                contentDescription = DesiStrings.tr(lang, "Customer Support", "ग्राहक सहायता"),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        LanguageTogglePill(lang = lang, onToggle = onToggleLang)
                    }
                }
            }

            item {
                DemoModeNoticeBanner(lang = lang)
            }

            // Attractive Hero Banner Section with custom generated art
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_banner_1795000291776),
                            contentDescription = "Desi Paisa Hero Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .matchParentSize()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            EmeraldDeep.copy(alpha = 0.88f),
                                            EmeraldPrimary.copy(alpha = 0.55f)
                                        )
                                    )
                                )
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = DesiStrings.tr(lang, "TOTAL WALLET BALANCE", "कुल वॉलेट बैलेंस"),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = SaffronBright.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = DesiStrings.tr(lang, "DEMO INR", "डेमो ₹"),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFE0A6),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Text(
                                text = formatInr(profile.walletBalance),
                                style = MaterialTheme.typography.displaySmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${DesiStrings.tr(lang, "Available to withdraw / spend:", "उपलब्ध बैलेंस:")} ${formatInr(availableBalance)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = onOpenDeposit,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SaffronBright,
                                        contentColor = Color(0xFF231400)
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("home_deposit_button")
                                ) {
                                    Icon(Icons.Filled.AddCard, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = DesiStrings.tr(lang, "Deposit", "जमा करें"),
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                FilledTonalButton(
                                    onClick = onOpenWithdrawal,
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = Color.White.copy(alpha = 0.18f),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("home_withdraw_button")
                                ) {
                                    Icon(Icons.Filled.Outbox, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = DesiStrings.tr(lang, "Withdraw", "निकासी"),
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4 Wallet Metric Tiles (2x2 Grid)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        WalletStatCard(
                            title = DesiStrings.tr(lang, "Wallet Balance", "वॉलेट बैलेंस"),
                            value = formatInr(profile.walletBalance),
                            icon = Icons.Filled.AccountBalanceWallet,
                            accent = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        WalletStatCard(
                            title = DesiStrings.tr(lang, "Available Balance", "उपलब्ध बैलेंस"),
                            value = formatInr(availableBalance),
                            icon = Icons.Filled.Savings,
                            accent = StatusApproved,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        WalletStatCard(
                            title = DesiStrings.tr(lang, "Total Deposit", "कुल जमा"),
                            value = formatInr(profile.totalDeposit),
                            icon = Icons.Filled.ArrowCircleDown,
                            accent = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                        WalletStatCard(
                            title = DesiStrings.tr(lang, "Total Withdrawal", "कुल निकासी"),
                            value = formatInr(profile.totalWithdrawal),
                            icon = Icons.Filled.ArrowCircleUp,
                            accent = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Daily Activity Section
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.LocalFireDepartment,
                                        contentDescription = "Daily Activity",
                                        tint = Color.White
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = DesiStrings.tr(
                                        lang,
                                        "Daily Activity • Streak ${profile.dailyStreakCount}",
                                        "दैनिक गतिविधि • स्ट्रीक ${profile.dailyStreakCount}"
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = DesiStrings.tr(
                                        lang,
                                        "Claim +₹25 Demo Activity Points once daily",
                                        "प्रतिदिन +₹25 डेमो एक्टिविटी बोनस प्राप्त करें"
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onClaimDailyActivity,
                            enabled = !alreadyClaimedToday && !isBusy,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("daily_activity_button")
                        ) {
                            if (alreadyClaimedToday) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(DesiStrings.tr(lang, "Done", "पूर्ण"))
                            } else {
                                Text(DesiStrings.tr(lang, "Claim", "क्लेम"))
                            }
                        }
                    }
                }
            }

            // Promotional Demo Catalog Banner
            item {
                Card(
                    onClick = onOpenProducts,
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_banner_products_cta")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_prod_soundbox_1795000292726),
                            contentDescription = "Featured Demo Product",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = DesiStrings.tr(
                                    lang,
                                    "Explore Desi Paisa Demo Store",
                                    "देसी पैसा डेमो स्टोर देखें"
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = DesiStrings.tr(
                                    lang,
                                    "Browse utility kits & test virtual orders — no investment claims.",
                                    "मर्चेंट किट देखें और वर्चुअल ऑर्डर आज़माएं।"
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Latest Transactions Header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = DesiStrings.tr(lang, "Latest Transactions", "नवीनतम लेन-देन"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(
                        onClick = onOpenTransactions,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("home_view_all_transactions")
                    ) {
                        Text(DesiStrings.tr(lang, "View All", "सभी देखें"))
                    }
                }
            }

            if (transactions.isEmpty()) {
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
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = DesiStrings.tr(
                                    lang,
                                    "No demo transactions yet. Claim your Daily Activity bonus or submit a Demo Deposit!",
                                    "अभी तक कोई डेमो लेन-देन नहीं है। अपना दैनिक बोनस क्लेम करें या डेमो डिपॉजिट करें!"
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(transactions.take(5), key = { it.id.ifBlank { it.idempotencyKey } }) { tx ->
                    TransactionRowCard(tx = tx, lang = lang)
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun WalletStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
