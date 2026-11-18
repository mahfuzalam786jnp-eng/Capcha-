package com.example.ui.screens

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.core.AppLang
import com.example.core.DesiStrings
import com.example.data.model.WalletTransaction
import com.example.ui.components.DemoModeNoticeBanner
import com.example.ui.components.SubScreenTopBar
import com.example.ui.components.TransactionRowCard

@Composable
fun TransactionsScreen(
    transactions: List<WalletTransaction>,
    lang: AppLang,
    onBack: () -> Unit,
    onToggleLang: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filtered = remember(transactions, selectedFilter) {
        when (selectedFilter) {
            "DEPOSIT" -> transactions.filter { it.type == "DEMO_DEPOSIT" }
            "WITHDRAWAL" -> transactions.filter { it.type == "DEMO_WITHDRAWAL" }
            "ACTIVITY" -> transactions.filter {
                it.type in listOf("DAILY_ACTIVITY", "REFERRAL_REWARD", "DEMO_PRODUCT_ORDER")
            }
            else -> transactions
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        SubScreenTopBar(
            title = DesiStrings.tr(lang, "Transaction History", "लेन-देन इतिहास"),
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
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp)
            ) {
                item { DemoModeNoticeBanner(lang = lang) }

                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val filters = listOf(
                            "ALL" to DesiStrings.tr(lang, "All", "सभी"),
                            "DEPOSIT" to DesiStrings.tr(lang, "Deposits", "जमा"),
                            "WITHDRAWAL" to DesiStrings.tr(lang, "Withdrawals", "निकासी"),
                            "ACTIVITY" to DesiStrings.tr(lang, "Activity & Orders", "गतिविधि और ऑर्डर")
                        )
                        items(filters, key = { it.first }) { (key, label) ->
                            FilterChip(
                                selected = selectedFilter == key,
                                onClick = { selectedFilter = key },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                if (filtered.isEmpty()) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp)
                        ) {
                            Text(
                                text = DesiStrings.tr(
                                    lang,
                                    "No matching demo transactions found.",
                                    "कोई मेल खाता डेमो लेन-देन नहीं मिला।"
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(filtered, key = { it.id.ifBlank { it.idempotencyKey } }) { tx ->
                        TransactionRowCard(tx = tx, lang = lang)
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}
