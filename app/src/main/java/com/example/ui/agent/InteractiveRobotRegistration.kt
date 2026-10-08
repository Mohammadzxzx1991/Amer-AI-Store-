package com.example.ui.agent

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CountryDial
import com.example.ui.MarketViewModel
import com.example.ui.worldCountries
import com.example.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Interactive Robot-Guided Registration Experience
 * The Robot dynamically moves across screen coordinates (top, left, center, right)
 * guiding the user step-by-step through the account creation and authentication journey
 * with rich personality, animations, speech bubbles, and reactive feedback.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveRobotRegistration(
    viewModel: MarketViewModel,
    onDirectLoginToggle: (Boolean) -> Unit = {}
) {
    val lang by viewModel.appLanguage.collectAsState()
    val isAr = lang == "ar"
    val step by viewModel.verificationStep.collectAsState()
    val codeSent by viewModel.verificationCodeSent.collectAsState()
    val authError by viewModel.authError.collectAsState()

    var regSubStep by remember { mutableIntStateOf(0) } // 0: Name, 1: Role, 2: Contact, 3: Verify
    var isQuickLoginMode by remember { mutableStateOf(false) }

    // Form inputs
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Customer") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var enteredCode by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf(worldCountries[0]) }
    var showCountryDropdown by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Quick Login inputs
    var loginEmailOrPhone by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }

    val roles = listOf("Customer", "Merchant", "Delivery", "Admin")

    // Dynamic Robot Movement Coordinates across screen areas based on step
    val robotTargetX by animateDpAsState(
        targetValue = when {
            isQuickLoginMode -> if (isAr) 80.dp else (-80).dp
            regSubStep == 0 -> 0.dp // Center for initial greeting
            regSubStep == 1 -> if (isAr) (-70).dp else 70.dp // Moves to side to showcase roles
            regSubStep == 2 -> if (isAr) 70.dp else (-70).dp // Moves to other side for contact
            else -> 0.dp
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "robot_x"
    )

    // Robot Floating Hover Loop
    val infiniteTransition = rememberInfiniteTransition(label = "robot_hover")
    val hoverY by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hover_y"
    )

    val coreGlowScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "core_glow"
    )

    // Dynamic Speech with Personality
    val robotSpeech = remember(regSubStep, isQuickLoginMode, name, role, isAr) {
        when {
            isQuickLoginMode -> {
                if (isAr) "مرحباً بعودتك! أدخل بيانات حسابك وسأقوم بتسجيل دخولك وتفعيل رادار الأسعار فوراً 🔑"
                else "Welcome back! Enter your login details and I'll activate your Price Radar right away 🔑"
            }
            regSubStep == 0 -> {
                if (name.isBlank()) {
                    if (isAr) "أهلاً بك في متجر أمير للذكاء الاصطناعي! 🤖 أنا رفيقك الآلي الذكي، سأقودك خطوة بخطوة في التسجيل. ما اسمك الكريم؟"
                    else "Welcome to Amer AI Store! 🤖 I am your AI companion robot. I'll guide your registration. What's your name?"
                } else {
                    if (isAr) "يا له من اسم رائع يا $name! اضغط على 'التالي' لنحدد مجالك ودورك بالمنصة ✨"
                    else "Great to meet you, $name! Tap 'Next' to pick your role on the platform ✨"
                }
            }
            regSubStep == 1 -> {
                if (isAr) "يا $name، اختر دورك الوظيفي وسأقوم بتهيئة الأدوات الـ 19 المناسبة لصلاحياتك! (اخترت: $role) 🚀"
                else "$name, select your role and I'll tailor our 19 AI agents for your account! (Selected: $role) 🚀"
            }
            regSubStep == 2 -> {
                if (isAr) "ممتاز! أدخل هاتفك أو بريدك الإلكتروني لنربطه برادار الأسعار التلقائي وسجل الحوكمة الموثق 🛡️"
                else "Awesome! Enter your phone or email to connect Price Radar and governance security 🛡️"
            }
            else -> {
                if (isAr) "أدخل رمز التحقق السري الآن لتفعيل الحساب والانطلاق في عالم التجارة الذكية!"
                else "Enter your verification code now to activate your account and launch into smart commerce!"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. DYNAMIC MOVING & TALKING ROBOT SECTION ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .offset(x = robotTargetX, y = hoverY.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Expressive Aura Robot Avatar with Canvas and Animatable Expressions
                val robotExpr = when {
                    isQuickLoginMode -> RobotExpression.WINK
                    regSubStep == 0 -> if (name.isNotBlank()) RobotExpression.HAPPY else RobotExpression.TALKING
                    regSubStep == 1 -> RobotExpression.EXCITED
                    regSubStep == 2 -> RobotExpression.SCANNING
                    else -> RobotExpression.HAPPY
                }
                val isRobotScanning = regSubStep == 2

                AuraRobot(
                    size = 110.dp,
                    expression = robotExpr,
                    isScanning = isRobotScanning,
                    isTalking = true,
                    auraColor = if (isRobotScanning) Color(0xFFFF5252) else Color(0xFF00E676),
                    secondaryAuraColor = Color(0xFF00B0FF),
                    torsoVisible = true
                )

                // Interactive Speech Bubble with Personality
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1E293B).copy(alpha = 0.95f),
                    border = BorderStroke(1.5.dp, Color(0xFF00E676).copy(alpha = 0.6f)),
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth(0.92f)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E676).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Speaking",
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = robotSpeech,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 18.sp,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // --- 2. STEP PROGRESS INDICATOR (When in Guided Registration) ---
        if (!isQuickLoginMode && step == 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    if (isAr) "1. الاسم" else "1. Name",
                    if (isAr) "2. الدور" else "2. Role",
                    if (isAr) "3. الاتصال" else "3. Contact"
                ).forEachIndexed { index, title ->
                    val isActive = regSubStep == index
                    val isCompleted = regSubStep > index
                    val badgeColor = when {
                        isActive -> Color(0xFF00E676)
                        isCompleted -> Color(0xFF0284C7)
                        else -> Color(0xFF64748B)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(badgeColor),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                            } else {
                                Text("${index + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = if (isActive) Color(0xFF00E676) else Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }

        // --- 3. DYNAMIC REGISTRATION / LOGIN CARD ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Brush.linearGradient(listOf(Color(0xFF00E676).copy(alpha = 0.4f), Color(0xFF0284C7).copy(alpha = 0.4f))), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.90f)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Temporary Platform Owner Direct Access Button (posadmin)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.5.dp, Color(0xFFFFD700)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.loginDirectAsOwner() }
                        .testTag("posadmin")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF78350F).copy(alpha = 0.5f), Color(0xFFB45309).copy(alpha = 0.3f), Color(0xFF1E293B))
                                )
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFD700)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (isAr) "دخول مالك المنصة المباشر (posadmin) 👑" else "Direct Platform Owner Login (posadmin) 👑",
                                    color = Color(0xFFFFD700),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = if (isAr) "زر مؤقت لدخول المالك فوراً دون تسجيل بيانات" else "Temporary button: direct owner login without credentials",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 10.sp
                                )
                            }
                        }
                        Button(
                            onClick = { viewModel.loginDirectAsOwner() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(
                                text = if (isAr) "دخول" else "Enter",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Tab switcher between Guided Robot Registration & Quick Direct Login
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E293B))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp)),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable {
                                isQuickLoginMode = false
                                errorMessage = null
                            }
                            .background(if (!isQuickLoginMode) Color(0xFF00E676) else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isAr) "تسجيل ذكي مع الروبوت 🤖" else "Robot Smart Sign Up 🤖",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isQuickLoginMode) Color.Black else Color.White
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable {
                                isQuickLoginMode = true
                                errorMessage = null
                            }
                            .background(if (isQuickLoginMode) Color(0xFF00E676) else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isAr) "تسجيل الدخول السريع ⚡" else "Quick Sign In ⚡",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isQuickLoginMode) Color.Black else Color.White
                        )
                    }
                }

                // If Error Message Present
                val activeError = errorMessage ?: authError
                if (activeError != null) {
                    Surface(
                        color = Color(0xFFEF4444).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFFEF4444)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = activeError,
                            color = Color(0xFFFCA5A5),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // --- FLOW CONTENT ---
                if (!isQuickLoginMode) {
                    // STEP A: NAME INPUT
                    if (regSubStep == 0) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = if (isAr) "ما هو اسمك الكريم؟" else "What is your full name?",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            OutlinedTextField(
                                value = name,
                                onValueChange = {
                                    name = it
                                    errorMessage = null
                                },
                                label = { Text(if (isAr) "الاسم الكامل" else "Full Name") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF00E676)) },
                                modifier = Modifier.fillMaxWidth().testTag("input_robot_name"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00E676),
                                    unfocusedBorderColor = Color(0xFF475569),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            Button(
                                onClick = {
                                    if (name.trim().isBlank()) {
                                        errorMessage = if (isAr) "يرجى كتابة اسمك لنتمكن من المتابعة." else "Please enter your name to proceed."
                                    } else {
                                        errorMessage = null
                                        regSubStep = 1 // Advance to Role Selection
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_robot_step1_next"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (isAr) "التالي: اختيار الدور بالمنصة 🚀" else "Next: Select Role 🚀",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    // STEP B: ROLE SELECTION
                    if (regSubStep == 1) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = if (isAr) "حدد دورك وصلاحياتك بالمنصة:" else "Select your role on the platform:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            roles.forEach { r ->
                                val isSelected = role == r
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { role = r },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) Color(0xFF00E676).copy(alpha = 0.2f) else Color(0xFF1E293B),
                                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF00E676) else Color(0xFF334155))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = when (r) {
                                                "Customer" -> Icons.Default.ShoppingCart
                                                "Merchant" -> Icons.Default.Storefront
                                                "Delivery" -> Icons.Default.LocalShipping
                                                else -> Icons.Default.AdminPanelSettings
                                            },
                                            contentDescription = null,
                                            tint = if (isSelected) Color(0xFF00E676) else Color(0xFF94A3B8)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = when (r) {
                                                    "Customer" -> if (isAr) "عميل ومشتري (Customer)" else "Customer"
                                                    "Merchant" -> if (isAr) "تاجر وبائع معتمد (Merchant)" else "Merchant"
                                                    "Delivery" -> if (isAr) "مندوب وسائق توصيل (Delivery)" else "Delivery"
                                                    else -> if (isAr) "مشرف وإدارة عامة (Admin)" else "Admin"
                                                },
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color.White
                                            )
                                            Text(
                                                text = when (r) {
                                                    "Customer" -> if (isAr) "تصفح رادار الأسعار، والعروض الحصرية والتسوق الذكي" else "Browse Price Radar, AI recommendations and store"
                                                    "Merchant" -> if (isAr) "إدارة المنتجات، المخزون، وتحليل الأرباح بالذكاء الاصطناعي" else "Manage products, inventory and AI analytics"
                                                    "Delivery" -> if (isAr) "استلام الطلبات، تتبع الخرائط وتوثيق التسليم بالأدلة" else "Accept orders, route maps and delivery proof"
                                                    else -> if (isAr) "صلاحيات الحوكمة والإشراف على الوكلاء الـ 19" else "Governance oversight of 19 AI agents"
                                                },
                                                fontSize = 11.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00E676))
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { regSubStep = 0 },
                                    modifier = Modifier.weight(1f).height(46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFF475569))
                                ) {
                                    Text(if (isAr) "رجوع" else "Back", color = Color.White)
                                }
                                Button(
                                    onClick = { regSubStep = 2 },
                                    modifier = Modifier.weight(2f).height(46.dp).testTag("btn_robot_step2_next"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(if (isAr) "التالي: بيانات الاتصال 📱" else "Next: Contact Info 📱", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // STEP C: CONTACT & INITIAL REGISTRATION
                    if (regSubStep == 2) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = if (isAr) "أدخل بيانات الاتصال لتأمين حسابك:" else "Enter your contact details:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            // Phone with country selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .height(56.dp)
                                        .width(100.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF1E293B))
                                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                                        .clickable { showCountryDropdown = true }
                                        .padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(selectedCountry.flag, fontSize = 18.sp)
                                        Text(selectedCountry.code, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                                    }

                                    DropdownMenu(
                                        expanded = showCountryDropdown,
                                        onDismissRequest = { showCountryDropdown = false },
                                        modifier = Modifier.background(Color(0xFF0F172A)).heightIn(max = 240.dp)
                                    ) {
                                        worldCountries.forEach { c ->
                                            DropdownMenuItem(
                                                text = { Text("${c.flag} ${c.code} (${if (isAr) c.nameAr else c.nameEn})", color = Color.White) },
                                                onClick = {
                                                    selectedCountry = c
                                                    showCountryDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = phone,
                                    onValueChange = { phone = it },
                                    label = { Text(if (isAr) "رقم الهاتف" else "Phone Number") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    modifier = Modifier.weight(1f).testTag("input_robot_phone"),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF00E676),
                                        unfocusedBorderColor = Color(0xFF475569),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )
                            }

                            // Email
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text(if (isAr) "البريد الإلكتروني" else "Email Address") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF00E676)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier.fillMaxWidth().testTag("input_robot_email"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00E676),
                                    unfocusedBorderColor = Color(0xFF475569),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            // Password
                            OutlinedTextField(
                                value = regPassword,
                                onValueChange = { regPassword = it },
                                label = { Text(if (isAr) "كلمة المرور (6 خانات على الأقل)" else "Password (6+ chars)") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF00E676)) },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth().testTag("input_robot_password"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00E676),
                                    unfocusedBorderColor = Color(0xFF475569),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            // Submit Registration Button
                            Button(
                                onClick = {
                                    if (phone.trim().isBlank() && email.trim().isBlank()) {
                                        errorMessage = if (isAr) "يرجى إدخال الهاتف أو البريد الإلكتروني للمتابعة." else "Please enter phone or email to proceed."
                                    } else if (regPassword.length < 6) {
                                        errorMessage = if (isAr) "يجب ألا تقل كلمة المرور عن 6 خانات." else "Password must be at least 6 characters."
                                    } else {
                                        errorMessage = null
                                        viewModel.initiateRegister(name, selectedCountry.code + phone, email, role, regPassword)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_robot_submit_registration"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (isAr) "إرسال رمز التفعيل وبدء الحساب ✨" else "Send Verification Code ✨",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            OutlinedButton(
                                onClick = { regSubStep = 1 },
                                modifier = Modifier.fillMaxWidth().height(42.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFF475569))
                            ) {
                                Text(if (isAr) "رجوع لتعديل الدور" else "Back to edit role", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    // --- QUICK SIGN IN MODE ---
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = if (isAr) "تسجيل الدخول إلى حسابك المسجل:" else "Sign in to your account:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        OutlinedTextField(
                            value = loginEmailOrPhone,
                            onValueChange = { loginEmailOrPhone = it },
                            label = { Text(if (isAr) "البريد الإلكتروني أو الهاتف" else "Email or Phone") },
                            leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color(0xFF00E676)) },
                            modifier = Modifier.fillMaxWidth().testTag("input_quick_login_id"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00E676),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            label = { Text(if (isAr) "كلمة المرور" else "Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF00E676)) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().testTag("input_quick_login_pass"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00E676),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00E676))
                            )
                            Text(
                                text = if (isAr) "تذكرني على هذا الجهاز" else "Remember me",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Button(
                            onClick = {
                                if (loginEmailOrPhone.trim().isBlank()) {
                                    errorMessage = if (isAr) "يرجى إدخال اسم المستخدم أو البريد." else "Please enter your username or email."
                                } else {
                                    errorMessage = null
                                    viewModel.login(loginEmailOrPhone, loginPassword, rememberMe)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_quick_login_submit"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isAr) "تسجيل الدخول الآن 🚀" else "Sign In Now 🚀",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.loginDirectAsOwner() },
                            modifier = Modifier.fillMaxWidth().height(44.dp).testTag("btn_owner_posadmin_signin"),
                            border = BorderStroke(1.5.dp, Color(0xFFFFD700)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (isAr) "دخول فوري كمالك المنصة (posadmin) 👑" else "Instant Platform Owner Login (posadmin) 👑",
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
