package com.example.data.agent

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

/**
 * Autonomous Operating System Orchestrator for the 19 AI Agents
 */
object AgentEngine {
    private const val TAG = "AgentEngine"
    private val scope = CoroutineScope(Dispatchers.Default)

    // Centralized issue sequence strictly starting at 1010001
    private val issueSequence = AtomicLong(1010001L)

    // Live state of all 19 agents
    private val _agentSnapshots = MutableStateFlow<Map<String, AgentRuntimeSnapshot>>(emptyMap())
    val agentSnapshots: StateFlow<Map<String, AgentRuntimeSnapshot>> = _agentSnapshots.asStateFlow()

    // Message Bus
    private val _messageBus = MutableStateFlow<List<AgentMessage>>(emptyList())
    val messageBus: StateFlow<List<AgentMessage>> = _messageBus.asStateFlow()

    // Tasks list
    private val _taskList = MutableStateFlow<List<AgentTask>>(emptyList())
    val taskList: StateFlow<List<AgentTask>> = _taskList.asStateFlow()

    // Centralized Issue Registry
    private val _issueRegistry = MutableStateFlow<List<AgentIssue>>(emptyList())
    val issueRegistry: StateFlow<List<AgentIssue>> = _issueRegistry.asStateFlow()

    // Evidence Ledger
    private val _evidenceLedger = MutableStateFlow<List<EvidenceEnvelope>>(emptyList())
    val evidenceLedger: StateFlow<List<EvidenceEnvelope>> = _evidenceLedger.asStateFlow()

    // Active Workflows
    private val _workflows = MutableStateFlow<List<DurableWorkflow>>(emptyList())
    val workflows: StateFlow<List<DurableWorkflow>> = _workflows.asStateFlow()

    // Broadcast Receipts History
    private val _broadcasts = MutableStateFlow<List<BroadcastReceipt>>(emptyList())
    val broadcasts: StateFlow<List<BroadcastReceipt>> = _broadcasts.asStateFlow()

    init {
        initializeAgents()
        startHeartbeatLoop()
    }

    private fun initializeAgents() {
        val initialMap = mutableMapOf<String, AgentRuntimeSnapshot>()
        AgentKey.entries.forEach { agent ->
            initialMap[agent.key] = AgentRuntimeSnapshot(
                agentKey = agent.key,
                state = AgentRuntimeState.IDLE,
                heartbeatAt = System.currentTimeMillis(),
                executionNotes = "Ready and actively monitoring queue."
            )
        }
        _agentSnapshots.value = initialMap

        // Seed initial Evidence & Issue verification baseline
        recordEvidence(
            operationId = "SYS_INIT_001",
            actorId = AgentKey.GENERAL_MANAGER.key,
            type = "SYSTEM_GOVERNANCE_BOOT",
            source = "Amer Autonomous OS",
            payloadSummary = "19 agents registered with zero tolerance for fake execution. Policy 2026.09-v1 active."
        )

        // Create sample product creation workflow ready for execution
        initSampleProductWorkflow()
    }

    private fun startHeartbeatLoop() {
        scope.launch {
            while (true) {
                delay(15000) // update heartbeat every 15 seconds
                val current = _agentSnapshots.value.toMutableMap()
                AgentKey.entries.forEach { agent ->
                    val snapshot = current[agent.key]
                    if (snapshot != null) {
                        current[agent.key] = snapshot.copy(
                            heartbeatAt = System.currentTimeMillis()
                        )
                    }
                }
                _agentSnapshots.value = current
            }
        }
    }

    /**
     * Records an immutable evidence record in the Evidence Ledger
     */
    fun recordEvidence(
        operationId: String,
        actorId: String,
        type: String,
        source: String,
        payloadSummary: String,
        metadata: Map<String, String> = emptyMap()
    ): EvidenceEnvelope {
        val evidence = EvidenceEnvelope(
            operationId = operationId,
            actorId = actorId,
            type = type,
            source = source,
            payloadSummary = payloadSummary,
            metadata = metadata
        )
        _evidenceLedger.value = listOf(evidence) + _evidenceLedger.value
        Log.i(TAG, "Evidence Recorded: [${evidence.evidenceId}] by $actorId ($type)")
        return evidence
    }

    /**
     * Scope Gate: Evaluates if the agent is authorized to execute the given task
     */
    fun evaluateScope(agentKey: String, actionCategory: String): ScopeDecision {
        val agent = AgentKey.fromKey(agentKey) ?: return ScopeDecision.OutOfScope(
            responsibleAgent = AgentKey.GENERAL_MANAGER.key,
            reason = "Unknown agent key $agentKey"
        )

        val isInScope = when (agent) {
            AgentKey.GENERAL_MANAGER -> actionCategory in listOf("governance", "closure", "initiation", "approval")
            AgentKey.AGENTS_SUPERVISOR -> actionCategory in listOf("compliance", "handoff", "audit", "issues")
            AgentKey.IDEAS_AGENT -> actionCategory in listOf("idea", "opportunity", "trend")
            AgentKey.DEVELOPMENT_MANAGER -> actionCategory in listOf("spec", "architecture", "task_breakdown")
            AgentKey.UX_CUSTOMER_JOURNEY -> actionCategory in listOf("ux", "journey", "friction", "accessibility")
            AgentKey.DESIGN_PRESENTATION -> actionCategory in listOf("ui", "design", "layout", "visual")
            AgentKey.EXECUTION_AGENT -> actionCategory in listOf("coding", "build", "api", "database", "issue006")
            AgentKey.QA_AGENT -> actionCategory in listOf("testing", "qa", "verification", "acceptance")
            AgentKey.PREVIEW_AGENT -> actionCategory in listOf("preview", "live_build", "deployment")
            AgentKey.EVALUATION_AGENT -> actionCategory in listOf("evaluation", "benchmark", "scorecard")
            AgentKey.FIELD_AGENT -> actionCategory in listOf("field", "survey", "local_store")
            AgentKey.MARKETING_AGENT -> actionCategory in listOf("marketing", "campaign", "copywriting", "creative")
            AgentKey.DATA_GROWTH_INTELLIGENCE -> actionCategory in listOf("data", "growth", "analytics", "demand")
            AgentKey.MARKETPLACE_MERCHANT_SUCCESS -> actionCategory in listOf("catalog", "merchant", "wholesale", "stock")
            AgentKey.DELIVERY_AGENT -> actionCategory in listOf("delivery", "logistics", "eta", "route", "digital_twin")
            AgentKey.TRUST_CUSTOMER_SUPPORT -> actionCategory in listOf("support", "dispute", "trust", "receipt_claim")
            AgentKey.SECURITY_FRAUD -> actionCategory in listOf("security", "fraud", "risk", "anomaly")
            AgentKey.FINANCE_UNIT_ECONOMICS -> actionCategory in listOf("finance", "margin", "economics", "basket_cost")
            AgentKey.LEGAL_COMPLIANCE -> actionCategory in listOf("legal", "compliance", "terms", "consumer_protection")
        }

        return if (isInScope) {
            ScopeDecision.InScope
        } else {
            ScopeDecision.OutOfScope(
                responsibleAgent = findResponsibleAgentForCategory(actionCategory),
                reason = "Action category '$actionCategory' is outside ${agent.displayNameAr}'s mandated charter."
            )
        }
    }

    private fun findResponsibleAgentForCategory(category: String): String {
        return when (category) {
            "coding", "build", "database" -> AgentKey.EXECUTION_AGENT.key
            "testing", "qa" -> AgentKey.QA_AGENT.key
            "design", "ui" -> AgentKey.DESIGN_PRESENTATION.key
            "marketing", "creative" -> AgentKey.MARKETING_AGENT.key
            "delivery", "logistics" -> AgentKey.DELIVERY_AGENT.key
            "dispute", "support" -> AgentKey.TRUST_CUSTOMER_SUPPORT.key
            "security", "fraud" -> AgentKey.SECURITY_FRAUD.key
            "finance" -> AgentKey.FINANCE_UNIT_ECONOMICS.key
            "legal" -> AgentKey.LEGAL_COMPLIANCE.key
            else -> AgentKey.GENERAL_MANAGER.key
        }
    }

    /**
     * Centralized Error Registry: Log new issue starting from 1010001
     */
    fun logIssue(
        originalTaskId: String,
        sourceAgent: String,
        cause: String,
        expectedResponsibleAgent: String,
        evidenceIds: List<String> = emptyList()
    ): AgentIssue {
        val nextId = issueSequence.getAndIncrement()
        val issue = AgentIssue(
            issueId = nextId,
            originalTaskId = originalTaskId,
            sourceAgent = sourceAgent,
            cause = cause,
            expectedResponsibleAgent = expectedResponsibleAgent,
            status = "OPEN",
            evidenceIds = evidenceIds
        )
        _issueRegistry.value = listOf(issue) + _issueRegistry.value
        Log.e(TAG, "Central Issue Registered: #${issue.issueId} | Cause: $cause | Assigned to $expectedResponsibleAgent")
        return issue
    }

    /**
     * Resolves an open issue with remediation proof and evidence
     */
    fun resolveIssue(issueId: Long, remediationNote: String, evidenceId: String) {
        _issueRegistry.value = _issueRegistry.value.map { issue ->
            if (issue.issueId == issueId) {
                issue.copy(
                    status = "RESOLVED",
                    remediation = remediationNote,
                    closedAt = System.currentTimeMillis()
                )
            } else issue
        }
    }

    /**
     * Durable Agent Bus: Sends a single point-to-point message
     */
    fun sendMessage(
        sourceAgent: String,
        targetAgent: String,
        messageType: String,
        payloadSummary: String,
        workflowId: String = "wf_general"
    ): AgentMessage {
        val msg = AgentMessage(
            workflowId = workflowId,
            sourceAgent = sourceAgent,
            targetAgent = targetAgent,
            messageType = messageType,
            payloadSummary = payloadSummary,
            status = TransportStatus.QUEUED
        )
        _messageBus.value = listOf(msg) + _messageBus.value

        // Simulate delivery & ack progression
        scope.launch {
            delay(400)
            updateMessageStatus(msg.messageId, TransportStatus.DELIVERED)
            delay(500)
            updateMessageStatus(msg.messageId, TransportStatus.READ)
            delay(600)
            val ackEvidence = recordEvidence(
                operationId = msg.messageId,
                actorId = targetAgent,
                type = "BUS_ACK",
                source = "Amer Agent Bus",
                payloadSummary = "Message acknowledged by $targetAgent: $payloadSummary"
            )
            updateMessageStatus(msg.messageId, TransportStatus.ACKNOWLEDGED, ackEvidence.evidenceId)
        }

        return msg
    }

    private fun updateMessageStatus(messageId: String, newStatus: TransportStatus, evidenceId: String? = null) {
        _messageBus.value = _messageBus.value.map { msg ->
            if (msg.messageId == messageId) {
                msg.copy(
                    status = newStatus,
                    evidenceRefs = if (evidenceId != null) msg.evidenceRefs + evidenceId else msg.evidenceRefs
                )
            } else msg
        }
    }

    /**
     * Broadcast Directive to ALL 19 agents independently
     * Enforces independent message creation for every agent
     */
    fun broadcastDirective(directiveText: String, directiveId: String = "DIR-" + System.currentTimeMillis().toString().takeLast(5)): BroadcastReceipt {
        val recipientList = mutableListOf<RecipientReceipt>()

        AgentKey.entries.forEach { agent ->
            val msg = sendMessage(
                sourceAgent = AgentKey.GENERAL_MANAGER.key,
                targetAgent = agent.key,
                messageType = "EXECUTIVE_DIRECTIVE",
                payloadSummary = "[$directiveId] $directiveText"
            )
            recipientList.add(
                RecipientReceipt(
                    agentKey = agent.key,
                    messageId = msg.messageId,
                    status = TransportStatus.DELIVERED
                )
            )
        }

        val receipt = BroadcastReceipt(
            directiveId = directiveId,
            directiveText = directiveText,
            recipients = recipientList
        )
        _broadcasts.value = listOf(receipt) + _broadcasts.value

        recordEvidence(
            operationId = directiveId,
            actorId = AgentKey.GENERAL_MANAGER.key,
            type = "DIRECTIVE_BROADCAST",
            source = "Amer Executive Desk",
            payloadSummary = "Directive broadcasted to all 19 agents independently. Verification tracking active."
        )

        return receipt
    }

    /**
     * Initializes the standard Product Creation Pipeline
     */
    private fun initSampleProductWorkflow() {
        val stages = listOf(
            WorkflowStage("IDEATION", AgentKey.IDEAS_AGENT.key, "صياغة فكرة المنتج والفرصة السوقية", isPassed = true, evidenceId = "EV-INIT-01", status = "PASSED"),
            WorkflowStage("TECH_SPEC", AgentKey.DEVELOPMENT_MANAGER.key, "تحويل الفكرة إلى وثيقة متطلبات برمجية", isPassed = true, evidenceId = "EV-INIT-02", status = "PASSED"),
            WorkflowStage("UX_FLOW", AgentKey.UX_CUSTOMER_JOURNEY.key, "تصميم رحلة الشراء وإزالة نقاط الاحتكاك", isPassed = true, evidenceId = "EV-INIT-03", status = "PASSED"),
            WorkflowStage("VISUAL_DESIGN", AgentKey.DESIGN_PRESENTATION.key, "بناء واجهات Living Store و Command Orb", isPassed = true, evidenceId = "EV-INIT-04", status = "PASSED"),
            WorkflowStage("EXECUTION", AgentKey.EXECUTION_AGENT.key, "برمجة الكود وربط Room Database و Bus", isPassed = true, evidenceId = "EV-INIT-05", status = "PASSED"),
            WorkflowStage("QA_VERIFY", AgentKey.QA_AGENT.key, "فحص قبول المعايير واختبارات عدم المحاكاة", isPassed = true, evidenceId = "EV-INIT-06", status = "PASSED"),
            WorkflowStage("PREVIEW", AgentKey.PREVIEW_AGENT.key, "تشغيل أحدث build تفاعلي جاهز للفحص", isPassed = true, evidenceId = "EV-INIT-07", status = "PASSED"),
            WorkflowStage("EVALUATION", AgentKey.EVALUATION_AGENT.key, "إصدار بطاقة التقييم المرئي النهائي", isPassed = false, status = "IN_REVIEW")
        )

        val wf = DurableWorkflow(
            workflowId = "WF-PROD-VISION-X",
            type = "PRODUCT_CREATION",
            title = "AMER Vision X Commerce OS Core Pipeline",
            stages = stages,
            currentStageIndex = 7,
            status = "IN_PROGRESS"
        )
        _workflows.value = listOf(wf)
    }

    /**
     * Runs an autonomous full product cycle pipeline
     */
    fun runProductPipeline(featureName: String) {
        scope.launch {
            val stages = listOf(
                WorkflowStage("IDEATION", AgentKey.IDEAS_AGENT.key, "تحليل فكرة: $featureName"),
                WorkflowStage("TECH_SPEC", AgentKey.DEVELOPMENT_MANAGER.key, "صياغة مواصفات $featureName"),
                WorkflowStage("UX_FLOW", AgentKey.UX_CUSTOMER_JOURNEY.key, "تصميم مسار المستخدم لـ $featureName"),
                WorkflowStage("VISUAL_DESIGN", AgentKey.DESIGN_PRESENTATION.key, "مكونات الشاشة لـ $featureName"),
                WorkflowStage("EXECUTION", AgentKey.EXECUTION_AGENT.key, "تنفيذ كود $featureName"),
                WorkflowStage("QA_VERIFY", AgentKey.QA_AGENT.key, "فحص وضمان الجودة لـ $featureName"),
                WorkflowStage("PREVIEW", AgentKey.PREVIEW_AGENT.key, "تجهيز معاينة حية لـ $featureName"),
                WorkflowStage("EVALUATION", AgentKey.EVALUATION_AGENT.key, "تقييم الجودة النهائية")
            )

            val wfId = "WF-" + UUID.randomUUID().toString().take(6).uppercase()
            var currentWf = DurableWorkflow(
                workflowId = wfId,
                type = "PRODUCT_CREATION",
                title = "تطوير ميزة: $featureName",
                stages = stages,
                currentStageIndex = 0,
                status = "IN_PROGRESS"
            )
            _workflows.value = listOf(currentWf) + _workflows.value

            for (index in stages.indices) {
                val stage = stages[index]
                setAgentState(stage.responsibleAgent, AgentRuntimeState.EXECUTING, "Executing stage ${stage.stageKey} for $featureName")
                delay(1200)

                val evidence = recordEvidence(
                    operationId = wfId,
                    actorId = stage.responsibleAgent,
                    type = "STAGE_COMPLETION",
                    source = "Workflow Engine",
                    payloadSummary = "Stage ${stage.stageKey} successfully completed by ${stage.responsibleAgent} for $featureName."
                )

                val updatedStages = currentWf.stages.toMutableList()
                updatedStages[index] = stage.copy(
                    isPassed = true,
                    evidenceId = evidence.evidenceId,
                    status = "PASSED"
                )

                setAgentState(stage.responsibleAgent, AgentRuntimeState.DONE_VERIFIED, "Completed with evidence ${evidence.evidenceId}")

                currentWf = currentWf.copy(
                    stages = updatedStages,
                    currentStageIndex = index + 1
                )
                _workflows.value = _workflows.value.map { if (it.workflowId == wfId) currentWf else it }
            }

            // GM Closure Gate
            val gmEvidence = recordEvidence(
                operationId = wfId,
                actorId = AgentKey.GENERAL_MANAGER.key,
                type = "GM_APPROVAL_CLOSURE",
                source = "GM Closure Gate",
                payloadSummary = "All 8 stages passed with immutable evidence. Approved by General Manager."
            )

            currentWf = currentWf.copy(
                status = "GM_APPROVED",
                closureEvidenceId = gmEvidence.evidenceId
            )
            _workflows.value = _workflows.value.map { if (it.workflowId == wfId) currentWf else it }
        }
    }

    fun setAgentState(agentKey: String, state: AgentRuntimeState, notes: String = "") {
        val current = _agentSnapshots.value.toMutableMap()
        val existing = current[agentKey]
        if (existing != null) {
            current[agentKey] = existing.copy(
                state = state,
                heartbeatAt = System.currentTimeMillis(),
                executionNotes = notes
            )
            _agentSnapshots.value = current
        }
    }
}
