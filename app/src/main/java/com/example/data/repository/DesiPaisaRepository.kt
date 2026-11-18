package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.core.InputValidators
import com.example.core.PasswordCrypto
import com.example.core.currentEpochDay
import com.example.data.model.DemoProduct
import com.example.data.model.DepositRequest
import com.example.data.model.ReferralRecord
import com.example.data.model.SupportTicket
import com.example.data.model.UserProfile
import com.example.data.model.WalletTransaction
import com.example.data.model.WithdrawalRequest
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class DesiPaisaRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String =
        auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")

    private fun userDoc(uid: String) = db.collection("users").document(uid)

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val path = "users/$userId"
        userDoc(userId).snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                } else {
                    null
                }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                    handleFirestoreError(exceptionToLog, OperationType.GET, path)
                }
                throw error
            }
            .collect { emit(it) }
    }

    fun observeDeposits(userId: String): Flow<List<DepositRequest>> = flow {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val path = "users/$userId/deposits"
        userDoc(userId).collection("deposits")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(DepositRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                logStreamError(error, userId, OperationType.LIST, path)
                throw error
            }
            .collect { emit(it) }
    }

    fun observeWithdrawals(userId: String): Flow<List<WithdrawalRequest>> = flow {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val path = "users/$userId/withdrawals"
        userDoc(userId).collection("withdrawals")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(WithdrawalRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                logStreamError(error, userId, OperationType.LIST, path)
                throw error
            }
            .collect { emit(it) }
    }

    fun observeTransactions(userId: String): Flow<List<WalletTransaction>> = flow {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val path = "users/$userId/transactions"
        userDoc(userId).collection("transactions")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(WalletTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                logStreamError(error, userId, OperationType.LIST, path)
                throw error
            }
            .collect { emit(it) }
    }

    fun observeReferrals(userId: String): Flow<List<ReferralRecord>> = flow {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val path = "users/$userId/referrals"
        userDoc(userId).collection("referrals")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(ReferralRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                logStreamError(error, userId, OperationType.LIST, path)
                throw error
            }
            .collect { emit(it) }
    }

    fun observeSupportTickets(userId: String): Flow<List<SupportTicket>> = flow {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val path = "users/$userId/support_tickets"
        userDoc(userId).collection("support_tickets")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(SupportTicket::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                logStreamError(error, userId, OperationType.LIST, path)
                throw error
            }
            .collect { emit(it) }
    }

    private fun logStreamError(error: Throwable, userId: String, op: OperationType, path: String) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == userId) {
            handleFirestoreError(exceptionToLog, op, path)
        }
    }

    suspend fun createUserProfile(
        fullName: String,
        mobileNumber: String,
        email: String,
        referredByCode: String,
        securityPassword: String,
        languageCode: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not authenticated"))
        val docPath = "users/$uid"
        return try {
            require(InputValidators.isValidFullName(fullName)) { "Name must be 2–60 characters" }
            require(InputValidators.isValidMobile(mobileNumber)) { "Enter a valid 10-digit mobile number" }
            require(InputValidators.isValidPassword(securityPassword)) { "Password must be at least 6 characters" }
            val cleanRef = referredByCode.trim().uppercase()
            require(InputValidators.isValidReferralCodeOptional(cleanRef)) { "Invalid referral code format" }

            val hashed = PasswordCrypto.hashPassword(securityPassword)
            val generatedCode = "DP" + uid.take(4).uppercase().filter { it.isLetterOrDigit() }.padEnd(2, '7') +
                UUID.randomUUID().toString().replace("-", "").take(4).uppercase()

            val payload = mapOf(
                "userId" to uid,
                "fullName" to fullName.trim(),
                "mobileNumber" to mobileNumber.trim(),
                "email" to email.trim().take(120),
                "referralCode" to generatedCode.take(12),
                "referredByCode" to cleanRef,
                "walletBalance" to 0.0,
                "totalDeposit" to 0.0,
                "totalWithdrawal" to 0.0,
                "dailyStreakCount" to 0L,
                "lastCheckInEpochDay" to 0L,
                "passwordHash" to hashed.hashBase64,
                "passwordSalt" to hashed.saltBase64,
                "languagePref" to if (languageCode == "HI") "HI" else "EN",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            userDoc(uid).set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(fullName: String, mobileNumber: String, languageCode: String): Result<Unit> {
        val uid = runCatching { requireUserId() }.getOrElse { return Result.failure(it) }
        val docPath = "users/$uid"
        return try {
            require(InputValidators.isValidFullName(fullName)) { "Name must be 2–60 characters" }
            require(InputValidators.isValidMobile(mobileNumber)) { "Enter a valid 10-digit mobile number" }
            userDoc(uid).update(
                mapOf(
                    "fullName" to fullName.trim(),
                    "mobileNumber" to mobileNumber.trim(),
                    "languagePref" to if (languageCode == "HI") "HI" else "EN",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateLanguagePreference(profile: UserProfile, languageCode: String): Result<Unit> {
        val uid = runCatching { requireUserId() }.getOrElse { return Result.failure(it) }
        val docPath = "users/$uid"
        return try {
            userDoc(uid).update(
                mapOf(
                    "fullName" to profile.fullName,
                    "mobileNumber" to profile.mobileNumber,
                    "languagePref" to if (languageCode == "HI") "HI" else "EN",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun changeSecurityPassword(currentPassword: String, newPassword: String): Result<Unit> {
        val uid = runCatching { requireUserId() }.getOrElse { return Result.failure(it) }
        val docPath = "users/$uid"
        return try {
            require(InputValidators.isValidPassword(newPassword)) { "New password must be at least 6 characters" }
            val snap = userDoc(uid).get().await()
            val storedHash = snap.getString("passwordHash") ?: ""
            val storedSalt = snap.getString("passwordSalt") ?: ""
            if (!PasswordCrypto.verifyPassword(currentPassword, storedHash, storedSalt)) {
                return Result.failure(IllegalArgumentException("Current password is incorrect"))
            }
            val nextHash = PasswordCrypto.hashPassword(newPassword)
            userDoc(uid).update(
                mapOf(
                    "passwordHash" to nextHash.hashBase64,
                    "passwordSalt" to nextHash.saltBase64,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun claimDailyActivity(): Result<Unit> {
        val uid = runCatching { requireUserId() }.getOrElse { return Result.failure(it) }
        val todayEpoch = currentEpochDay()
        val txId = "daily_${uid.take(8)}_$todayEpoch"
        val docPath = "users/$uid"
        return try {
            db.runTransaction { transaction ->
                val userRef = userDoc(uid)
                val txRef = userRef.collection("transactions").document(txId)
                val userSnap = transaction.get(userRef)
                val existingTx = transaction.get(txRef)
                val lastDay = userSnap.getLong("lastCheckInEpochDay") ?: 0L
                if (lastDay >= todayEpoch || existingTx.exists()) {
                    throw IllegalStateException("Daily activity reward already claimed today")
                }
                val currentBalance = (userSnap.get("walletBalance") as? Number)?.toDouble() ?: 0.0
                val streak = userSnap.getLong("dailyStreakCount") ?: 0L
                val bonus = 25.0

                transaction.update(
                    userRef,
                    mapOf(
                        "walletBalance" to (currentBalance + bonus),
                        "dailyStreakCount" to (streak + 1L),
                        "lastCheckInEpochDay" to todayEpoch,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                transaction.set(
                    txRef,
                    mapOf(
                        "userId" to uid,
                        "idempotencyKey" to txId,
                        "type" to "DAILY_ACTIVITY",
                        "title" to "Daily Check-in Streak #${streak + 1} [DEMO]",
                        "titleHi" to "दैनिक चेक-इन स्ट्रीक #${streak + 1} [डेमो]",
                        "amount" to bonus,
                        "direction" to "CREDIT",
                        "status" to "COMPLETED",
                        "demoMode" to true,
                        "relatedRequestId" to "",
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun submitDemoDepositRequest(
        amount: Double,
        paymentMethod: String,
        referenceNote: String,
        idempotencyKey: String
    ): Result<Unit> {
        val uid = runCatching { requireUserId() }.getOrElse { return Result.failure(it) }
        val cleanKey = idempotencyKey.trim()
        val depPath = "users/$uid/deposits/$cleanKey"
        return try {
            require(amount > 0.0 && amount <= 100_000.0) { "Amount must be between ₹1 and ₹1,00,000" }
            require(paymentMethod in listOf("DEMO_UPI", "DEMO_BANK", "DEMO_CARD")) { "Invalid demo payment method" }
            require(cleanKey.length in 8..128) { "Invalid request key" }

            val txKey = "tx_$cleanKey".take(128)
            db.runTransaction { transaction ->
                val userRef = userDoc(uid)
                val depRef = userRef.collection("deposits").document(cleanKey)
                val txRef = userRef.collection("transactions").document(txKey)

                if (transaction.get(depRef).exists() || transaction.get(txRef).exists()) {
                    throw IllegalStateException("Duplicate deposit request blocked")
                }

                transaction.set(
                    depRef,
                    mapOf(
                        "userId" to uid,
                        "idempotencyKey" to cleanKey,
                        "amount" to amount,
                        "paymentMethod" to paymentMethod,
                        "referenceNote" to referenceNote.trim().take(120),
                        "status" to "PENDING",
                        "demoMode" to true,
                        "adminRemark" to "",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                transaction.set(
                    txRef,
                    mapOf(
                        "userId" to uid,
                        "idempotencyKey" to txKey,
                        "type" to "DEMO_DEPOSIT",
                        "title" to "Demo Deposit Request (${paymentMethod.replace("DEMO_", "")}) [DEMO]",
                        "titleHi" to "डेमो डिपॉजिट अनुरोध (${paymentMethod.replace("DEMO_", "")}) [डेमो]",
                        "amount" to amount,
                        "direction" to "CREDIT",
                        "status" to "PENDING",
                        "demoMode" to true,
                        "relatedRequestId" to cleanKey,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, depPath)
            Result.failure(e)
        }
    }

    suspend fun reviewDemoDeposit(
        depositId: String,
        approve: Boolean,
        adminRemark: String
    ): Result<Unit> {
        val uid = runCatching { requireUserId() }.getOrElse { return Result.failure(it) }
        val depPath = "users/$uid/deposits/$depositId"
        return try {
            val nextStatus = if (approve) "APPROVED" else "REJECTED"
            val txKey = "tx_$depositId".take(128)

            db.runTransaction { transaction ->
                val userRef = userDoc(uid)
                val depRef = userRef.collection("deposits").document(depositId)
                val txRef = userRef.collection("transactions").document(txKey)

                val depSnap = transaction.get(depRef)
                val userSnap = transaction.get(userRef)
                val txSnap = transaction.get(txRef)

                if (!depSnap.exists()) throw IllegalStateException("Deposit request not found")
                val currentStatus = depSnap.getString("status") ?: ""
                if (currentStatus != "PENDING") {
                    throw IllegalStateException("Deposit request already processed ($currentStatus)")
                }
                val amount = (depSnap.get("amount") as? Number)?.toDouble() ?: 0.0
                if (amount <= 0.0) throw IllegalStateException("Invalid deposit amount")

                transaction.update(
                    depRef,
                    mapOf(
                        "status" to nextStatus,
                        "adminRemark" to adminRemark.trim().take(200),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                if (txSnap.exists() && txSnap.getString("status") == "PENDING") {
                    transaction.update(txRef, mapOf("status" to nextStatus))
                }
                if (approve) {
                    val walletBalance = (userSnap.get("walletBalance") as? Number)?.toDouble() ?: 0.0
                    val totalDeposit = (userSnap.get("totalDeposit") as? Number)?.toDouble() ?: 0.0
                    transaction.update(
                        userRef,
                        mapOf(
                            "walletBalance" to (walletBalance + amount),
                            "totalDeposit" to (totalDeposit + amount),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, depPath)
            Result.failure(e)
        }
    }

    suspend fun submitDemoWithdrawalRequest(
        amount: Double,
        payoutMethod: String,
        payoutDestination: String,
        accountHolderName: String,
        securityPassword: String,
        idempotencyKey: String,
        pendingWithdrawalsTotal: Double
    ): Result<Unit> {
        val uid = runCatching { requireUserId() }.getOrElse { return Result.failure(it) }
        val cleanKey = idempotencyKey.trim()
        val wdPath = "users/$uid/withdrawals/$cleanKey"
        return try {
            require(amount > 0.0 && amount <= 100_000.0) { "Amount must be between ₹1 and ₹1,00,000" }
            require(payoutMethod in listOf("UPI", "BANK_TRANSFER")) { "Invalid payout method" }
            require(accountHolderName.trim().length in 2..60) { "Enter valid account holder name" }
            require(payoutDestination.trim().length in 4..100) { "Enter valid payout details" }

            val userSnapPre = userDoc(uid).get().await()
            val storedHash = userSnapPre.getString("passwordHash") ?: ""
            val storedSalt = userSnapPre.getString("passwordSalt") ?: ""
            if (!PasswordCrypto.verifyPassword(securityPassword, storedHash, storedSalt)) {
                return Result.failure(IllegalArgumentException("Incorrect security password"))
            }

            val txKey = "tx_$cleanKey".take(128)
            val maskedDestination = InputValidators.maskPayoutDestination(payoutMethod, payoutDestination)

            db.runTransaction { transaction ->
                val userRef = userDoc(uid)
                val wdRef = userRef.collection("withdrawals").document(cleanKey)
                val txRef = userRef.collection("transactions").document(txKey)

                if (transaction.get(wdRef).exists() || transaction.get(txRef).exists()) {
                    throw IllegalStateException("Duplicate withdrawal request blocked")
                }

                val userSnap = transaction.get(userRef)
                val walletBalance = (userSnap.get("walletBalance") as? Number)?.toDouble() ?: 0.0
                val available = (walletBalance - pendingWithdrawalsTotal).coerceAtLeast(0.0)
                if (amount > available) {
                    throw IllegalStateException("Insufficient available balance")
                }

                transaction.set(
                    wdRef,
                    mapOf(
                        "userId" to uid,
                        "idempotencyKey" to cleanKey,
                        "amount" to amount,
                        "payoutMethod" to payoutMethod,
                        "payoutDestination" to maskedDestination,
                        "accountHolderName" to accountHolderName.trim().take(60),
                        "status" to "PENDING",
                        "demoMode" to true,
                        "adminRemark" to "",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                transaction.set(
                    txRef,
                    mapOf(
                        "userId" to uid,
                        "idempotencyKey" to txKey,
                        "type" to "DEMO_WITHDRAWAL",
                        "title" to "Demo Withdrawal ($payoutMethod) [DEMO]",
                        "titleHi" to "डेमो निकासी ($payoutMethod) [डेमो]",
                        "amount" to amount,
                        "direction" to "DEBIT",
                        "status" to "PENDING",
                        "demoMode" to true,
                        "relatedRequestId" to cleanKey,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, wdPath)
            Result.failure(e)
        }
    }

    suspend fun reviewDemoWithdrawal(
        withdrawalId: String,
        approve: Boolean,
        adminRemark: String
    ): Result<Unit> {
        val uid = runCatching { requireUserId() }.getOrElse { return Result.failure(it) }
        val wdPath = "users/$uid/withdrawals/$withdrawalId"
        return try {
            val nextStatus = if (approve) "APPROVED" else "REJECTED"
            val txKey = "tx_$withdrawalId".take(128)

            db.runTransaction { transaction ->
                val userRef = userDoc(uid)
                val wdRef = userRef.collection("withdrawals").document(withdrawalId)
                val txRef = userRef.collection("transactions").document(txKey)

                val wdSnap = transaction.get(wdRef)
                val userSnap = transaction.get(userRef)
                val txSnap = transaction.get(txRef)

                if (!wdSnap.exists()) throw IllegalStateException("Withdrawal request not found")
                val currentStatus = wdSnap.getString("status") ?: ""
                if (currentStatus != "PENDING") {
                    throw IllegalStateException("Withdrawal already processed ($currentStatus)")
                }
                val amount = (wdSnap.get("amount") as? Number)?.toDouble() ?: 0.0
                val walletBalance = (userSnap.get("walletBalance") as? Number)?.toDouble() ?: 0.0
                val totalWithdrawal = (userSnap.get("totalWithdrawal") as? Number)?.toDouble() ?: 0.0

                if (approve && walletBalance < amount) {
                    throw IllegalStateException("Wallet balance is lower than withdrawal amount")
                }

                transaction.update(
                    wdRef,
                    mapOf(
                        "status" to nextStatus,
                        "adminRemark" to adminRemark.trim().take(200),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                if (txSnap.exists() && txSnap.getString("status") == "PENDING") {
                    transaction.update(txRef, mapOf("status" to nextStatus))
                }
                if (approve) {
                    transaction.update(
                        userRef,
                        mapOf(
                            "walletBalance" to (walletBalance - amount),
                            "totalWithdrawal" to (totalWithdrawal + amount),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, wdPath)
            Result.failure(e)
        }
    }

    suspend fun placeDemoProductOrder(
        product: DemoProduct,
        securityPassword: String,
        idempotencyKey: String,
        pendingWithdrawalsTotal: Double
    ): Result<Unit> {
        val uid = runCatching { requireUserId() }.getOrElse { return Result.failure(it) }
        val cleanKey = "ord_$idempotencyKey".take(128)
        val txPath = "users/$uid/transactions/$cleanKey"
        return try {
            val userSnapPre = userDoc(uid).get().await()
            val storedHash = userSnapPre.getString("passwordHash") ?: ""
            val storedSalt = userSnapPre.getString("passwordSalt") ?: ""
            if (!PasswordCrypto.verifyPassword(securityPassword, storedHash, storedSalt)) {
                return Result.failure(IllegalArgumentException("Incorrect security password"))
            }

            db.runTransaction { transaction ->
                val userRef = userDoc(uid)
                val txRef = userRef.collection("transactions").document(cleanKey)
                if (transaction.get(txRef).exists()) {
                    throw IllegalStateException("Duplicate demo order blocked")
                }
                val userSnap = transaction.get(userRef)
                val walletBalance = (userSnap.get("walletBalance") as? Number)?.toDouble() ?: 0.0
                val available = (walletBalance - pendingWithdrawalsTotal).coerceAtLeast(0.0)
                if (product.priceDemoInr > available) {
                    throw IllegalStateException("Insufficient available demo balance")
                }
                transaction.update(
                    userRef,
                    mapOf(
                        "walletBalance" to (walletBalance - product.priceDemoInr),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                transaction.set(
                    txRef,
                    mapOf(
                        "userId" to uid,
                        "idempotencyKey" to cleanKey,
                        "type" to "DEMO_PRODUCT_ORDER",
                        "title" to "${product.nameEn} [DEMO ORDER]",
                        "titleHi" to "${product.nameHi} [डेमो ऑर्डर]",
                        "amount" to product.priceDemoInr,
                        "direction" to "DEBIT",
                        "status" to "COMPLETED",
                        "demoMode" to true,
                        "relatedRequestId" to product.id,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, txPath)
            Result.failure(e)
        }
    }

    suspend fun recordDemoReferral(inviteeName: String, inviteeContact: String): Result<Unit> {
        val uid = runCatching { requireUserId() }.getOrElse { return Result.failure(it) }
        val refId = "ref_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val txId = "tx_$refId"
        val docPath = "users/$uid/referrals/$refId"
        return try {
            require(inviteeName.trim().length in 2..60) { "Invitee name must be 2–60 characters" }
            require(inviteeContact.trim().length in 4..30) { "Enter a valid code or phone" }
            val reward = 50L

            db.runTransaction { transaction ->
                val userRef = userDoc(uid)
                val refDoc = userRef.collection("referrals").document(refId)
                val txDoc = userRef.collection("transactions").document(txId)
                val userSnap = transaction.get(userRef)
                val walletBalance = (userSnap.get("walletBalance") as? Number)?.toDouble() ?: 0.0

                transaction.set(
                    refDoc,
                    mapOf(
                        "userId" to uid,
                        "inviteeName" to inviteeName.trim().take(60),
                        "inviteeCodeOrPhone" to inviteeContact.trim().take(30),
                        "status" to "VERIFIED",
                        "rewardPoints" to reward,
                        "demoMode" to true,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                transaction.set(
                    txDoc,
                    mapOf(
                        "userId" to uid,
                        "idempotencyKey" to txId,
                        "type" to "REFERRAL_REWARD",
                        "title" to "Demo Referral Bonus (${inviteeName.trim()}) [DEMO]",
                        "titleHi" to "डेमो रेफरल बोनस (${inviteeName.trim()}) [डेमो]",
                        "amount" to reward.toDouble(),
                        "direction" to "CREDIT",
                        "status" to "COMPLETED",
                        "demoMode" to true,
                        "relatedRequestId" to refId,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                transaction.update(
                    userRef,
                    mapOf(
                        "walletBalance" to (walletBalance + reward.toDouble()),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun submitSupportTicket(
        category: String,
        subject: String,
        message: String
    ): Result<Unit> {
        val uid = runCatching { requireUserId() }.getOrElse { return Result.failure(it) }
        val ticketId = "tkt_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docPath = "users/$uid/support_tickets/$ticketId"
        return try {
            require(category in listOf("GENERAL", "DEPOSIT_ISSUE", "WITHDRAWAL_ISSUE", "BUG_REPORT", "ACCOUNT")) {
                "Invalid support category"
            }
            require(subject.trim().length in 3..100) { "Subject must be 3–100 characters" }
            require(message.trim().length in 5..1000) { "Message must be 5–1000 characters" }

            userDoc(uid).collection("support_tickets").document(ticketId)
                .set(
                    mapOf(
                        "userId" to uid,
                        "category" to category,
                        "subject" to subject.trim(),
                        "message" to message.trim(),
                        "status" to "OPEN",
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }
}
