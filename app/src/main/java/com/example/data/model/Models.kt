package com.example.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import com.example.R
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val mobileNumber: String = "",
    val email: String = "",
    val referralCode: String = "",
    val referredByCode: String = "",
    val walletBalance: Double = 0.0,
    val totalDeposit: Double = 0.0,
    val totalWithdrawal: Double = 0.0,
    val dailyStreakCount: Long = 0L,
    val lastCheckInEpochDay: Long = 0L,
    val passwordHash: String = "",
    val passwordSalt: String = "",
    val languagePref: String = "EN",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class DepositRequest(
    @DocumentId val id: String = "",
    val userId: String = "",
    val idempotencyKey: String = "",
    val amount: Double = 0.0,
    val paymentMethod: String = "DEMO_UPI",
    val referenceNote: String = "",
    val status: String = "PENDING",
    val demoMode: Boolean = true,
    val adminRemark: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class WithdrawalRequest(
    @DocumentId val id: String = "",
    val userId: String = "",
    val idempotencyKey: String = "",
    val amount: Double = 0.0,
    val payoutMethod: String = "UPI",
    val payoutDestination: String = "",
    val accountHolderName: String = "",
    val status: String = "PENDING",
    val demoMode: Boolean = true,
    val adminRemark: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class WalletTransaction(
    @DocumentId val id: String = "",
    val userId: String = "",
    val idempotencyKey: String = "",
    val type: String = "DEMO_DEPOSIT",
    val title: String = "",
    val titleHi: String = "",
    val amount: Double = 0.0,
    val direction: String = "CREDIT",
    val status: String = "PENDING",
    val demoMode: Boolean = true,
    val relatedRequestId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ReferralRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val inviteeName: String = "",
    val inviteeCodeOrPhone: String = "",
    val status: String = "JOINED",
    val rewardPoints: Long = 0L,
    val demoMode: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class SupportTicket(
    @DocumentId val id: String = "",
    val userId: String = "",
    val category: String = "GENERAL",
    val subject: String = "",
    val message: String = "",
    val status: String = "OPEN",
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class DemoProduct(
    val id: String,
    val nameEn: String,
    val nameHi: String,
    val categoryEn: String,
    val categoryHi: String,
    val shortDescEn: String,
    val shortDescHi: String,
    val fullDetailsEn: String,
    val fullDetailsHi: String,
    val priceDemoInr: Double,
    @DrawableRes val imageRes: Int?,
    val specsEn: List<String>,
    val specsHi: List<String>
)

object ProductCatalog {
    val products: List<DemoProduct> = listOf(
        DemoProduct(
            id = "prod_khata_tablet",
            nameEn = "Digital Khata Pro Kit",
            nameHi = "डिजिटल खाता प्रो किट",
            categoryEn = "Business Tools",
            categoryHi = "व्यापार उपकरण",
            shortDescEn = "Bilingual ledger organizer & receipt bundle for local shopkeepers (Demo Item).",
            shortDescHi = "दुकानदारों के लिए द्विभाषी बही-खाता और रसीद बंडल (डेमो उत्पाद)।",
            fullDetailsEn = "A showcase demo product demonstrating how merchants manage daily credit/debit entries, customer reminders, and itemized billing. No real-world hardware is shipped and no investment returns are offered.",
            fullDetailsHi = "यह एक डेमो उत्पाद है जो दिखाता है कि व्यापारी दैनिक उधार-जमा प्रविष्टियां और बिलिंग कैसे प्रबंधित करते हैं। कोई वास्तविक हार्डवेयर नहीं भेजा जाता और कोई निवेश रिटर्न नहीं है।",
            priceDemoInr = 1499.0,
            imageRes = R.drawable.img_prod_khata_1795000291955,
            specsEn = listOf("Demo Showcase Catalog Item", "Hindi & English Invoice Preview", "Zero Hidden Charges", "Virtual Demo Balance Only"),
            specsHi = listOf("डेमो शोकेस कैटलॉग आइटम", "हिंदी और अंग्रेजी चालान पूर्वावलोकन", "कोई छिपा हुआ शुल्क नहीं", "केवल वर्चुअल डेमो बैलेंस")
        ),
        DemoProduct(
            id = "prod_qr_soundbox",
            nameEn = "Dukaan QR Soundbox",
            nameHi = "दुकान QR साउंडबॉक्स",
            categoryEn = "Merchant Hardware",
            categoryHi = "मर्चेंट हार्डवेयर",
            shortDescEn = "Compact voice-alert counter terminal model in matte emerald & brass (Demo Item).",
            shortDescHi = "मैट एमरल्ड और ब्रास में वॉयस-अलर्ट काउंटर टर्मिनल मॉडल (डेमो उत्पाद)।",
            fullDetailsEn = "Demonstration hardware showcase unit designed for counter payment alerts in Hindi and English. Strictly a demo product for testing virtual wallet checkouts.",
            fullDetailsHi = "हिंदी और अंग्रेजी में काउंटर भुगतान अलर्ट के लिए डिज़ाइन किया गया डेमो हार्डवेयर शोकेस। यह केवल वर्चुअल वॉलेट चेकआउट परीक्षण के लिए डेमो उत्पाद है।",
            priceDemoInr = 999.0,
            imageRes = R.drawable.img_prod_soundbox_1795000292726,
            specsEn = listOf("Demo Showcase Hardware", "Bilingual Audio Alert Concept", "One-Tap Demo Checkout", "No Real Payment Required"),
            specsHi = listOf("डेमो शोकेस हार्डवेयर", "द्विभाषी ऑडियो अलर्ट मॉडल", "वन-टैप डेमो चेकआउट", "कोई वास्तविक भुगतान आवश्यक नहीं")
        ),
        DemoProduct(
            id = "prod_nfc_card",
            nameEn = "Smart NFC Vyapar Card",
            nameHi = "स्मार्ट NFC व्यापार कार्ड",
            categoryEn = "Identity & Tap",
            categoryHi = "पहचान और टैप",
            shortDescEn = "Brushed brass & emerald contactless business profile card (Demo Item).",
            shortDescHi = "ब्रश्ड ब्रास और एमरल्ड कॉन्टैक्टलेस बिजनेस प्रोफाइल कार्ड (डेमो उत्पाद)।",
            fullDetailsEn = "Demo digital business card showcase featuring instant shop profile sharing and QR verification. Purely a demonstration product with no financial yield.",
            fullDetailsHi = "तुरंत दुकान प्रोफाइल साझा करने और QR सत्यापन के लिए डेमो डिजिटल बिजनेस कार्ड। यह पूरी तरह से एक डेमो उत्पाद है।",
            priceDemoInr = 499.0,
            imageRes = R.drawable.img_prod_nfc_card_1795000293067,
            specsEn = listOf("Demo Catalog Card", "Contactless Profile Concept", "Instant Virtual Order Receipt", "Zero Real-Money Charge"),
            specsHi = listOf("डेमो कैटलॉग कार्ड", "कॉन्टैक्टलेस प्रोफाइल कॉन्सेप्ट", "तुरंत वर्चुअल ऑर्डर रसीद", "शून्य वास्तविक धन शुल्क")
        ),
        DemoProduct(
            id = "prod_gst_pack",
            nameEn = "Desi GST Billing Pack",
            nameHi = "देसी GST बिलिंग पैक",
            categoryEn = "Software Suite",
            categoryHi = "सॉफ्टवेयर सूट",
            shortDescEn = "Annual demo software license for itemized shop billing & tax summaries (Demo Item).",
            shortDescHi = "दुकान बिलिंग और कर सारांश के लिए वार्षिक डेमो सॉफ्टवेयर पैक (डेमो उत्पाद)।",
            fullDetailsEn = "Demo productivity package illustrating digital receipt generation and monthly ledger summaries for small Indian retailers. Strictly virtual and educational.",
            fullDetailsHi = "छोटे भारतीय व्यापारियों के लिए डिजिटल रसीद और मासिक बही-खाता सारांश दिखाने वाला डेमो सॉफ्टवेयर पैक।",
            priceDemoInr = 799.0,
            imageRes = null,
            specsEn = listOf("Demo Digital License", "Bilingual PDF Summary Concept", "No Subscription Auto-Debit", "Safe Demo Sandbox"),
            specsHi = listOf("डेमो डिजिटल लाइसेंस", "द्विभाषी PDF सारांश मॉडल", "कोई ऑटो-डेबिट नहीं", "सुरक्षित डेमो सैंडबॉक्स")
        ),
        DemoProduct(
            id = "prod_starter_bundle",
            nameEn = "Vyapar Starter Bundle",
            nameHi = "व्यापार स्टार्टर बंडल",
            categoryEn = "Starter Kit",
            categoryHi = "स्टार्टर किट",
            shortDescEn = "Complete demo merchant welcome pack with QR standee & ledger templates (Demo Item).",
            shortDescHi = "QR स्टैंडी और खाता टेम्पलेट्स के साथ संपूर्ण डेमो मर्चेंट पैक (डेमो उत्पाद)।",
            fullDetailsEn = "All-in-one demo starter bundle for exploring Desi Paisa's virtual order and transaction ledger flow. Zero hidden fees and no promised returns.",
            fullDetailsHi = "देसी पैसा के वर्चुअल ऑर्डर और लेन-देन प्रवाह को समझने के लिए ऑल-इन-वन डेमो स्टार्टर बंडल।",
            priceDemoInr = 299.0,
            imageRes = null,
            specsEn = listOf("Lowest Demo Price Tier", "Great for Testing Demo Orders", "Instant Ledger Entry", "100% Transparent Demo"),
            specsHi = listOf("न्यूनतम डेमो मूल्य", "डेमो ऑर्डर परीक्षण के लिए उत्तम", "तुरंत लेजर प्रविष्टि", "100% पारदर्शी डेमो")
        )
    )
}
