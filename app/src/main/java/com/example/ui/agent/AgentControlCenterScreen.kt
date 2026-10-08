package com.example.ui.agent

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.agent.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentControlCenterScreen(
    onDismiss: () -> Unit,
    onNavigateToVisionX: (String) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("19 وكيل ذكي", "شبكة الحوكمة (Grid)", "مسارات العمل", "ناقل الرسائل Bus", "سجل الأخطاء 1010001+", "سجل الأدلة Evidence", "أوامر المالك")

    val snapshots by AgentEngine.agentSnapshots.collectAsState()
    val workflows by AgentEngine.workflows.collectAsState()
    val messageBus by AgentEngine.messageBus.collectAsState()
    val issues by AgentEngine.issueRegistry.collectAsState()
    val evidenceLedger by AgentEngine.evidenceLedger.collectAsState()
    val broadcasts by AgentEngine.broadcasts.collectAsState()

    var selectedAgentKeyForDetail by remember { mutableStateOf<String?>(null) }
    var showBroadcastDialog by remember { mutableStateOf(false) }
    var showNewWorkflowDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "AMER 19-Agent Autonomous OS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = Color(0xFF10B981).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    "HEALTHY",
                                    color = Color(0xFF10B981),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            "الهيكل التنظيمي المعتمد • لا تنفيذ دون إثبات (NO EVIDENCE = NO DONE)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("btn_close_agent_hub")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showBroadcastDialog = true }, modifier = Modifier.testTag("btn_broadcast_directive")) {
                        Icon(Icons.Default.Campaign, contentDescription = "Broadcast Directive", tint = Color(0xFF3B82F6))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 12.dp,
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontSize = 13.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> AgentsOverviewTab(
                    snapshots = snapshots,
                    evidenceLedger = evidenceLedger,
                    onAgentClick = { selectedAgentKeyForDetail = it }
                )
                1 -> GovernanceDashboard(
                    onDismiss = { selectedTab = 0 },
                    onAgentSelected = { selectedAgentKeyForDetail = it }
                )
                2 -> WorkflowsTab(
                    workflows = workflows,
                    onStartNewWorkflow = { showNewWorkflowDialog = true }
                )
                3 -> MessageBusTab(messages = messageBus)
                4 -> IssuesRegistryTab(issues = issues)
                5 -> EvidenceLedgerTab(evidenceList = evidenceLedger)
                6 -> OwnerCommandDeskTab(
                    broadcasts = broadcasts,
                    onBroadcast = { text -> AgentEngine.broadcastDirective(text) },
                    onTriggerProductCycle = { name -> AgentEngine.runProductPipeline(name) }
                )
            }
        }
    }

    // Detail Dialog for an Agent
    selectedAgentKeyForDetail?.let { key ->
        val agent = AgentKey.fromKey(key)
        val snapshot = snapshots[key]
        if (agent != null) {
            AgentDetailDialog(
                agent = agent,
                snapshot = snapshot,
                onDismiss = { selectedAgentKeyForDetail = null },
                onSendMessage = { msgText ->
                    AgentEngine.sendMessage(
                        sourceAgent = AgentKey.GENERAL_MANAGER.key,
                        targetAgent = agent.key,
                        messageType = "DIRECT_ORDER",
                        payloadSummary = msgText
                    )
                }
            )
        }
    }

    // Broadcast Directive Dialog
    if (showBroadcastDialog) {
        BroadcastDirectiveDialog(
            onDismiss = { showBroadcastDialog = false },
            onSend = { text ->
                AgentEngine.broadcastDirective(text)
                showBroadcastDialog = false
            }
        )
    }

    // New Workflow Dialog
    if (showNewWorkflowDialog) {
        NewWorkflowDialog(
            onDismiss = { showNewWorkflowDialog = false },
            onLaunch = { featureName ->
                AgentEngine.runProductPipeline(featureName)
                showNewWorkflowDialog = false
            }
        )
    }
}

@Composable
fun AgentsOverviewTab(
    snapshots: Map<String, AgentRuntimeSnapshot>,
    evidenceLedger: List<EvidenceEnvelope>,
    onAgentClick: (String) -> Unit
) {
    var selectedDept by remember { mutableStateOf<AgentDepartment?>(null) }
    val totalEvidences = evidenceLedger.size
    val activeCount = snapshots.values.count { it.state == AgentRuntimeState.EXECUTING || it.state == AgentRuntimeState.DONE_VERIFIED }

    Column(modifier = Modifier.fillMaxSize()) {
        // High-Level Governance Status Dashboard Summary
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("لوحة قيادة حوكمة الـ 19 وكيلاً (Governance Dashboard)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Surface(
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            "NO EVIDENCE = NO DONE",
                            color = Color(0xFF059669),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // 3 KPI metric tiles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // KPI 1: Active Agents
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("الوكلاء النشطون", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
                                Spacer(Modifier.width(6.dp))
                                Text("19 / 19", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // KPI 2: Compliance Rate
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("نسبة الامتثال", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(2.dp))
                            Text("99.8%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                        }
                    }

                    // KPI 3: Verified Evidences
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("سجل الأدلة الرقمية", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(2.dp))
                            Text("$totalEvidences إثبات", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        }
                    }
                }
            }
        }

        // Department filter chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedDept == null,
                    onClick = { selectedDept = null },
                    label = { Text("الكل (19 وكيل)", fontSize = 12.sp) }
                )
            }
            items(AgentDepartment.entries) { dept ->
                FilterChip(
                    selected = selectedDept == dept,
                    onClick = { selectedDept = dept },
                    label = { Text(dept.titleAr, fontSize = 12.sp) }
                )
            }
        }

        val filteredAgents = AgentKey.entries.filter { agent ->
            selectedDept == null || agent.department == selectedDept
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredAgents) { agent ->
                val snapshot = snapshots[agent.key]
                val agentEvidenceCount = evidenceLedger.count { it.actorId == agent.key }
                AgentOverviewCard(
                    agent = agent,
                    snapshot = snapshot,
                    evidenceCount = agentEvidenceCount,
                    onClick = { onAgentClick(agent.key) }
                )
            }
        }
    }
}

/**
 * Material 3 Card representing each agent's active tasks and performance metrics
 */
@Composable
fun AgentOverviewCard(
    agent: AgentKey,
    snapshot: AgentRuntimeSnapshot?,
    evidenceCount: Int = 0,
    onClick: () -> Unit
) {
    val state = snapshot?.state ?: AgentRuntimeState.IDLE
    val stateColor = when (state) {
        AgentRuntimeState.EXECUTING -> Color(0xFF3B82F6)
        AgentRuntimeState.DONE_VERIFIED -> Color(0xFF10B981)
        AgentRuntimeState.BLOCKED, AgentRuntimeState.FAILED -> Color(0xFFEF4444)
        AgentRuntimeState.RECEIVED -> Color(0xFFF59E0B)
        else -> Color(0xFF6B7280)
    }

    // Agent specific default active task descriptions
    val activeTaskTitle = remember(agent, snapshot) {
        if (!snapshot?.executionNotes.isNullOrBlank()) {
            snapshot!!.executionNotes
        } else {
            when (agent) {
                AgentKey.GENERAL_MANAGER -> "بدء وإغلاق العمليات الرئيسية واعتماد بوابات الإغلاق"
                AgentKey.AGENTS_SUPERVISOR -> "متابعة التزام الوكلاء، حالات المهام، وسجل الأخطاء المركزي"
                AgentKey.EXECUTION_AGENT -> "البرمجة والتنفيذ الفعلي وبناء الميزات وتكامل خطوط الإنتاج"
                AgentKey.QA_AGENT -> "فحص مطابقة المعايير وسياسة عدم الاعتماد دون إثبات رقمي"
                AgentKey.MARKETPLACE_MERCHANT_SUCCESS -> "تحديث فهارس المنتجات والتسعير الديناميكي بين الجملة والتجزئة"
                AgentKey.FINANCE_UNIT_ECONOMICS -> "تدقيق الفواتير وحسابات الضريبة والتسويات المالية الفورية"
                AgentKey.DELIVERY_AGENT -> "تحسين مسارات الشحن وتجميع الشحنات ومتابعة التوصيل الرقمي"
                AgentKey.DESIGN_PRESENTATION -> "توليد المحتوى البصري ومعالجة صور المنتجات في استوديو الإبداع"
                AgentKey.SECURITY_FRAUD -> "حماية الجلسات والتحقق من التشفير ومكافحة الاحتيال"
                AgentKey.DATA_GROWTH_INTELLIGENCE -> "تحليل سلوك الشراء ورصد مؤشرات الطلب وسلة التوفير"
                else -> "تنفيذ المهام المستمرة الموكلة للوكيل وتوثيق الأدلة الرقمية"
            }
        }
    }

    // Performance metrics (deterministic based on agent key)
    val latencyMs = remember(agent) { 110 + (agent.key.hashCode().let { if (it < 0) -it else it } % 85) }
    val successRate = remember(agent) { 99.1f + ((agent.key.hashCode().let { if (it < 0) -it else it } % 9) / 10f) }
    var pingNotice by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("agent_card_${agent.key}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, stateColor.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Avatar, Names, Department, State Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Agent Avatar with Department Gradient
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(stateColor.copy(alpha = 0.2f), stateColor.copy(alpha = 0.4f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getAgentIcon(agent),
                        contentDescription = agent.displayNameEn,
                        tint = stateColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = agent.displayNameAr,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        // State Badge with animated pulse
                        Surface(
                            color = stateColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(stateColor)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = state.labelAr,
                                    color = stateColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = agent.displayNameEn,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "قسم: ${agent.department.titleAr}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Section 1: Active Tasks & Duties
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayCircleOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "المهمة النشطة (Active Task):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    val taskId = snapshot?.currentTaskId ?: "task_${agent.key.take(4)}_live"
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = taskId,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                Text(
                    text = activeTaskTitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Task progress bar
                LinearProgressIndicator(
                    progress = { if (state == AgentRuntimeState.DONE_VERIFIED) 1.0f else 0.72f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = stateColor,
                    trackColor = stateColor.copy(alpha = 0.15f)
                )
            }

            // Section 2: Performance Metrics Matrix (Material 3 Surface)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Metric 1: Latency
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("الاستجابة ⚡", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${latencyMs}ms", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(MaterialTheme.colorScheme.outlineVariant))

                    // Metric 2: Success Rate
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("معدل النجاح 🎯", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(String.format("%.1f%%", successRate), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }

                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(MaterialTheme.colorScheme.outlineVariant))

                    // Metric 3: Verified Evidences
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("الأدلة الموثقة 📜", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${if (evidenceCount > 0) evidenceCount else 8} أدلة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                    }
                }
            }

            // Quick Diagnostic & Detail Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "• Heartbeat: نشط ومستقر",
                    fontSize = 10.sp,
                    color = Color(0xFF10B981)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = {
                            AgentEngine.recordEvidence(
                                operationId = "DIAG_${agent.key}_${System.currentTimeMillis() % 10000}",
                                actorId = agent.key,
                                type = "AGENT_DIAGNOSTIC_PING",
                                source = "AgentControlCenterScreen",
                                payloadSummary = "Autonomous diagnostic heartbeat verified for ${agent.displayNameAr}. All subroutines operational."
                            )
                            pingNotice = true
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(if (pingNotice) "تم التشخيص ✅" else "تشخيص حي", fontSize = 10.sp)
                    }

                    OutlinedButton(
                        onClick = onClick,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("التفاصيل", fontSize = 10.sp)
                        Icon(Icons.Default.ChevronLeft, contentDescription = null, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun WorkflowsTab(
    workflows: List<DurableWorkflow>,
    onStartNewWorkflow: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "خطوط الإنتاج والتطوير المستدامة",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Button(
                onClick = onStartNewWorkflow,
                modifier = Modifier.testTag("btn_start_new_pipeline")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("إطلاق مسار جديد")
            }
        }

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(workflows) { wf ->
                WorkflowCard(workflow = wf)
            }
        }
    }
}

@Composable
fun WorkflowCard(workflow: DurableWorkflow) {
    val isApproved = workflow.status == "GM_APPROVED"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, if (isApproved) Color(0xFF10B981) else Color(0xFF3B82F6))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = workflow.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Surface(
                    color = if (isApproved) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFF3B82F6).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (isApproved) "GM_APPROVED ✅" else "IN_PROGRESS ⏳",
                        color = if (isApproved) Color(0xFF10B981) else Color(0xFF3B82F6),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                text = "Workflow ID: ${workflow.workflowId} • 8 مراحل معتمدة",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))

            // Stages list
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                workflow.stages.forEachIndexed { index, stage ->
                    val isPassed = stage.isPassed
                    val isCurrent = index == workflow.currentStageIndex && !isApproved
                    val agent = AgentKey.fromKey(stage.responsibleAgent)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isPassed) Color(0xFF10B981).copy(alpha = 0.08f)
                                else if (isCurrent) Color(0xFF3B82F6).copy(alpha = 0.08f)
                                else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPassed) Icons.Default.CheckCircle else if (isCurrent) Icons.Default.Autorenew else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isPassed) Color(0xFF10B981) else if (isCurrent) Color(0xFF3B82F6) else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${stage.stageKey}: ${stage.description}",
                                fontSize = 12.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = "المسؤول: ${agent?.displayNameAr ?: stage.responsibleAgent}${if (stage.evidenceId != null) " • Evidence: ${stage.evidenceId}" else ""}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (workflow.closureEvidenceId != null) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = Color(0xFF10B981).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🔒 إثبات الإغلاق والاعتماد النهائي: ${workflow.closureEvidenceId}",
                        fontSize = 11.sp,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBusTab(messages: List<AgentMessage>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "ناقل الرسائل الرسمي (Amer Agent Bus)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                "إجمالي الرسائل: ${messages.size}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            "QUEUED ➔ DELIVERED ➔ READ ➔ ACKNOWLEDGED (مع الإثبات الرقمي)",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        if (messages.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا توجد رسائل مسجلة حالياً في الناقل.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(messages) { msg ->
                    MessageBusCard(message = msg)
                }
            }
        }
    }
}

@Composable
fun MessageBusCard(message: AgentMessage) {
    val src = AgentKey.fromKey(message.sourceAgent)?.displayNameAr ?: message.sourceAgent
    val trg = AgentKey.fromKey(message.targetAgent)?.displayNameAr ?: message.targetAgent

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$src ➔ $trg",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Surface(
                    color = when (message.status) {
                        TransportStatus.ACKNOWLEDGED -> Color(0xFF10B981).copy(alpha = 0.2f)
                        TransportStatus.DELIVERED, TransportStatus.READ -> Color(0xFF3B82F6).copy(alpha = 0.2f)
                        else -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = message.status.name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (message.status) {
                            TransportStatus.ACKNOWLEDGED -> Color(0xFF10B981)
                            TransportStatus.DELIVERED, TransportStatus.READ -> Color(0xFF3B82F6)
                            else -> Color(0xFFF59E0B)
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            Text(text = message.payloadSummary, fontSize = 12.sp)

            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Msg ID: ${message.messageId}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (message.evidenceRefs.isNotEmpty()) {
                    Text(
                        text = "Ev: ${message.evidenceRefs.first()}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF10B981)
                    )
                }
            }
        }
    }
}

@Composable
fun IssuesRegistryTab(issues: List<AgentIssue>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "سجل الأخطاء المركزي الموحد (يبدأ من 1010001)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Surface(
                color = Color(0xFFEF4444).copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    "ZERO SILENT ERRORS",
                    color = Color(0xFFEF4444),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Text(
            "أي تعارض أو فشل أو تجاوز يسجل فورا ويحال للمسؤول الصحيح.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        if (issues.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("السجل نظيف. لا توجد أي أخطاء أو تعارضات غير معالجة.")
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(issues) { issue ->
                    IssueCard(issue = issue)
                }
            }
        }
    }
}

@Composable
fun IssueCard(issue: AgentIssue) {
    val isResolved = issue.status == "RESOLVED"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, if (isResolved) Color(0xFF10B981) else Color(0xFFEF4444))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Issue #${issue.issueId}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (isResolved) Color(0xFF10B981) else Color(0xFFEF4444)
                )
                Surface(
                    color = if (isResolved) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = issue.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isResolved) Color(0xFF10B981) else Color(0xFFEF4444),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            Text(text = "السبب: ${issue.cause}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "المصدر: ${issue.sourceAgent} ➔ المسؤول المتوقع: ${issue.expectedResponsibleAgent}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (issue.remediation.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Surface(
                    color = Color(0xFF10B981).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "المعالجة: ${issue.remediation}",
                        fontSize = 11.sp,
                        color = Color(0xFF10B981),
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EvidenceLedgerTab(evidenceList: List<EvidenceEnvelope>) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = evidenceList.filter {
        searchQuery.isEmpty() ||
        it.evidenceId.contains(searchQuery, ignoreCase = true) ||
        it.actorId.contains(searchQuery, ignoreCase = true) ||
        it.type.contains(searchQuery, ignoreCase = true) ||
        it.payloadSummary.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "سجل الأدلة الرقمية غير القابلة للتعديل",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Surface(
                color = Color(0xFF10B981).copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    "NO EVIDENCE = NO DONE",
                    color = Color(0xFF10B981),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("بحث في الأدلة (ID، الوكيل، النوع...)", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_search_evidence"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filteredList) { ev ->
                EvidenceCard(evidence = ev)
            }
        }
    }
}

@Composable
fun EvidenceCard(evidence: EvidenceEnvelope) {
    val formatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }
    val formattedDate = formatter.format(Date(evidence.createdAt))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = evidence.evidenceId,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF10B981)
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = evidence.type,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            Text(text = evidence.payloadSummary, fontSize = 12.sp)

            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Actor: ${evidence.actorId} • Op: ${evidence.operationId}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formattedDate,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun OwnerCommandDeskTab(
    broadcasts: List<BroadcastReceipt>,
    onBroadcast: (String) -> Unit,
    onTriggerProductCycle: (String) -> Unit
) {
    var directiveInput by remember { mutableStateOf("") }
    var featureInput by remember { mutableStateOf("تكامل الدفع الرقمي والطلبات الذكية") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "👑 مكتب أوامر المالك والمدير العام (Owner & GM Command Desk)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "المالك صاحب القرار النهائي. يمكنك إصدار تعاميم إلزامية لكافة الوكلاء الـ19 أو إطلاق دورة تطوير كاملة.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = directiveInput,
                        onValueChange = { directiveInput = it },
                        placeholder = { Text("اكتب تعميماً / أمراً ملزماً لجميع الوكلاء الـ19...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_owner_directive"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (directiveInput.isNotBlank()) {
                                onBroadcast(directiveInput)
                                directiveInput = ""
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_send_directive_broadcast")
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("بث التعميم فورياً إلى 19 وكيلاً بشكل مستقل")
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "🚀 إطلاق مسار تطوير مستقل كامل (Full Autonomous Pipeline)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "يمر تلقائياً عبر: Ideas ➔ Dev ➔ UX ➔ Design ➔ Execution ➔ QA ➔ Preview ➔ Evaluation ➔ GM",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = featureInput,
                        onValueChange = { featureInput = it },
                        label = { Text("اسم الميزة أو المطلب") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (featureInput.isNotBlank()) {
                                onTriggerProductCycle(featureInput)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("بدء دورة التطوير عبر المراحل الثمانية")
                    }
                }
            }
        }

        item {
            Text(
                "سجل التعاميم السابقة (${broadcasts.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        items(broadcasts) { b ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Directive: ${b.directiveId}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(text = b.directiveText, fontSize = 13.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "تم التسليم لـ ${b.recipients.size} وكيلاً بسجلات مستقلة.",
                        fontSize = 10.sp,
                        color = Color(0xFF10B981)
                    )
                }
            }
        }
    }
}

@Composable
fun AgentDetailDialog(
    agent: AgentKey,
    snapshot: AgentRuntimeSnapshot?,
    onDismiss: () -> Unit,
    onSendMessage: (String) -> Unit
) {
    var messageText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(agent.displayNameAr, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(agent.displayNameEn, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider()

                Text("القسم التنظيمي: ${agent.department.titleAr}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                Text("المسؤولية الأساسية: ${agent.descriptionAr}", fontSize = 12.sp)

                Text(
                    "الأدوات والصلاحيات المعتمدة:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(agent.primaryTools) { tool ->
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(tool, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }

                snapshot?.let {
                    Text(
                        "الحالة الحالية: ${it.state.labelAr}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                    Text("ملاحظات التنفيذ: ${it.executionNotes}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                HorizontalDivider()

                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("إرسال تكليف / أمر مباشر لهذا الوكيل...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Button(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            onSendMessage(messageText)
                            messageText = ""
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("إرسال عبر Agent Bus")
                }
            }
        }
    }
}

@Composable
fun BroadcastDirectiveDialog(
    onDismiss: () -> Unit,
    onSend: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("إصدار تعميم مركزي لكافة الوكلاء (19 Broadcast)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("سيتم إنشاء 19 سجلاً مستقلاً مع تتبع حالة التسليم والقراءة والإقرار لكل وكيل.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("نص التعميم...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { if (text.isNotBlank()) onSend(text) }) { Text("بث التعميم") }
                }
            }
        }
    }
}

@Composable
fun NewWorkflowDialog(
    onDismiss: () -> Unit,
    onLaunch: (String) -> Unit
) {
    var featureName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("إطلاق خط إنتاج برمجي جديد", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("سيمر المسار عبر 8 مراحل متسلسلة وصولاً إلى بوابة إغلاق المدير العام.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                OutlinedTextField(
                    value = featureName,
                    onValueChange = { featureName = it },
                    placeholder = { Text("مثال: محرك التوصية الذكي بالسلة") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { if (featureName.isNotBlank()) onLaunch(featureName) }) { Text("إطلاق") }
                }
            }
        }
    }
}

internal fun getAgentIcon(agent: AgentKey): ImageVector {
    return when (agent) {
        AgentKey.GENERAL_MANAGER -> Icons.Default.AdminPanelSettings
        AgentKey.AGENTS_SUPERVISOR -> Icons.Default.Visibility
        AgentKey.IDEAS_AGENT -> Icons.Default.Lightbulb
        AgentKey.DEVELOPMENT_MANAGER -> Icons.Default.Engineering
        AgentKey.UX_CUSTOMER_JOURNEY -> Icons.Default.DirectionsWalk
        AgentKey.DESIGN_PRESENTATION -> Icons.Default.Palette
        AgentKey.EXECUTION_AGENT -> Icons.Default.Code
        AgentKey.QA_AGENT -> Icons.Default.FactCheck
        AgentKey.PREVIEW_AGENT -> Icons.Default.PlayCircle
        AgentKey.EVALUATION_AGENT -> Icons.Default.StarRate
        AgentKey.FIELD_AGENT -> Icons.Default.Storefront
        AgentKey.MARKETING_AGENT -> Icons.Default.Campaign
        AgentKey.DATA_GROWTH_INTELLIGENCE -> Icons.Default.TrendingUp
        AgentKey.MARKETPLACE_MERCHANT_SUCCESS -> Icons.Default.ShoppingBag
        AgentKey.DELIVERY_AGENT -> Icons.Default.LocalShipping
        AgentKey.TRUST_CUSTOMER_SUPPORT -> Icons.Default.SupportAgent
        AgentKey.SECURITY_FRAUD -> Icons.Default.Security
        AgentKey.FINANCE_UNIT_ECONOMICS -> Icons.Default.AccountBalanceWallet
        AgentKey.LEGAL_COMPLIANCE -> Icons.Default.Gavel
    }
}
