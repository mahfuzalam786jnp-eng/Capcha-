package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.auth.authStateFlow
import com.example.auth.signOutWithCleanup
import com.example.core.AppLang
import com.example.core.DesiStrings
import com.example.core.UiState
import com.example.data.model.DepositRequest
import com.example.data.model.ReferralRecord
import com.example.data.model.SupportTicket
import com.example.data.model.WalletTransaction
import com.example.data.model.WithdrawalRequest
import com.example.data.repository.DesiPaisaRepository
import com.example.ui.components.DesiLogoMark
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DepositScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InviteScreen
import com.example.ui.screens.LegalScreen
import com.example.ui.screens.ProductsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ProfileSetupScreen
import com.example.ui.screens.SupportScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.screens.WithdrawalScreen
import com.example.ui.viewmodel.DesiPaisaViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

object DesiRoutes {
    const val HOME = "home"
    const val PRODUCTS = "products"
    const val INVITE = "invite"
    const val PROFILE = "profile"
    const val DEPOSIT = "deposit"
    const val WITHDRAWAL = "withdrawal"
    const val TRANSACTIONS = "transactions"
    const val SUPPORT = "support"
    const val PRIVACY = "privacy"
    const val TERMS = "terms"
}

private data class TopTab(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: (AppLang) -> String,
    val testTag: String
)

private val TOP_TABS = listOf(
    TopTab(DesiRoutes.HOME, Icons.Filled.Home, Icons.Outlined.Home, DesiStrings::navHome, "nav_home"),
    TopTab(DesiRoutes.PRODUCTS, Icons.Filled.Storefront, Icons.Outlined.Storefront, DesiStrings::navProducts, "nav_products"),
    TopTab(DesiRoutes.INVITE, Icons.Filled.CardGiftcard, Icons.Outlined.CardGiftcard, DesiStrings::navInvite, "nav_invite"),
    TopTab(DesiRoutes.PROFILE, Icons.Filled.Person, Icons.Outlined.Person, DesiStrings::navProfile, "nav_profile")
)

@Composable
fun DesiPaisaApp(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    var authLang by remember { mutableStateOf(AppLang.EN) }
    val user = currentUser

    if (user == null) {
        AuthScreen(
            lang = authLang,
            onToggleLang = { authLang = if (authLang == AppLang.EN) AppLang.HI else AppLang.EN },
            onAuthSuccess = { }
        )
    } else {
        key(user.uid) {
            AuthenticatedDesiPaisaShell(
                currentUserId = user.uid,
                userDisplayName = user.displayName ?: "",
                userEmail = user.email ?: ""
            )
        }
    }
}

@Composable
private fun AuthenticatedDesiPaisaShell(
    currentUserId: String,
    userDisplayName: String,
    userEmail: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: DesiPaisaViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                DesiPaisaViewModel(DesiPaisaRepository(db), currentUserId)
            }
        }
    )

    val lang by viewModel.selectedLang.collectAsStateWithLifecycle()
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()
    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val depositsState by viewModel.depositsState.collectAsStateWithLifecycle()
    val withdrawalsState by viewModel.withdrawalsState.collectAsStateWithLifecycle()
    val transactionsState by viewModel.transactionsState.collectAsStateWithLifecycle()
    val referralsState by viewModel.referralsState.collectAsStateWithLifecycle()
    val ticketsState by viewModel.supportTicketsState.collectAsStateWithLifecycle()
    val availableBalance by viewModel.availableBalance.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.snackbarEvents.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val performLogout: () -> Unit = {
        signOutWithCleanup(credentialManager = credentialManager, scope = scope)
    }

    when (val pState = profileState) {
        is UiState.Loading -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DesiLogoMark(sizeDp = 56)
                    CircularProgressIndicator()
                    Text(
                        text = DesiStrings.tr(lang, "Loading Desi Paisa wallet...", "देसी पैसा वॉलेट लोड हो रहा है..."),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        is UiState.Error -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = DesiStrings.tr(lang, "Unable to load profile", "प्रोफ़ाइल लोड नहीं हो सकी"),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = pState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = performLogout) {
                        Text(DesiStrings.tr(lang, "Sign Out", "साइन आउट"))
                    }
                }
            }
        }

        is UiState.Success -> {
            val profile = pState.data
            if (profile == null) {
                Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        ProfileSetupScreen(
                            lang = lang,
                            initialName = userDisplayName,
                            email = userEmail,
                            isBusy = isBusy,
                            onToggleLang = viewModel::toggleLanguage,
                            onSubmitProfile = { fullName, mobile, refCode, pass ->
                                viewModel.createProfile(fullName, mobile, userEmail, refCode, pass) {}
                            },
                            onSignOut = performLogout
                        )
                    }
                }
            } else {
                val deposits: List<DepositRequest> = (depositsState as? UiState.Success)?.data ?: emptyList()
                val withdrawals: List<WithdrawalRequest> = (withdrawalsState as? UiState.Success)?.data ?: emptyList()
                val transactions: List<WalletTransaction> = (transactionsState as? UiState.Success)?.data ?: emptyList()
                val referrals: List<ReferralRecord> = (referralsState as? UiState.Success)?.data ?: emptyList()
                val tickets: List<SupportTicket> = (ticketsState as? UiState.Success)?.data ?: emptyList()

                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route ?: DesiRoutes.HOME
                val showTopLevelNav = currentRoute in TOP_TABS.map { it.route }

                val navigateTab: (String) -> Unit = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val useRail = maxWidth >= 640.dp

                    Scaffold(
                        contentWindowInsets = WindowInsets.safeDrawing,
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        bottomBar = {
                            if (showTopLevelNav && !useRail) {
                                NavigationBar {
                                    TOP_TABS.forEach { tab ->
                                        val selected = currentRoute == tab.route
                                        NavigationBarItem(
                                            selected = selected,
                                            onClick = { navigateTab(tab.route) },
                                            icon = {
                                                Icon(
                                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                    contentDescription = tab.label(lang)
                                                )
                                            },
                                            label = { Text(tab.label(lang)) },
                                            modifier = Modifier.testTag(tab.testTag)
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            if (showTopLevelNav && useRail) {
                                NavigationRail(modifier = Modifier.fillMaxHeight()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    DesiLogoMark(sizeDp = 36)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    TOP_TABS.forEach { tab ->
                                        val selected = currentRoute == tab.route
                                        NavigationRailItem(
                                            selected = selected,
                                            onClick = { navigateTab(tab.route) },
                                            icon = {
                                                Icon(
                                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                    contentDescription = tab.label(lang)
                                                )
                                            },
                                            label = { Text(tab.label(lang)) },
                                            modifier = Modifier.testTag(tab.testTag)
                                        )
                                    }
                                }
                            }

                            NavHost(
                                navController = navController,
                                startDestination = DesiRoutes.HOME,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                composable(DesiRoutes.HOME) {
                                    HomeScreen(
                                        profile = profile,
                                        availableBalance = availableBalance,
                                        transactions = transactions,
                                        lang = lang,
                                        isBusy = isBusy,
                                        onToggleLang = viewModel::toggleLanguage,
                                        onClaimDailyActivity = viewModel::claimDailyActivity,
                                        onOpenDeposit = { navController.navigate(DesiRoutes.DEPOSIT) },
                                        onOpenWithdrawal = { navController.navigate(DesiRoutes.WITHDRAWAL) },
                                        onOpenTransactions = { navController.navigate(DesiRoutes.TRANSACTIONS) },
                                        onOpenSupport = { navController.navigate(DesiRoutes.SUPPORT) },
                                        onOpenProducts = { navigateTab(DesiRoutes.PRODUCTS) }
                                    )
                                }

                                composable(DesiRoutes.PRODUCTS) {
                                    ProductsScreen(
                                        availableBalance = availableBalance,
                                        lang = lang,
                                        isBusy = isBusy,
                                        onToggleLang = viewModel::toggleLanguage,
                                        onPlaceDemoOrder = { product, pass, key, onSuccess ->
                                            viewModel.placeDemoOrder(product, pass, key) { res ->
                                                if (res.isSuccess) onSuccess()
                                            }
                                        }
                                    )
                                }

                                composable(DesiRoutes.INVITE) {
                                    InviteScreen(
                                        profile = profile,
                                        referrals = referrals,
                                        lang = lang,
                                        isBusy = isBusy,
                                        onToggleLang = viewModel::toggleLanguage,
                                        onCopiedCode = { msg -> viewModel.emitMessage(msg) },
                                        onAddDemoReferral = { name, contact, onSuccess ->
                                            viewModel.recordDemoReferral(name, contact) { res ->
                                                if (res.isSuccess) onSuccess()
                                            }
                                        }
                                    )
                                }

                                composable(DesiRoutes.PROFILE) {
                                    ProfileScreen(
                                        profile = profile,
                                        lang = lang,
                                        isBusy = isBusy,
                                        onToggleLang = viewModel::toggleLanguage,
                                        onUpdateProfile = { name, mobile, onSuccess ->
                                            viewModel.updateProfile(name, mobile) { res ->
                                                if (res.isSuccess) onSuccess()
                                            }
                                        },
                                        onChangePassword = { curr, next, onSuccess ->
                                            viewModel.changePassword(curr, next) { res ->
                                                if (res.isSuccess) onSuccess()
                                            }
                                        },
                                        onOpenDeposits = { navController.navigate(DesiRoutes.DEPOSIT) },
                                        onOpenWithdrawals = { navController.navigate(DesiRoutes.WITHDRAWAL) },
                                        onOpenTransactions = { navController.navigate(DesiRoutes.TRANSACTIONS) },
                                        onOpenSupport = { navController.navigate(DesiRoutes.SUPPORT) },
                                        onOpenPrivacyPolicy = { navController.navigate(DesiRoutes.PRIVACY) },
                                        onOpenTerms = { navController.navigate(DesiRoutes.TERMS) },
                                        onLogout = performLogout
                                    )
                                }

                                composable(DesiRoutes.DEPOSIT) {
                                    DepositScreen(
                                        deposits = deposits,
                                        lang = lang,
                                        isBusy = isBusy,
                                        onBack = { navController.popBackStack() },
                                        onToggleLang = viewModel::toggleLanguage,
                                        onSubmitDeposit = { amount, method, note, key, onSuccess ->
                                            viewModel.submitDeposit(amount, method, note, key) { res ->
                                                if (res.isSuccess) onSuccess()
                                            }
                                        },
                                        onReviewDeposit = viewModel::reviewDeposit
                                    )
                                }

                                composable(DesiRoutes.WITHDRAWAL) {
                                    WithdrawalScreen(
                                        availableBalance = availableBalance,
                                        withdrawals = withdrawals,
                                        lang = lang,
                                        isBusy = isBusy,
                                        onBack = { navController.popBackStack() },
                                        onToggleLang = viewModel::toggleLanguage,
                                        onSubmitWithdrawal = { amt, method, dest, holder, pass, key, onSuccess ->
                                            viewModel.submitWithdrawal(amt, method, dest, holder, pass, key) { res ->
                                                if (res.isSuccess) onSuccess()
                                            }
                                        },
                                        onReviewWithdrawal = viewModel::reviewWithdrawal
                                    )
                                }

                                composable(DesiRoutes.TRANSACTIONS) {
                                    TransactionsScreen(
                                        transactions = transactions,
                                        lang = lang,
                                        onBack = { navController.popBackStack() },
                                        onToggleLang = viewModel::toggleLanguage
                                    )
                                }

                                composable(DesiRoutes.SUPPORT) {
                                    SupportScreen(
                                        tickets = tickets,
                                        lang = lang,
                                        isBusy = isBusy,
                                        onBack = { navController.popBackStack() },
                                        onToggleLang = viewModel::toggleLanguage,
                                        onNotify = { msg -> viewModel.emitMessage(msg) },
                                        onSubmitTicket = { cat, subj, msg, onSuccess ->
                                            viewModel.submitSupportTicket(cat, subj, msg) { res ->
                                                if (res.isSuccess) onSuccess()
                                            }
                                        }
                                    )
                                }

                                composable(DesiRoutes.PRIVACY) {
                                    LegalScreen(
                                        isPrivacyPolicy = true,
                                        lang = lang,
                                        onBack = { navController.popBackStack() },
                                        onToggleLang = viewModel::toggleLanguage
                                    )
                                }

                                composable(DesiRoutes.TERMS) {
                                    LegalScreen(
                                        isPrivacyPolicy = false,
                                        lang = lang,
                                        onBack = { navController.popBackStack() },
                                        onToggleLang = viewModel::toggleLanguage
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
