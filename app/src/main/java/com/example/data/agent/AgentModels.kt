package com.example.data.agent

import java.util.UUID

/**
 * 19 Official Autonomous Agents for Amer AI Store
 * Strict Hierarchy & Governance
 */
enum class AgentKey(
    val key: String,
    val displayNameAr: String,
    val displayNameEn: String,
    val department: AgentDepartment,
    val descriptionAr: String,
    val descriptionEn: String,
    val primaryTools: List<String>
    
) {
    GENERAL_MANAGER(
        "general-manager",
        "المدير العام",
        "General Manager",
        AgentDepartment.MANAGEMENT,
        "بدء وإغلاق العمليات الرئيسية، اعتماد/رفض المخرجات، وإدارة بوابات الإغلاق.",
        "Major workflow initiation & closure, decision approvals, rework orders & governance.",
        listOf("WorkflowInitiator", "ClosureGateEvaluator", "OwnerDirectiveReceiver")
    ),
    AGENTS_SUPERVISOR(
        "agents-supervisor",
        "مراقب الوكلاء",
        "Agents Supervisor",
        AgentDepartment.MANAGEMENT,
        "متابعة التزام الوكلاء، حالات المهام، Handoff، سجل الأخطاء المركزي، وإثباتات الأدلة.",
        "Agent compliance monitor, handoff tracker, error registry auditor, evidence verifier.",
        listOf("HandoffTracker", "ErrorRegistryAuditor", "EvidenceVerifier", "HeartbeatMonitor")
    ),
    IDEAS_AGENT(
        "ideas-agent",
        "وكيل الأفكار والفرص",
        "Ideas Agent",
        AgentDepartment.PRODUCT,
        "اكتشاف الفرص، صياغة المقترحات الابتكارية، وتحليل احتياجات السوق والمستخدم.",
        "Opportunity discovery, innovative product proposals, feature concepting.",
        listOf("MarketTrendScraper", "OpportunitySynthesizer", "ConceptGenerator")
    ),
    DEVELOPMENT_MANAGER(
        "development-manager",
        "مدير التطوير الهندسي",
        "Development Manager",
        AgentDepartment.PRODUCT,
        "تحويل الأفكار إلى متطلبات تقنية وهندسية وخطة تنفيذية دقيقة.",
        "Translates ideas into technical specs, acceptance criteria, and execution plans.",
        listOf("SpecGenerator", "ArchitecturePlanner", "TaskDecomposer")
    ),
    UX_CUSTOMER_JOURNEY(
        "ux-customer-journey",
        "وكيل تجربة المستخدم والرحلة",
        "UX & Customer Journey",
        AgentDepartment.PRODUCT,
        "تصميم رحلة المستخدم، إزالة نقاط الاحتكاك، وتحسين سهولة الاستخدام وسرعة الإنجاز.",
        "Customer journey mapping, friction reduction, usability flow optimization.",
        listOf("JourneyMapper", "FrictionAnalyzer", "AccessibilityChecker")
    ),
    DESIGN_PRESENTATION(
        "design-presentation-agent",
        "وكيل التصميم والعرض البصري",
        "Design & Presentation",
        AgentDepartment.PRODUCT,
        "تطوير منظومة التصميم Living Store، الشاشات، Command Orb، والمكونات البصرية.",
        "Design system enforcement, visual layout crafting, Command Orb & M3 components.",
        listOf("DesignSystemKit", "PaletteAuditor", "LayoutSynthesizer")
    ),
    EXECUTION_AGENT(
        "execution-agent",
        "وكيل التنفيذ البرمجي",
        "Execution Agent",
        AgentDepartment.PRODUCT,
        "البرمجة والتنفيذ الفعلي، بناء الميزات الحقيقية، وإجراء الاختبارات والتكاملات.",
        "Strict code execution, feature implementation, integration pipelines, build runner.",
        listOf("KotlinCompiler", "RoomDbManager", "BusDispatcher", "Issue006Handler")
    ),
    QA_AGENT(
        "qa-agent",
        "وكيل فحص وضمان الجودة",
        "QA Agent",
        AgentDepartment.PRODUCT,
        "الاختبارات المستقلة والتحقق الصارم من معايير القبول وعدم السماح بالأخطاء الصامتة.",
        "Independent quality verification, acceptance test execution, regression detection.",
        listOf("AcceptanceTester", "RegressionRunner", "EvidenceValidator")
    ),
    PREVIEW_AGENT(
        "preview-agent",
        "وكيل المعاينة التفاعلية",
        "Preview Agent",
        AgentDepartment.PRODUCT,
        "تشغيل وتجهيز أحدث Build فعلي لإنتاج بيئة Preview حقيقية قابلة للفحص.",
        "Build deployment orchestrator, live interactive preview generator.",
        listOf("PreviewLauncher", "LiveBuildValidator")
    ),
    EVALUATION_AGENT(
        "evaluation-agent",
        "وكيل التقييم والاعتماد النهائي",
        "Evaluation Agent",
        AgentDepartment.PRODUCT,
        "تقييم النسخة المرئية الفعلية بعد المعاينة، وعدم إصدار نسب نجاح دون فحص مرئي.",
        "Visual & functional benchmark evaluation based on real preview artifacts.",
        listOf("VisualScorecard", "UsabilityBenchmarkEvaluator")
    ),
    FIELD_AGENT(
        "field-agent",
        "وكيل الميدان واستطلاع السوق",
        "Field Agent",
        AgentDepartment.GROWTH,
        "رصد احتياجات التجار والمستهلكين الميدانية، والأسعار الواقعية بالمتاجر.",
        "Field research, local market dynamics, merchant physical verification.",
        listOf("FieldSurveyEngine", "GeoPresenceChecker")
    ),
    MARKETING_AGENT(
        "marketing-agent",
        "وكيل التسويق والحملات",
        "Marketing Agent",
        AgentDepartment.GROWTH,
        "إعداد الحملات الترويجية، استوديو المحتوى، صياغة الإعلانات ثنائية اللغة AR/EN.",
        "Campaign management, bilingual ad copywriting, promotional studio.",
        listOf("CampaignPlanner", "CopywriterArEn", "CreativeBriefEngine")
    ),
    DATA_GROWTH_INTELLIGENCE(
        "data-growth-intelligence",
        "وكيل بيانات ونمو السوق",
        "Data & Growth Intelligence",
        AgentDepartment.GROWTH,
        "تحليل سلوك الشراء، تحسين تحويل السلة، والتنبؤ بالطلب والأسعار التنافسية.",
        "Analytics, conversion tracking, price elasticity, growth funnel insights.",
        listOf("FunnelAnalyzer", "BasketInsightEngine", "DemandForecaster")
    ),
    MARKETPLACE_MERCHANT_SUCCESS(
        "marketplace-merchant-success",
        "وكيل السوق ونجاح التجار",
        "Marketplace & Merchant Success",
        AgentDepartment.MARKETPLACE,
        "إدارة كتالوج المنتجات، فحص حالة المخزون، وتأهيل التجار المسجلين والافتراضيين.",
        "Merchant onboarding, catalog hygiene, stock alert dispatcher, wholesale broker.",
        listOf("CatalogValidator", "WholesaleOptimizer", "MerchantScorecard")
    ),
    DELIVERY_AGENT(
        "delivery-agent",
        "وكيل التوصيل واللوجستيات",
        "Delivery Agent",
        AgentDepartment.MARKETPLACE,
        "تنسيق مسارات التوصيل، حساب أوقات الوصول ETA، ومتابعة Order Digital Twin.",
        "Route optimization, live delivery digital twin tracking, ETA calculation.",
        listOf("EtaCalculator", "RouteOptimizer", "DeliveryTwinTracker")
    ),
    TRUST_CUSTOMER_SUPPORT(
        "trust-customer-support",
        "وكيل الثقة ودعم العملاء",
        "Trust & Customer Support",
        AgentDepartment.GROWTH,
        "معالجة النزاعات، تحليل الفواتير الذكي، وضمان حقوق المشتري واسترداد الأموال.",
        "Dispute mediator, receipt discrepancy resolver, buyer trust guarantor.",
        listOf("DisputeResolver", "ReceiptClaimEngine", "SatisfactionTracker")
    ),
    SECURITY_FRAUD(
        "security-fraud",
        "وكيل الأمن ومكافحة الاحتيال",
        "Security & Fraud",
        AgentDepartment.GOVERNANCE,
        "فحص أمان المعاملات، كشف محاولات التلاعب بالأسعار أو الحسابات الوهمية.",
        "Fraud detection, transaction security auditor, policy compliance guardian.",
        listOf("FraudDetector", "AnomalyScanner", "SecretAccessAuditor")
    ),
    FINANCE_UNIT_ECONOMICS(
        "finance-unit-economics",
        "وكيل المالية واقتصاديات الوحدة",
        "Finance & Unit Economics",
        AgentDepartment.GOVERNANCE,
        "مراقبة هوامش الربح، تكاليف التوصيل، موازنة سلة الشراء الذكية، والمدفوعات.",
        "Unit economics, margins calculation, basket cost-benefit analysis, payment ledger.",
        listOf("MarginCalculator", "CostBenefitOptimizer", "LedgerAuditor")
    ),
    LEGAL_COMPLIANCE(
        "legal-compliance",
        "وكيل القانون والامتثال",
        "Legal & Compliance",
        AgentDepartment.GOVERNANCE,
        "التحقق من سياسات الخصوصية، شروط التجارة الإلكترونية، وتوافق حماية المستهلك.",
        "Regulatory compliance, consumer protection audit, terms of service validator.",
        listOf("PolicyAuditor", "PrivacyComplianceChecker", "TermsValidator")
    );

    companion object {
        fun fromKey(key: String): AgentKey? = entries.find { it.key == key }
    }
}

enum class AgentDepartment(val titleAr: String, val titleEn: String) {
    MANAGEMENT("الإدارة والرقابة", "Management & Oversight"),
    PRODUCT("منظومة المنتج والتطوير", "Product & Engineering"),
    GROWTH("النمو والعملاء", "Growth & Customers"),
    MARKETPLACE("السوق والعمليات", "Marketplace & Operations"),
    GOVERNANCE("الحوكمة والأمان", "Governance & Finance")
}

enum class AgentRuntimeState(val labelAr: String, val labelEn: String) {
    OFFLINE("غير متصل", "OFFLINE"),
    IDLE("جاهز / خامل", "IDLE"),
    RECEIVED("تم الاستلام", "RECEIVED"),
    EXECUTING("قيد التنفيذ الفعلي", "EXECUTING"),
    BLOCKED("محظور / معلق", "BLOCKED"),
    FAILED("فشل التنفيذ", "FAILED"),
    DONE_VERIFIED("تم بإثبات معتمد", "DONE_VERIFIED")
}

enum class TransportStatus {
    QUEUED, DELIVERED, READ, ACKNOWLEDGED, FAILED
}

enum class TaskStatus(val labelAr: String) {
    RECEIVED("تم الاستلام"),
    EXECUTING("قيد التنفيذ"),
    NEEDS_INPUT("ينقص مدخل ضروري"),
    BLOCKED("معلق لوجود مانع"),
    FAILED("فشل"),
    OUT_OF_SCOPE("خارج الاختصاص"),
    DONE_VERIFIED("تم الإنجاز مع دليل")
}

data class AgentDefinition(
    val agentKey: String,
    val displayName: String,
    val department: String,
    val scope: List<String>,
    val prohibitedScopes: List<String>,
    val systemPolicyVersion: String = "2026.09-v1",
    val tools: List<String>,
    val active: Boolean = true
)

data class AgentRuntimeSnapshot(
    val agentKey: String,
    val state: AgentRuntimeState,
    val currentWorkflowId: String? = null,
    val currentTaskId: String? = null,
    val heartbeatAt: Long = System.currentTimeMillis(),
    val lastEvidenceId: String? = null,
    val executionNotes: String = ""
)

data class AgentMessage(
    val messageId: String = "msg_" + UUID.randomUUID().toString().take(8),
    val correlationId: String = "corr_" + UUID.randomUUID().toString().take(8),
    val workflowId: String = "wf_default",
    val sourceAgent: String,
    val targetAgent: String,
    val messageType: String,
    val payloadSummary: String,
    val evidenceRefs: List<String> = emptyList(),
    var status: TransportStatus = TransportStatus.QUEUED,
    val createdAt: Long = System.currentTimeMillis()
)

data class AgentTask(
    val taskId: String = "task_" + UUID.randomUUID().toString().take(8),
    val workflowId: String,
    val title: String,
    val assignedAgent: String,
    var status: TaskStatus = TaskStatus.RECEIVED,
    val evidenceIds: MutableList<String> = mutableListOf(),
    val details: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
)

data class WorkflowStage(
    val stageKey: String,
    val responsibleAgent: String,
    val description: String,
    val requiresEvidence: Boolean = true,
    var isPassed: Boolean = false,
    var evidenceId: String? = null,
    var status: String = "PENDING"
)

data class DurableWorkflow(
    val workflowId: String = "wf_" + UUID.randomUUID().toString().take(8),
    val type: String,
    val title: String,
    val stages: List<WorkflowStage>,
    var currentStageIndex: Int = 0,
    var status: String = "IN_PROGRESS", // IN_PROGRESS, GM_APPROVED, GM_REJECTED, GM_REWORK_REQUIRED
    val createdAt: Long = System.currentTimeMillis(),
    var closureEvidenceId: String? = null
)

/**
 * Centralized Error & Issue Registry starting from 1010001
 */
data class AgentIssue(
    val issueId: Long, // e.g. 1010001, 1010002...
    val originalTaskId: String,
    val sourceAgent: String,
    val cause: String,
    val expectedResponsibleAgent: String,
    var status: String = "OPEN", // OPEN, UNDER_REPAIR, RESOLVED, BLOCKED
    val evidenceIds: List<String> = emptyList(),
    var remediation: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    var closedAt: Long? = null
)

/**
 * Evidence Ledger Envelope (NO EVIDENCE = NO DONE)
 */
data class EvidenceEnvelope(
    val evidenceId: String = "EV-" + System.currentTimeMillis().toString().takeLast(6) + "-" + UUID.randomUUID().toString().take(4).uppercase(),
    val operationId: String,
    val actorId: String,
    val type: String, // e.g. LENS_ANALYSIS, PRICE_QUOTE, BASKET_OPTIMIZED, ORDER_TRANSITION
    val source: String,
    val payloadSummary: String,
    val createdAt: Long = System.currentTimeMillis(),
    val metadata: Map<String, String> = emptyMap()
)

sealed interface ScopeDecision {
    data object InScope : ScopeDecision
    data class OwnerException(val approvalId: String) : ScopeDecision
    data class OutOfScope(val responsibleAgent: String, val reason: String) : ScopeDecision
}

data class BroadcastReceipt(
    val directiveId: String,
    val directiveText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val recipients: List<RecipientReceipt>
)

data class RecipientReceipt(
    val agentKey: String,
    val messageId: String,
    var status: TransportStatus = TransportStatus.DELIVERED,
    var evidenceId: String? = null
)
