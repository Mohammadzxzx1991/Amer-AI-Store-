package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.interaction.MutableInteractionSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.ui.agent.AnimatedTalkingRobotAssistant
import com.example.ui.agent.InteractiveRobotRegistration
import com.example.ui.agent.AuraRobotOnboardingSequence

data class CountryDial(val flag: String, val code: String, val nameEn: String, val nameAr: String)

val worldCountries = listOf(
    CountryDial("🇯🇴", "+962", "Jordan", "الأردن"),
    CountryDial("🇸🇦", "+966", "Saudi Arabia", "السعودية"),
    CountryDial("🇦🇪", "+971", "UAE", "الإمارات"),
    CountryDial("🇪🇬", "+20", "Egypt", "مصر"),
    CountryDial("🇵🇸", "+970", "Palestine", "فلسطين"),
    CountryDial("🇸🇾", "+963", "Syria", "سوريا"),
    CountryDial("🇮🇶", "+964", "Iraq", "العراق"),
    CountryDial("🇱🇧", "+961", "Lebanon", "لبنان"),
    CountryDial("🇰🇼", "+965", "Kuwait", "الكويت"),
    CountryDial("🇶🇦", "+974", "Qatar", "قطر"),
    CountryDial("🇧🇭", "+973", "Bahrain", "البحرين"),
    CountryDial("🇴🇲", "+968", "Oman", "عمان"),
    CountryDial("🇾🇪", "+967", "Yemen", "اليمن"),
    CountryDial("🇲🇦", "+212", "Morocco", "المغرب"),
    CountryDial("🇩🇿", "+213", "Algeria", "الجزائر"),
    CountryDial("🇹🇳", "+216", "Tunisia", "تونس"),
    CountryDial("🇱🇾", "+218", "Libya", "ليبيا"),
    CountryDial("🇸🇩", "+249", "Sudan", "السودان"),
    CountryDial("🇺🇸", "+1", "USA", "أمريكا"),
    CountryDial("🇬🇧", "+44", "UK", "بريطانيا"),
    CountryDial("🇩🇪", "+49", "Germany", "ألمانيا"),
    CountryDial("🇫🇷", "+33", "France", "فرنسا"),
    CountryDial("🇮🇹", "+39", "Italy", "إيطاليا"),
    CountryDial("🇪🇸", "+34", "Spain", "إسبانيا"),
    CountryDial("🇹🇷", "+90", "Turkey", "تركيا"),
    CountryDial("🇮🇳", "+91", "India", "الهند"),
    CountryDial("🇵🇰", "+92", "Pakistan", "باكستان"),
    CountryDial("🇧🇩", "+880", "Bangladesh", "بنجلاديش"),
    CountryDial("🇨🇳", "+86", "China", "الصين"),
    CountryDial("🇯🇵", "+81", "Japan", "اليابان"),
    CountryDial("🇰🇷", "+82", "South Korea", "كوريا الجنوبية"),
    CountryDial("🇿🇦", "+27", "South Africa", "جنوب أفريقيا"),
    CountryDial("🇳🇬", "+234", "Nigeria", "نيجيريا"),
    CountryDial("🇧🇷", "+55", "Brazil", "البرازيل"),
    CountryDial("🇦🇷", "+54", "Argentina", "الأرجنتين"),
    CountryDial("🇦🇺", "+61", "Australia", "أستراليا")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(viewModel: MarketViewModel) {
    val SlateDarkBg = MaterialTheme.colorScheme.background
    val CardDarkBg = MaterialTheme.colorScheme.surface
    val PolarLight = MaterialTheme.colorScheme.onBackground

    val step by viewModel.verificationStep.collectAsState()
    val codeSent by viewModel.verificationCodeSent.collectAsState()
    val lang by viewModel.appLanguage.collectAsState()

    // Helper to get localized strings
    fun txt(key: String): String = Localization.get(key, lang)

    var isLoginMode by remember { mutableStateOf(false) } // false = Sign Up, true = Sign In
    var showGoogleAccountPicker by remember { mutableStateOf(false) }
    var showGoogleConsent by remember { mutableStateOf(false) }
    var showAuraOnboardingSequence by rememberSaveable { mutableStateOf(true) }

    // Sign Up Fields
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Customer") }
    var enteredCode by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf(worldCountries[0]) } // Jordan is index 0
    var showCountryDropdown by remember { mutableStateOf(false) }

    // Sign In Fields
    var loginEmailOrPhone by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }

    var errorMsg by remember { mutableStateOf<String?>(null) }

    val roles = listOf("Customer", "Merchant", "Delivery", "Admin")

    val coroutineScope = rememberCoroutineScope()
    var tapCount by remember { mutableStateOf(0) }
    var showPosAdmin by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SlateDarkBg, CardDarkBg, SlateDarkBg)
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                tapCount++
                if (tapCount >= 5) {
                    tapCount = 0
                    coroutineScope.launch {
                        delay(5000L) // 5 seconds after 5 taps
                        showPosAdmin = true
                    }
                }
            }
    ) {
        // Floating Top Header Actions: Aura Robot Tour & Language Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Aura Robot Tour Button
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CardDarkBg.copy(alpha = 0.9f),
                border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.5f)),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showAuraOnboardingSequence = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "Aura Robot Tour",
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (lang == "ar") "جولة أورا 🤖" else "Aura Tour 🤖",
                        color = Color(0xFF00E676),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Temporary Owner Quick Access Button (posadmin)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CardDarkBg.copy(alpha = 0.95f),
                border = BorderStroke(1.5.dp, Color(0xFFFFD700)),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { viewModel.loginDirectAsOwner() }
                    .testTag("posadmin")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "POSAdmin",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (lang == "ar") "دخول المالك (posadmin) 👑" else "Owner posadmin 👑",
                        color = Color(0xFFFFD700),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Language Toggle
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardDarkBg.copy(alpha = 0.85f))
                    .border(1.dp, PrimaryCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .clickable { viewModel.toggleLanguage() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Language Option",
                    tint = PrimaryCyan,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (lang == "ar") "English" else "العربية",
                    color = PolarLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sleek Modern Brand Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_app_brand_logo),
                    contentDescription = "AI Platform Logo",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(42.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = txt("app_title"),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF00E676),
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color(0xFF00E676).copy(alpha = 0.5f),
                                blurRadius = 12f
                            )
                        )
                    )
                    Text(
                        text = txt("app_subtitle"),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = SoftGrayText
                    )
                }
            }

            // Temporary Platform Owner Quick Entry Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CardDarkBg.copy(alpha = 0.95f),
                border = BorderStroke(1.5.dp, Color(0xFFFFD700)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { viewModel.loginDirectAsOwner() }
                    .testTag("posadmin_banner_login")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF78350F).copy(alpha = 0.45f), Color(0xFFB45309).copy(alpha = 0.35f), Color(0xFF1E293B))
                            )
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFD700)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (lang == "ar") "دخول مالك المنصة دون تسجيل بيانات (posadmin) 👑" else "Direct Owner Login without Credentials (posadmin) 👑",
                                color = Color(0xFFFFD700),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (lang == "ar") "زر مؤقت يمنحك كامل الصلاحيات والتحكم فوراً" else "Temporary button grants full admin access immediately",
                                color = PolarLight.copy(alpha = 0.8f),
                                fontSize = 9.sp
                            )
                        }
                    }
                    Button(
                        onClick = { viewModel.loginDirectAsOwner() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp).testTag("posadmin_banner_enter")
                    ) {
                        Text(
                            text = if (lang == "ar") "دخول" else "Enter",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = step == 0,
                enter = fadeIn(tween(400)) + slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(400)),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                InteractiveRobotRegistration(viewModel = viewModel)
            }

            AnimatedVisibility(
                visible = step == 1,
                enter = fadeIn(tween(400)) + slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(400))
            ) {
                // Verification Code flow for signup
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SecondaryMint.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = CardDarkBg.copy(alpha = 0.9f)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = txt("shield_sec"),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryMint
                            )

                            Text(
                                text = if (lang == "ar") "تم إنشاء رمز أمان فريد مكون من 6 أرقام بواسطة مصفوفة مالي نيو مارت وإرساله إلى $email / $phone." else "A unique 6-digit access OTP has been compiled by the AI platform security matrix and sent to $email / $phone.",
                                fontSize = 13.sp,
                                color = SoftGrayText,
                                textAlign = TextAlign.Center
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(WarmAmbar.copy(alpha = 0.15f))
                                    .border(1.dp, WarmAmbar.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(vertical = 12.dp, horizontal = 24.dp)
                            ) {
                                Text(
                                    text = txt("secret_msg") + "$codeSent",
                                    color = WarmAmbar,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                            }

                            OutlinedTextField(
                                value = enteredCode,
                                onValueChange = { enteredCode = it },
                                label = { Text(txt("six_digit")) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryCyan) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("otp_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SecondaryMint,
                                    unfocusedBorderColor = SoftGrayText.copy(alpha = 0.4f),
                                    focusedTextColor = PolarLight,
                                    unfocusedTextColor = PolarLight
                                )
                            )

                            if (errorMsg != null) {
                                Text(
                                    text = errorMsg!!,
                                    color = AccentCoral,
                                    fontSize = 12.sp
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.logout() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftGrayText)
                                ) {
                                    Text(txt("back"))
                                }

                                Button(
                                    onClick = {
                                        val ok = viewModel.confirmCode(enteredCode.trim())
                                        if (!ok) {
                                            errorMsg = if (lang == "ar") "رمز غير صالح. يرجى التحقق وإعادة المحاولة." else "Invalid access key. Please verify and retry."
                                        } else {
                                            errorMsg = null
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(2f)
                                        .testTag("verify_confirm_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = txt("confirm_access"),
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Google Simulated Account Picker Dialog
    if (showGoogleAccountPicker) {
        AlertDialog(
            onDismissRequest = { showGoogleAccountPicker = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBox,
                        contentDescription = null,
                        tint = PrimaryCyan,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(txt("select_google_acc"), color = PolarLight, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        txt("google_picker_desc"),
                        fontSize = 11.sp,
                        color = SoftGrayText
                    )

                    // Active Google user selection matching user email
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showGoogleAccountPicker = false
                                showGoogleConsent = true
                            }
                            .border(1.dp, PrimaryCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = SlateDarkBg),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryCyan),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("U", color = SlateDarkBg, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Column {
                                Text("Verified User", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PolarLight)
                                Text("user@example.com", fontSize = 10.sp, color = SoftGrayText)
                            }
                        }
                    }

                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showGoogleAccountPicker = false }) {
                    Text(txt("cancel"), color = AccentCoral)
                }
            },
            containerColor = CardDarkBg
        )
    }

    // Google Simulated Consent Form Dialog
    if (showGoogleConsent) {
        AlertDialog(
            onDismissRequest = { showGoogleConsent = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = SecondaryMint,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(txt("google_consent_title"), color = PolarLight, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        txt("google_consent_desc"),
                        fontSize = 11.sp,
                        color = PolarLight
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        txt("google_consent_bullets"),
                        fontSize = 10.sp,
                        color = SoftGrayText,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        txt("google_consent_note"),
                        fontSize = 9.sp,
                        color = WarmAmbar,
                        lineHeight = 13.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showGoogleConsent = false
                        viewModel.loginWithGoogleSimulated(
                            name = "Verified User",
                            email = "user@example.com",
                            role = role
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan)
                ) {
                    Text(txt("approve_connect"), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoogleConsent = false }) {
                    Text(txt("deny"), color = AccentCoral, fontSize = 11.sp)
                }
            },
            containerColor = CardDarkBg
        )

        // Cinematic Dynamic Path Onboarding with AuraRobot
        if (showAuraOnboardingSequence) {
            AuraRobotOnboardingSequence(
                isAr = lang == "ar",
                onComplete = { showAuraOnboardingSequence = false },
                onStartRegistration = { showAuraOnboardingSequence = false }
            )
        }

        // Posadmin Direct Owner Login Overlay (activated after 5 taps and 5 seconds delay)
        if (showPosAdmin) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CardDarkBg,
                    border = BorderStroke(2.dp, Color(0xFF00E676)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (lang == "ar") "وضع المالك المؤقت (PosAdmin)" else "Temporary Owner Mode (PosAdmin)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (lang == "ar") "تم تفعيل الزر بعد 5 ضغطات ومرور 5 ثواني. انقر أدناه للدخول المباشر بصلاحيات كاملة." else "Button activated after 5 taps and 5 seconds. Tap below for direct full admin login.",
                            color = PolarLight.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = {
                                viewModel.loginDirectAsOwner()
                            },
                            modifier = Modifier
                                .testTag("posadmin")
                                .height(56.dp)
                                .fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                        ) {
                            Text(
                                text = "posadmin",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        TextButton(onClick = { showPosAdmin = false }) {
                            Text(if (lang == "ar") "إلغاء" else "Cancel", color = PolarLight.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }
    }
}
