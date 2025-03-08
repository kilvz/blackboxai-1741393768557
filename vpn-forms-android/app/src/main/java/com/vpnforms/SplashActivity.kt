package com.vpnforms

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.vpnforms.service.FormsVpnService
import com.vpnforms.service.FormsVpnService.Companion.ACTION_VPN_STATUS
import com.vpnforms.service.FormsVpnService.Companion.EXTRA_VPN_STARTED

class SplashActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "SplashActivity"
        private const val VPN_LAUNCH_TIMEOUT = 3000L
    }

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            startVpn()
        } else {
            showVpnError()
        }
    }

    private val vpnStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_VPN_STATUS) {
                val started = intent.getBooleanExtra(EXTRA_VPN_STARTED, false)
                if (started) {
                    proceedToMain()
                } else {
                    showVpnError()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Prevent screenshots and screen recording
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        
        setContentView(R.layout.activity_splash)

        // Handle back button for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Disable back button during splash
                }
            })
        }

        // Register receiver based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                vpnStatusReceiver,
                IntentFilter(ACTION_VPN_STATUS),
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            registerReceiver(vpnStatusReceiver, IntentFilter(ACTION_VPN_STATUS))
        }

        if (FormsVpnService.isActive) {
            proceedToMain()
        } else {
            showVpnExplanationDialog()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(vpnStatusReceiver)
    }

    private fun showVpnExplanationDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.vpn_setup_title)
            .setMessage(R.string.vpn_setup_message)
            .setCancelable(false)
            .setPositiveButton(R.string.vpn_setup_continue) { _, _ ->
                prepareVpn()
            }
            .setNegativeButton(R.string.vpn_setup_cancel) { _, _ ->
                finish()
            }
            .show()
    }

    private fun prepareVpn() {
        val vpnIntent = VpnService.prepare(this)
        if (vpnIntent != null) {
            AlertDialog.Builder(this)
                .setTitle(R.string.vpn_config_title)
                .setMessage(R.string.vpn_config_message)
                .setCancelable(false)
                .setPositiveButton(R.string.vpn_config_ok) { _, _ ->
                    vpnPermissionLauncher.launch(vpnIntent)
                }
                .setNegativeButton(R.string.vpn_config_cancel) { _, _ ->
                    finish()
                }
                .show()
        } else {
            startVpn()
        }
    }

    private fun startVpn() {
        try {
            val serviceIntent = Intent(this, FormsVpnService::class.java)
            startService(serviceIntent)

            Handler(Looper.getMainLooper()).postDelayed({
                if (!isFinishing && !FormsVpnService.isActive) {
                    showVpnError()
                }
            }, VPN_LAUNCH_TIMEOUT)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting VPN", e)
            showVpnError()
        }
    }

    private fun proceedToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showVpnError() {
        AlertDialog.Builder(this)
            .setTitle(R.string.vpn_error_title)
            .setMessage(R.string.vpn_error_message)
            .setCancelable(false)
            .setPositiveButton(R.string.vpn_error_retry) { _, _ ->
                prepareVpn()
            }
            .setNegativeButton(R.string.vpn_error_exit) { _, _ ->
                finish()
            }
            .show()
    }

    // Support for Android 7-12
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            // Disable back button during splash
        } else {
            super.onBackPressed()
        }
    }
}
