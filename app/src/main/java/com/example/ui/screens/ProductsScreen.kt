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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.AppLang
import com.example.core.DesiStrings
import com.example.core.formatInr
import com.example.data.model.DemoProduct
import com.example.data.model.ProductCatalog
import com.example.ui.components.LanguageTogglePill
import com.example.ui.components.PasswordConfirmDialog
import java.util.UUID

@Composable
fun ProductsScreen(
    availableBalance: Double,
    lang: AppLang,
    isBusy: Boolean,
    onToggleLang: () -> Unit,
    onPlaceDemoOrder: (product: DemoProduct, password: String, idempotencyKey: String, onSuccess: () -> Unit) -> Unit
) {
    var selectedProduct by remember { mutableStateOf<DemoProduct?>(null) }
    var productForOrderPassword by remember { mutableStateOf<DemoProduct?>(null) }
    var pendingOrderKey by remember { mutableStateOf(UUID.randomUUID().toString()) }

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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = DesiStrings.tr(lang, "Demo Products", "डेमो उत्पाद"),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = DesiStrings.tr(
                                lang,
                                "Available Demo Balance: ${formatInr(availableBalance)}",
                                "उपलब्ध डेमो बैलेंस: ${formatInr(availableBalance)}"
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    LanguageTogglePill(lang = lang, onToggle = onToggleLang)
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = DesiStrings.tr(
                            lang,
                            "Showcase Demo Catalog: These are demo utility products. No investment returns, passive profits, or guaranteed income claims are offered.",
                            "डेमो कैटलॉग: ये केवल डेमो उपयोगिता उत्पाद हैं। कोई निवेश रिटर्न या गारंटीड आय का दावा नहीं किया जाता है।"
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            items(ProductCatalog.products, key = { it.id }) { product ->
                ProductItemCard(
                    product = product,
                    lang = lang,
                    onDetailsClick = { selectedProduct = product }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    selectedProduct?.let { product ->
        val name = if (lang == AppLang.HI) product.nameHi else product.nameEn
        val details = if (lang == AppLang.HI) product.fullDetailsHi else product.fullDetailsEn
        val specs = if (lang == AppLang.HI) product.specsHi else product.specsEn

        AlertDialog(
            onDismissRequest = { selectedProduct = null },
            title = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Text(
                            text = DesiStrings.tr(lang, "DEMO PRODUCT • NO RETURNS", "डेमो उत्पाद • कोई रिटर्न नहीं"),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text(text = name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (product.imageRes != null) {
                        Image(
                            painter = painterResource(id = product.imageRes),
                            contentDescription = name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )
                    }
                    Text(
                        text = "${DesiStrings.tr(lang, "Demo Price:", "डेमो मूल्य:")} ${formatInr(product.priceDemoInr)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = details,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    specs.forEach { spec ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = spec, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingOrderKey = UUID.randomUUID().toString()
                        productForOrderPassword = product
                        selectedProduct = null
                    },
                    enabled = availableBalance >= product.priceDemoInr && !isBusy,
                    modifier = Modifier.testTag("product_try_demo_order_button")
                ) {
                    Text(
                        if (availableBalance >= product.priceDemoInr) {
                            DesiStrings.tr(lang, "Try Demo Order", "डेमो ऑर्डर करें")
                        } else {
                            DesiStrings.tr(lang, "Low Demo Balance", "कम डेमो बैलेंस")
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedProduct = null }) {
                    Text(DesiStrings.tr(lang, "Close", "बंद करें"))
                }
            }
        )
    }

    productForOrderPassword?.let { product ->
        val name = if (lang == AppLang.HI) product.nameHi else product.nameEn
        PasswordConfirmDialog(
            lang = lang,
            title = DesiStrings.tr(lang, "Confirm Demo Order", "डेमो ऑर्डर की पुष्टि करें"),
            subtitle = DesiStrings.tr(
                lang,
                "Enter your security password to place a virtual demo order for $name (${formatInr(product.priceDemoInr)}).",
                "$name (${formatInr(product.priceDemoInr)}) का वर्चुअल डेमो ऑर्डर करने के लिए अपना सुरक्षा पासवर्ड दर्ज करें।"
            ),
            confirmLabel = DesiStrings.tr(lang, "Confirm Order", "ऑर्डर पुष्टि करें"),
            isBusy = isBusy,
            onDismiss = { productForOrderPassword = null },
            onConfirm = { password ->
                onPlaceDemoOrder(product, password, pendingOrderKey) {
                    productForOrderPassword = null
                }
            }
        )
    }
}

@Composable
private fun ProductItemCard(
    product: DemoProduct,
    lang: AppLang,
    onDetailsClick: () -> Unit
) {
    val name = if (lang == AppLang.HI) product.nameHi else product.nameEn
    val category = if (lang == AppLang.HI) product.categoryHi else product.categoryEn
    val desc = if (lang == AppLang.HI) product.shortDescHi else product.shortDescEn

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(168.dp)
            ) {
                if (product.imageRes != null) {
                    Image(
                        painter = painterResource(id = product.imageRes),
                        contentDescription = name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.secondaryContainer
                                    )
                                )
                            )
                    ) {
                        Icon(
                            imageVector = if (product.id.contains("gst")) Icons.Filled.Inventory2 else Icons.Filled.Storefront,
                            contentDescription = name,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = DesiStrings.tr(lang, "DEMO PRODUCT", "डेमो उत्पाद"),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = DesiStrings.tr(lang, "Demo Price", "डेमो मूल्य"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatInr(product.priceDemoInr),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    OutlinedButton(
                        onClick = onDetailsClick,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("product_details_button_${product.id}")
                    ) {
                        Icon(Icons.Filled.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(DesiStrings.tr(lang, "Product Details", "उत्पाद विवरण"))
                    }
                }
            }
        }
    }
}
