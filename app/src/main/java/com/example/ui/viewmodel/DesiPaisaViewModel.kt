package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.AppLang
import com.example.core.UiState
import com.example.data.model.DemoProduct
import com.example.data.model.DepositRequest
import com.example.data.model.ReferralRecord
import com.example.data.model.SupportTicket
import com.example.data.model.UserProfile
import com.example.data.model.WalletTransaction
import com.example.data.model.WithdrawalRequest
import com.example.data.repository.DesiPaisaRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DesiPaisaViewModel(
    private val repository: DesiPaisaRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _selectedLang = MutableStateFlow(AppLang.EN)
    val selectedLang: StateFlow<AppLang> = _selectedLang.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _snackbarEvents = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val snackbarEvents: SharedFlow<String> = _snackbarEvents.asSharedFlow()

    val profileState: StateFlow<UiState<UserProfile?>> =
        repository.observeUserProfile(currentUserId)
            .map<UserProfile?, UiState<UserProfile?>> { profile ->
                if (profile != null) {
                    _selectedLang.value = AppLang.fromCode(profile.languagePref)
                }
                UiState.Success(profile)
            }
            .catchFirestoreError("Failed to load profile")
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val depositsState: StateFlow<UiState<List<DepositRequest>>> =
        repository.observeDeposits(currentUserId)
            .map<List<DepositRequest>, UiState<List<DepositRequest>>> { UiState.Success(it) }
            .catchFirestoreError("Failed to load deposits")
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val withdrawalsState: StateFlow<UiState<List<WithdrawalRequest>>> =
        repository.observeWithdrawals(currentUserId)
            .map<List<WithdrawalRequest>, UiState<List<WithdrawalRequest>>> { UiState.Success(it) }
            .catchFirestoreError("Failed to load withdrawals")
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val transactionsState: StateFlow<UiState<List<WalletTransaction>>> =
        repository.observeTransactions(currentUserId)
            .map<List<WalletTransaction>, UiState<List<WalletTransaction>>> { UiState.Success(it) }
            .catchFirestoreError("Failed to load transactions")
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val referralsState: StateFlow<UiState<List<ReferralRecord>>> =
        repository.observeReferrals(currentUserId)
            .map<List<ReferralRecord>, UiState<List<ReferralRecord>>> { UiState.Success(it) }
            .catchFirestoreError("Failed to load referrals")
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val supportTicketsState: StateFlow<UiState<List<SupportTicket>>> =
        repository.observeSupportTickets(currentUserId)
            .map<List<SupportTicket>, UiState<List<SupportTicket>>> { UiState.Success(it) }
            .catchFirestoreError("Failed to load support tickets")
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val pendingWithdrawalsTotal: StateFlow<Double> =
        withdrawalsState.map { state ->
            when (state) {
                is UiState.Success -> state.data.filter { it.status == "PENDING" }.sumOf { it.amount }
                else -> 0.0
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), 0.0)

    val availableBalance: StateFlow<Double> =
        combine(profileState, pendingWithdrawalsTotal) { pState, pending ->
            val total = (pState as? UiState.Success)?.data?.walletBalance ?: 0.0
            (total - pending).coerceAtLeast(0.0)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), 0.0)

    private fun <T> Flow<UiState<T>>.catchFirestoreError(fallbackMessage: String): Flow<UiState<T>> =
        catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, fallbackMessage, rootError)
            emit(UiState.Error(rootError.message ?: fallbackMessage))
        }

    fun toggleLanguage() {
        val next = if (_selectedLang.value == AppLang.EN) AppLang.HI else AppLang.EN
        _selectedLang.value = next
        val profile = (profileState.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            repository.updateLanguagePreference(profile, next.code)
        }
    }

    fun emitMessage(message: String) {
        _snackbarEvents.tryEmit(message)
    }

    fun createProfile(
        fullName: String,
        mobileNumber: String,
        email: String,
        referredByCode: String,
        securityPassword: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        if (_isBusy.value) return
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.createUserProfile(
                fullName = fullName,
                mobileNumber = mobileNumber,
                email = email,
                referredByCode = referredByCode,
                securityPassword = securityPassword,
                languageCode = _selectedLang.value.code
            )
            _isBusy.value = false
            res.onFailure { emitMessage(it.message ?: "Failed to create profile") }
            onResult(res)
        }
    }

    fun updateProfile(fullName: String, mobileNumber: String, onResult: (Result<Unit>) -> Unit) {
        if (_isBusy.value) return
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.updateUserProfile(fullName, mobileNumber, _selectedLang.value.code)
            _isBusy.value = false
            res.onSuccess {
                emitMessage(if (_selectedLang.value == AppLang.HI) "प्रोफ़ाइल अपडेट हो गई" else "Profile updated successfully")
            }.onFailure {
                emitMessage(it.message ?: "Update failed")
            }
            onResult(res)
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, onResult: (Result<Unit>) -> Unit) {
        if (_isBusy.value) return
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.changeSecurityPassword(currentPassword, newPassword)
            _isBusy.value = false
            res.onSuccess {
                emitMessage(if (_selectedLang.value == AppLang.HI) "सुरक्षा पासवर्ड बदल गया" else "Security password updated")
            }.onFailure {
                emitMessage(it.message ?: "Password update failed")
            }
            onResult(res)
        }
    }

    fun claimDailyActivity() {
        if (_isBusy.value) return
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.claimDailyActivity()
            _isBusy.value = false
            res.onSuccess {
                emitMessage(if (_selectedLang.value == AppLang.HI) "+₹25 डेमो दैनिक बोनस जोड़ा गया!" else "+₹25 Demo daily bonus credited!")
            }.onFailure {
                emitMessage(it.message ?: "Already claimed today")
            }
        }
    }

    fun submitDeposit(
        amount: Double,
        paymentMethod: String,
        referenceNote: String,
        idempotencyKey: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        if (_isBusy.value) return
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.submitDemoDepositRequest(amount, paymentMethod, referenceNote, idempotencyKey)
            _isBusy.value = false
            res.onSuccess {
                emitMessage(
                    if (_selectedLang.value == AppLang.HI)
                        "डेमो डिपॉजिट अनुरोध भेजा गया (सत्यापन लंबित)"
                    else
                        "Demo deposit submitted (Pending Admin Verification)"
                )
            }.onFailure {
                emitMessage(it.message ?: "Deposit request failed")
            }
            onResult(res)
        }
    }

    fun reviewDeposit(depositId: String, approve: Boolean, remark: String) {
        if (_isBusy.value) return
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.reviewDemoDeposit(depositId, approve, remark)
            _isBusy.value = false
            res.onSuccess {
                val msg = if (approve) {
                    if (_selectedLang.value == AppLang.HI) "डेमो डिपॉजिट स्वीकृत और बैलेंस अपडेट किया गया" else "Demo deposit approved & balance updated"
                } else {
                    if (_selectedLang.value == AppLang.HI) "डेमो डिपॉजिट अस्वीकृत किया गया" else "Demo deposit rejected"
                }
                emitMessage(msg)
            }.onFailure {
                emitMessage(it.message ?: "Review failed")
            }
        }
    }

    fun submitWithdrawal(
        amount: Double,
        payoutMethod: String,
        payoutDestination: String,
        accountHolderName: String,
        securityPassword: String,
        idempotencyKey: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        if (_isBusy.value) return
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.submitDemoWithdrawalRequest(
                amount = amount,
                payoutMethod = payoutMethod,
                payoutDestination = payoutDestination,
                accountHolderName = accountHolderName,
                securityPassword = securityPassword,
                idempotencyKey = idempotencyKey,
                pendingWithdrawalsTotal = pendingWithdrawalsTotal.value
            )
            _isBusy.value = false
            res.onSuccess {
                emitMessage(
                    if (_selectedLang.value == AppLang.HI)
                        "डेमो निकासी अनुरोध भेजा गया (अनुमोदन लंबित)"
                    else
                        "Demo withdrawal submitted (Pending Admin Approval)"
                )
            }.onFailure {
                emitMessage(it.message ?: "Withdrawal request failed")
            }
            onResult(res)
        }
    }

    fun reviewWithdrawal(withdrawalId: String, approve: Boolean, remark: String) {
        if (_isBusy.value) return
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.reviewDemoWithdrawal(withdrawalId, approve, remark)
            _isBusy.value = false
            res.onSuccess {
                val msg = if (approve) {
                    if (_selectedLang.value == AppLang.HI) "डेमो निकासी स्वीकृत की गई" else "Demo withdrawal approved"
                } else {
                    if (_selectedLang.value == AppLang.HI) "डेमो निकासी अस्वीकृत की गई" else "Demo withdrawal rejected"
                }
                emitMessage(msg)
            }.onFailure {
                emitMessage(it.message ?: "Review failed")
            }
        }
    }

    fun placeDemoOrder(
        product: DemoProduct,
        securityPassword: String,
        idempotencyKey: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        if (_isBusy.value) return
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.placeDemoProductOrder(
                product = product,
                securityPassword = securityPassword,
                idempotencyKey = idempotencyKey,
                pendingWithdrawalsTotal = pendingWithdrawalsTotal.value
            )
            _isBusy.value = false
            res.onSuccess {
                emitMessage(
                    if (_selectedLang.value == AppLang.HI)
                        "डेमो उत्पाद ऑर्डर दर्ज किया गया!"
                    else
                        "Demo product order recorded!"
                )
            }.onFailure {
                emitMessage(it.message ?: "Demo order failed")
            }
            onResult(res)
        }
    }

    fun recordDemoReferral(inviteeName: String, inviteeContact: String, onResult: (Result<Unit>) -> Unit) {
        if (_isBusy.value) return
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.recordDemoReferral(inviteeName, inviteeContact)
            _isBusy.value = false
            res.onSuccess {
                emitMessage(
                    if (_selectedLang.value == AppLang.HI)
                        "डेमो रेफरल जोड़ा गया (+₹50 बोनस)!"
                    else
                        "Demo referral added (+₹50 bonus)!"
                )
            }.onFailure {
                emitMessage(it.message ?: "Failed to add demo referral")
            }
            onResult(res)
        }
    }

    fun submitSupportTicket(
        category: String,
        subject: String,
        message: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        if (_isBusy.value) return
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.submitSupportTicket(category, subject, message)
            _isBusy.value = false
            res.onSuccess {
                emitMessage(
                    if (_selectedLang.value == AppLang.HI)
                        "सहायता टिकट सफलतापूर्वक सबमिट किया गया"
                    else
                        "Support ticket submitted successfully"
                )
            }.onFailure {
                emitMessage(it.message ?: "Failed to submit ticket")
            }
            onResult(res)
        }
    }

    private companion object {
        const val TAG = "DesiPaisaVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
