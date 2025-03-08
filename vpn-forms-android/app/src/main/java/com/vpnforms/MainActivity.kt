package com.vpnforms

import android.app.Activity
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vpnforms.adapters.FormsAdapter
import com.vpnforms.github.GitHubService
import com.vpnforms.models.Form
import com.vpnforms.service.FormsVpnService
import com.vpnforms.service.FormsVpnService.Companion.ACTION_VPN_STATUS
import com.vpnforms.service.FormsVpnService.Companion.EXTRA_VPN_STARTED
import com.vpnforms.utils.Constants

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var vpnButton: Button
    private lateinit var noVpnMessage: View
    private lateinit var formsList: View
    private lateinit var notificationManager: NotificationManager
    private var previousInterruptionFilter = NotificationManager.INTERRUPTION_FILTER_ALL

    companion object {
        private const val TAG = "MainActivity"
        private const val VPN_REQUEST_CODE = 1
        private const val LOAD_DELAY = 500L // Short delay to ensure VPN is ready
    }

    private val dndPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (notificationManager.isNotificationPolicyAccessGranted) {
            enableDoNotDisturb()
        }
    }

    private val vpnStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_VPN_STATUS) {
                val started = intent.getBooleanExtra(EXTRA_VPN_STARTED, false)
                updateVpnStatus(started)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        setupViews()
        registerVpnReceiver()
        
        // Check if VPN is already active
        if (FormsVpnService.isActive) {
            Handler(Looper.getMainLooper()).post {
                updateVpnStatus(true)
            }
        }
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.formsRecyclerView)
        vpnButton = findViewById(R.id.vpnButton)
        noVpnMessage = findViewById(R.id.noVpnMessage)
        formsList = findViewById(R.id.formsList)

        recyclerView.layoutManager = LinearLayoutManager(this)
        
        vpnButton.setOnClickListener {
            if (FormsVpnService.isActive) {
                stopVpn()
            } else {
                startVpn()
            }
        }
    }

    private fun registerVpnReceiver() {
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(vpnStatusReceiver, IntentFilter(ACTION_VPN_STATUS))
    }

    private fun updateVpnStatus(isRunning: Boolean) {
        Log.d(TAG, "Updating VPN status: $isRunning")
        
        noVpnMessage.visibility = if (isRunning) View.GONE else View.VISIBLE
        formsList.visibility = if (isRunning) View.VISIBLE else View.GONE
        vpnButton.visibility = if (isRunning) View.GONE else View.VISIBLE

        if (isRunning) {
            // Enable Do Not Disturb when VPN starts
            if (notificationManager.isNotificationPolicyAccessGranted) {
                enableDoNotDisturb()
            } else {
                requestDndPermission()
            }

            // Add a small delay before loading forms
            Handler(Looper.getMainLooper()).postDelayed({
                loadFormsFromGitHub()
            }, LOAD_DELAY)
        } else {
            // Disable Do Not Disturb when VPN stops
            if (notificationManager.isNotificationPolicyAccessGranted) {
                disableDoNotDisturb()
            }
            // Clear forms when VPN is stopped
            recyclerView.adapter = null
        }
    }

    private fun requestDndPermission() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        dndPermissionLauncher.launch(intent)
    }

    private fun enableDoNotDisturb() {
        try {
            previousInterruptionFilter = notificationManager.currentInterruptionFilter
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
            Toast.makeText(this, "Do Not Disturb enabled", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error enabling Do Not Disturb", e)
        }
    }

    private fun disableDoNotDisturb() {
        try {
            notificationManager.setInterruptionFilter(previousInterruptionFilter)
            Toast.makeText(this, "Do Not Disturb disabled", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error disabling Do Not Disturb", e)
        }
    }

    private fun startVpn() {
        try {
            val vpnIntent = VpnService.prepare(this)
            if (vpnIntent != null) {
                startActivityForResult(vpnIntent, VPN_REQUEST_CODE)
            } else {
                onActivityResult(VPN_REQUEST_CODE, Activity.RESULT_OK, null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting VPN", e)
            showError(getString(R.string.vpn_error_message))
        }
    }

    private fun stopVpn() {
        try {
            val serviceIntent = Intent(this, FormsVpnService::class.java)
            stopService(serviceIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping VPN", e)
            showError(getString(R.string.vpn_stop_error))
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    val serviceIntent = Intent(this, FormsVpnService::class.java)
                    startService(serviceIntent)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in onActivityResult", e)
                    showError(getString(R.string.vpn_error_message))
                }
            } else {
                showError(getString(R.string.vpn_permission_denied))
            }
        }
    }

    private fun loadFormsFromGitHub() {
        Log.d(TAG, "Loading forms from GitHub...")
        GitHubService.fetchForms { forms: List<Form>?, error: Exception? ->
            runOnUiThread {
                if (error != null) {
                    Log.e(TAG, "Error loading forms", error)
                    showError("Failed to load forms: ${error.message}")
                    return@runOnUiThread
                }
                
                forms?.let { formsList ->
                    Log.d(TAG, "Forms loaded successfully: ${formsList.size} forms")
                    recyclerView.adapter = FormsAdapter(formsList) { url: String ->
                        openWebView(url)
                    }
                }
            }
        }
    }

    private fun openWebView(url: String) {
        val intent = Intent(this, WebViewActivity::class.java).apply {
            putExtra(Constants.EXTRA_URL, url)
        }
        startActivity(intent)
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Restore previous DND state if VPN is active
        if (FormsVpnService.isActive && notificationManager.isNotificationPolicyAccessGranted) {
            disableDoNotDisturb()
        }
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(vpnStatusReceiver)
    }
}
