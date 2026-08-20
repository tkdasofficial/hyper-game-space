package com.example

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.util.PermissionManager

class OnboardingActivity : ComponentActivity() {

    private lateinit var tvUsageStatus: TextView
    private lateinit var tvAccessibilityStatus: TextView
    private lateinit var tvBatteryStatus: TextView
    private lateinit var btnContinue: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (PermissionManager.hasUsageAccess(this) && 
            PermissionManager.hasAccessibilityPermission(this) && 
            PermissionManager.isIgnoringBatteryOptimizations(this)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_onboarding)

        tvUsageStatus = findViewById(R.id.tvUsageStatus)
        tvAccessibilityStatus = findViewById(R.id.tvAccessibilityStatus)
        tvBatteryStatus = findViewById(R.id.tvBatteryStatus)
        btnContinue = findViewById(R.id.btnContinue)

        findViewById<LinearLayout>(R.id.btnUsageAccess).setOnClickListener {
            PermissionManager.requestUsageAccess(this)
        }

        findViewById<LinearLayout>(R.id.btnAccessibility).setOnClickListener {
            PermissionManager.requestAccessibility(this)
        }

        findViewById<LinearLayout>(R.id.btnBattery).setOnClickListener {
            PermissionManager.requestIgnoreBatteryOptimizations(this)
        }

        btnContinue.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
    }

    private fun checkPermissions() {
        val hasUsage = PermissionManager.hasUsageAccess(this)
        val hasAccessibility = PermissionManager.hasAccessibilityPermission(this)
        val hasBattery = PermissionManager.isIgnoringBatteryOptimizations(this)

        updateStatusText(tvUsageStatus, hasUsage)
        updateStatusText(tvAccessibilityStatus, hasAccessibility)
        updateStatusText(tvBatteryStatus, hasBattery)

        if (hasUsage && hasAccessibility && hasBattery) {
            btnContinue.isEnabled = true
            btnContinue.alpha = 1.0f
        } else {
            btnContinue.isEnabled = false
            btnContinue.alpha = 0.5f
        }
    }

    private fun updateStatusText(textView: TextView, isGranted: Boolean) {
        if (isGranted) {
            textView.text = "Granted"
            textView.setTextColor(Color.parseColor("#4CAF50")) // Green
        } else {
            textView.text = "Pending"
            textView.setTextColor(Color.parseColor("#FF5252")) // Red
        }
    }
}
