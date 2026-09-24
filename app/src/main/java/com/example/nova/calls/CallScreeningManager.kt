package com.example.nova.calls

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ScreenedCall(
    val id: String,
    val phoneNumber: String,
    val callerName: String?,
    val decision: CallDecision,
    val aiScreeningSummary: String,
    val riskScorePercent: Int,
    val timestamp: Long = System.currentTimeMillis()
)

enum class CallDecision {
    ALLOW,
    SCREEN_WITH_AI,
    BLOCK_SPAM,
    SILENCE_UNKNOWN
}

data class ScreeningRule(
    val id: String,
    val title: String,
    val description: String,
    val isEnabled: Boolean
)

class CallScreeningManager {

    private val _screenedCalls = MutableStateFlow<List<ScreenedCall>>(
        listOf(
            ScreenedCall(
                id = "CALL-01",
                phoneNumber = "+1 (555) 302-8819",
                callerName = "Cyber Sec Hotline",
                decision = CallDecision.ALLOW,
                aiScreeningSummary = "Caller identity matched verified contact database.",
                riskScorePercent = 4
            ),
            ScreenedCall(
                id = "CALL-02",
                phoneNumber = "+1 (800) 992-0193",
                callerName = "Unknown Telemarketer",
                decision = CallDecision.BLOCK_SPAM,
                aiScreeningSummary = "Flagged by global VoIP robocall registry: automated warranty scam.",
                riskScorePercent = 98
            ),
            ScreenedCall(
                id = "CALL-03",
                phoneNumber = "+1 (415) 809-2244",
                callerName = "Dr. Elena Vance",
                decision = CallDecision.SCREEN_WITH_AI,
                aiScreeningSummary = "Screened message: 'Calling regarding lab telemetry analysis data'.",
                riskScorePercent = 12
            )
        )
    )
    val screenedCalls: StateFlow<List<ScreenedCall>> = _screenedCalls.asStateFlow()

    private val _screeningRules = MutableStateFlow(
        listOf(
            ScreeningRule("RULE-01", "Robocall Auto-Block", "Automatically reject known robocalls and spoofed carrier numbers", true),
            ScreeningRule("RULE-02", "AI Screener for Unknown Numbers", "Prompt first-time callers to state their purpose before ringing phone", true),
            ScreeningRule("RULE-03", "Silence Suspected Spammers", "Allow call without audio ringtone; save visual transcript", true),
            ScreeningRule("RULE-04", "VIP Bypass Mode", "Directly ring contacts in your cryptographic whitelist", true)
        )
    )
    val screeningRules: StateFlow<List<ScreeningRule>> = _screeningRules.asStateFlow()

    fun toggleRule(ruleId: String) {
        _screeningRules.value = _screeningRules.value.map {
            if (it.id == ruleId) it.copy(isEnabled = !it.isEnabled) else it
        }
    }

    fun simulateIncomingCall(number: String, isKnownSpam: Boolean): ScreenedCall {
        val decision = if (isKnownSpam) {
            CallDecision.BLOCK_SPAM
        } else if (number.startsWith("+1 (555)") || number.contains("Mom") || number.contains("Chief")) {
            CallDecision.ALLOW
        } else {
            CallDecision.SCREEN_WITH_AI
        }

        val risk = if (isKnownSpam) 95 else if (decision == CallDecision.ALLOW) 2 else 28
        val summary = when (decision) {
            CallDecision.BLOCK_SPAM -> "Intercepted by Nova AI: Telemarketing robot pattern recognized. Auto-declined."
            CallDecision.ALLOW -> "Verified priority contact. Passed through without latency."
            CallDecision.SCREEN_WITH_AI -> "AI receptionist asked caller to speak: 'Urgent meeting update for project Nova'."
            CallDecision.SILENCE_UNKNOWN -> "Silenced and redirected to visual voicemail."
        }

        val newCall = ScreenedCall(
            id = "CALL-${System.currentTimeMillis() % 1000}",
            phoneNumber = number,
            callerName = if (isKnownSpam) "Suspected Robocaller" else "Inbound Caller",
            decision = decision,
            aiScreeningSummary = summary,
            riskScorePercent = risk,
            timestamp = System.currentTimeMillis()
        )

        _screenedCalls.value = listOf(newCall) + _screenedCalls.value
        return newCall
    }
}
