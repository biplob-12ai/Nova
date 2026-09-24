package com.example.nova.services

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log

class NovaCallScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val handle = callDetails.handle
        val schemeSpecific = handle?.schemeSpecificPart ?: ""
        Log.i("NovaCallScreening", "Inbound call detected: $schemeSpecific")

        val responseBuilder = CallResponse.Builder()

        // Known spam prefixes or spoofing heuristics
        val isSpam = schemeSpecific.startsWith("+1800") || schemeSpecific.startsWith("800")

        if (isSpam) {
            responseBuilder.apply {
                setDisallowCall(true)
                setRejectCall(true)
                setSkipCallLog(false)
                setSkipNotification(true)
            }
            Log.i("NovaCallScreening", "Spam call blocked by Nova: $schemeSpecific")
        } else {
            responseBuilder.apply {
                setDisallowCall(false)
                setRejectCall(false)
                setSkipCallLog(false)
                setSkipNotification(false)
            }
        }

        respondToCall(callDetails, responseBuilder.build())
    }
}
