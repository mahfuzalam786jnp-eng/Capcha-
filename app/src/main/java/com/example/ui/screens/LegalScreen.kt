package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.core.AppLang
import com.example.core.DesiStrings
import com.example.ui.components.SubScreenTopBar

@Composable
fun LegalScreen(
    isPrivacyPolicy: Boolean,
    lang: AppLang,
    onBack: () -> Unit,
    onToggleLang: () -> Unit
) {
    val title = if (isPrivacyPolicy) {
        DesiStrings.tr(lang, "Privacy Policy", "गोपनीयता नीति")
    } else {
        DesiStrings.tr(lang, "Terms & Conditions", "नियम और शर्तें")
    }
    val body = if (isPrivacyPolicy) {
        DesiStrings.privacyPolicyText(lang)
    } else {
        DesiStrings.termsText(lang)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        SubScreenTopBar(
            title = title,
            lang = lang,
            onBack = onBack,
            onToggleLang = onToggleLang
        )

        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 720.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}
