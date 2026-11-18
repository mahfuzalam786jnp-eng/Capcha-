package com.example.core

import com.google.firebase.Timestamp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

enum class AppLang(val code: String, val label: String) {
    EN("EN", "English"),
    HI("HI", "हिन्दी");

    companion object {
        fun fromCode(raw: String): AppLang = entries.firstOrNull { it.code == raw } ?: EN
    }
}

fun formatInr(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("en").setRegion("IN").build())
    return formatter.format(amount)
}

fun formatTimestamp(timestamp: Timestamp?): String {
    val date = (timestamp ?: Timestamp.now()).toDate()
    return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(date)
}

fun currentEpochDay(): Long = System.currentTimeMillis() / 86_400_000L

data class FaqEntry(val question: String, val answer: String)

object DesiStrings {
    fun tr(lang: AppLang, en: String, hi: String): String =
        if (lang == AppLang.HI) hi else en

    fun navHome(lang: AppLang) = tr(lang, "Home", "होम")
    fun navProducts(lang: AppLang) = tr(lang, "Products", "उत्पाद")
    fun navInvite(lang: AppLang) = tr(lang, "Invite", "आमंत्रण")
    fun navProfile(lang: AppLang) = tr(lang, "Profile", "प्रोफ़ाइल")

    fun demoBanner(lang: AppLang) = tr(
        lang,
        "DEMO MODE • Virtual balance only. No real-money deposits or withdrawals.",
        "डेमो मोड • केवल वर्चुअल बैलेंस। कोई वास्तविक धन लेन-देन नहीं।"
    )

    fun demoBadge(lang: AppLang) = tr(lang, "DEMO", "डेमो")

    fun statusLabel(lang: AppLang, status: String): String = when (status.uppercase()) {
        "PENDING" -> tr(lang, "Pending", "लंबित")
        "APPROVED" -> tr(lang, "Approved", "स्वीकृत")
        "REJECTED" -> tr(lang, "Rejected", "अस्वीकृत")
        "COMPLETED" -> tr(lang, "Completed", "पूर्ण")
        "JOINED" -> tr(lang, "Joined", "जुड़े")
        "VERIFIED" -> tr(lang, "Verified", "सत्यापित")
        "OPEN" -> tr(lang, "Open", "खुला")
        "RESOLVED" -> tr(lang, "Resolved", "सुलझाया गया")
        else -> status
    }

    fun faqs(lang: AppLang): List<FaqEntry> = if (lang == AppLang.HI) {
        listOf(
            FaqEntry(
                "क्या देसी पैसा वास्तविक धन का उपयोग करता है?",
                "नहीं। यह ऐप पूरी तरह से डेमो मोड में चलता है। वॉलेट बैलेंस, डिपॉजिट, निकासी और उत्पाद ऑर्डर केवल शैक्षिक और प्रदर्शन उपयोग के लिए वर्चुअल हैं।"
            ),
            FaqEntry(
                "डेमो डिपॉजिट के बाद बैलेंस तुरंत क्यों नहीं बढ़ता?",
                "सुरक्षा और पारदर्शिता के लिए प्रत्येक डिपॉजिट अनुरोध 'लंबित' (Pending) स्थिति में बनाया जाता है। व्यवस्थापक (Admin) सत्यापन के बाद ही बैलेंस अपडेट होता है।"
            ),
            FaqEntry(
                "मेरा सुरक्षा पासवर्ड कैसे सुरक्षित रखा जाता है?",
                "आपका पासवर्ड कभी भी सादे टेक्स्ट में सेव नहीं किया जाता। इसे 16-बाइट रैंडम सॉल्ट के साथ PBKDF2WithHmacSHA256 एल्गोरिदम द्वारा एन्क्रिप्ट (हैश) किया जाता है।"
            ),
            FaqEntry(
                "डुप्लिकेट लेन-देन से सुरक्षा कैसे होती है?",
                "प्रत्येक डिपॉजिट, निकासी और ऑर्डर के लिए एक अद्वितीय Idempotency Key और Firestore ट्रांज़ैक्शन लॉक का उपयोग किया जाता है ताकि एक ही अनुरोध दो बार प्रोसेस न हो।"
            ),
            FaqEntry(
                "रेफरल कोड कैसे साझा करें?",
                "आमंत्रण (Invite) पेज पर जाएं, अपना अद्वितीय कोड कॉपी करें या 'निमंत्रण साझा करें' बटन से अपने मित्रों को भेजें।"
            )
        )
    } else {
        listOf(
            FaqEntry(
                "Does Desi Paisa use real money?",
                "No. Desi Paisa operates strictly in Demo Mode. All wallet balances, deposits, withdrawals, and product orders use virtual demo credits only—no real-money payments are processed."
            ),
            FaqEntry(
                "Why does a Demo Deposit wait for Admin Verification?",
                "Every deposit request is created in PENDING status. Balance is credited only after Admin Verification completes, enforcing an auditable approval flow. You can test this workflow inside the Demo Admin Verification sandbox."
            ),
            FaqEntry(
                "How is my security password protected?",
                "Passwords are never stored in plain text. Your security password is hashed locally using PBKDF2WithHmacSHA256 (65,536 iterations) with a unique 16-byte cryptographic salt."
            ),
            FaqEntry(
                "How are duplicate transactions prevented?",
                "Every deposit, withdrawal, and order generates a unique UUID idempotency key used inside atomic Firestore transactions with terminal state locking."
            ),
            FaqEntry(
                "How does Available Balance work?",
                "Available Balance equals your Wallet Balance minus any Pending withdrawal requests, preventing over-withdrawal."
            )
        )
    }

    fun privacyPolicyText(lang: AppLang): String = if (lang == AppLang.HI) {
        """
        देसी पैसा (Desi Paisa) गोपनीयता नीति

        1. डेटा संग्रह और उपयोग:
        हम केवल आपके Google साइन-इन खाता आईडी, नाम, मोबाइल नंबर और ऐप प्राथमिकताएं सुरक्षित रूप से आपकी उपयोगकर्ता प्रोफ़ाइल प्रबंधित करने के लिए संग्रहीत करते हैं।

        2. पासवर्ड सुरक्षा:
        आपका सुरक्षा पासवर्ड कभी भी सादे टेक्स्ट में संग्रहीत नहीं किया जाता। हम PBKDF2WithHmacSHA256 हैशिंग और क्रिप्टोग्राफिक सॉल्ट का उपयोग करते हैं।

        3. उपयोगकर्ता-विशिष्ट डेटा अलगाव:
        आपका वॉलेट, लेन-देन, रेफरल और सहायता टिकट केवल आपके प्रमाणित खाता (UID) तक सीमित हैं। कोई अन्य उपयोगकर्ता आपका डेटा नहीं देख सकता।

        4. डेमो मोड अस्वीकरण:
        यह एप्लिकेशन वास्तविक बैंकिंग या यूपीआई भुगतान सेवा नहीं है। कोई वास्तविक वित्तीय कार्ड या बैंक पिन एकत्र नहीं किया जाता है।

        5. डेटा हटाना और लॉगआउट:
        आप किसी भी समय प्रोफ़ाइल पेज से लॉगआउट कर सकते हैं या ग्राहक सहायता के माध्यम से खाता सहायता का अनुरोध कर सकते हैं।
        """.trimIndent()
    } else {
        """
        Desi Paisa Privacy Policy

        1. Data Collected:
        We store only the profile details you provide (full name, mobile number, email from Google Sign-In, language preference) and your virtual demo wallet records.

        2. Password Encryption:
        Security passwords are NEVER stored in plain text. They are transformed using PBKDF2WithHmacSHA256 with a unique 16-byte random salt before storage.

        3. User-Specific Isolation:
        All wallet balances, deposit/withdrawal requests, transactions, referrals, and support tickets are strictly isolated under your authenticated user ID via Firestore Security Rules.

        4. Demo Mode Disclosure:
        Desi Paisa is a demonstration application. No real-money deposits, payouts, UPI charges, or bank debits are executed.

        5. Permissions Minimization:
        This app requests only the minimum INTERNET permission required for Firebase authentication and cloud synchronization. No storage, contacts, or location permissions are used.
        """.trimIndent()
    }

    fun termsText(lang: AppLang): String = if (lang == AppLang.HI) {
        """
        देसी पैसा नियम और शर्तें

        1. डेमो और शैक्षिक उपयोग:
        देसी पैसा एक वर्चुअल डेमो वॉलेट और मर्चेंट उत्पाद शोकेस ऐप है। यह कोई निवेश योजना, निश्चित रिटर्न या वास्तविक कमाई की गारंटी नहीं देता है।

        2. डिपॉजिट और निकासी सत्यापन:
        सभी डेमो डिपॉजिट और निकासी अनुरोधों को बैलेंस अपडेट होने से पहले प्रशासनिक (Admin) अनुमोदन की आवश्यकता होती है।

        3. उत्पाद कैटलॉग:
        उत्पाद पृष्ठ पर दिखाए गए सभी आइटम स्पष्ट रूप से डेमो उत्पाद हैं। डेमो ऑर्डर केवल वर्चुअल बैलेंस का उपयोग करते हैं।

        4. खाता सुरक्षा:
        अपना सुरक्षा पासवर्ड गोपनीय रखें। डुप्लिकेट लेन-देन से बचाव के लिए प्रत्येक अनुरोध की अद्वितीय कुंजी से जांच की जाती है।

        5. सेवा में परिवर्तन:
        डेमो नियम और सुविधाएं गुणवत्ता सुधार के लिए बिना पूर्व सूचना के अपडेट की जा सकती हैं।
        """.trimIndent()
    } else {
        """
        Desi Paisa Terms & Conditions

        1. Strict Demo Mode Policy:
        Desi Paisa provides a virtual wallet simulation and demo merchant product showcase. It does NOT offer financial advice, investment returns, or guaranteed income claims.

        2. Admin Verification Workflow:
        All demo deposit requests require Admin Verification prior to wallet credit, and all demo withdrawal requests require Admin Approval while locking pending funds from Available Balance.

        3. Demo Products:
        All items listed on the Product page are clearly marked as Demo Products. Virtual orders use demo wallet credits only and do not ship physical hardware.

        4. Security & Integrity:
        Users must provide accurate inputs and keep their security password confidential. Duplicate transaction submissions are automatically blocked via idempotency keys.

        5. Support:
        For questions or problem reports, please use the in-app Customer Support section.
        """.trimIndent()
    }
}
