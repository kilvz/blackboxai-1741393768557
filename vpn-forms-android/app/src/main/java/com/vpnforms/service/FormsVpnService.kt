package com.vpnforms.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.vpnforms.MainActivity
import com.vpnforms.R
import com.vpnforms.utils.Constants
import com.vpnforms.utils.PacketHandler
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicBoolean

class FormsVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null
    private val isRunning = AtomicBoolean(false)
    private var vpnThread: Thread? = null
    private var packetHandler: PacketHandler? = null

    companion object {
        private const val TAG = "FormsVpnService"
        private const val NOTIFICATION_CHANNEL_ID = "vpn_forms_channel"
        private const val NOTIFICATION_ID = 1
        private const val VPN_ADDRESS = "10.0.0.2"
        private const val VPN_ROUTE = "0.0.0.0"

        var isActive = false
            private set

        const val ACTION_VPN_STATUS = "com.vpnforms.VPN_STATUS"
        const val EXTRA_VPN_STARTED = "vpn_started"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Creating VPN service")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Starting VPN service")
        
        if (isRunning.get()) {
            Log.d(TAG, "VPN already running")
            return START_STICKY
        }

        startForeground(NOTIFICATION_ID, createNotification())
        startVpn()
        return START_STICKY
    }

    private fun startVpn() {
        try {
            Log.d(TAG, "Establishing VPN connection")
            
            // Create VPN interface
            vpnInterface = Builder()
                .setSession("VPN Forms")
                .addAddress(VPN_ADDRESS, 32)
                .addDnsServer(Constants.VPN_DNS)
                .addRoute(VPN_ROUTE, 0)
                .addDisallowedApplication(packageName)
                .setBlocking(true)
                .establish()

            vpnInterface?.let { vpn ->
                val inputStream = FileInputStream(vpn.fileDescriptor)
                val outputStream = FileOutputStream(vpn.fileDescriptor)
                
                // Create packet handler
                packetHandler = PacketHandler(inputStream, outputStream)

                isRunning.set(true)
                isActive = true
                broadcastStatus(true)

                // Start VPN thread
                vpnThread = Thread(VpnRunnable(), "VPN Thread").apply { start() }
                
                Log.d(TAG, "VPN started successfully")
            } ?: run {
                Log.e(TAG, "Failed to establish VPN interface")
                stopVpn()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error starting VPN", e)
            stopVpn()
        }
    }

    private inner class VpnRunnable : Runnable {
        override fun run() {
            Log.d(TAG, "VPN thread started")
            
            try {
                while (isRunning.get()) {
                    try {
                        // Handle packets
                        packetHandler?.handlePacket()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error handling packet", e)
                    }
                }
            } finally {
                Log.d(TAG, "VPN thread stopping")
                stopVpn()
            }
        }
    }

    private fun stopVpn() {
        Log.d(TAG, "Stopping VPN")
        
        isRunning.set(false)
        isActive = false
        
        vpnThread?.interrupt()
        vpnThread = null
        
        packetHandler = null
        
        vpnInterface?.close()
        vpnInterface = null
        
        broadcastStatus(false)
        stopForeground(true)
        stopSelf()
    }

    private fun broadcastStatus(started: Boolean) {
        val intent = Intent(ACTION_VPN_STATUS).apply {
            putExtra(EXTRA_VPN_STARTED, started)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_description)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification() = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        .setContentTitle(getString(R.string.notification_title))
        .setContentText(getString(R.string.notification_text))
        .setSmallIcon(R.drawable.ic_vpn_connected)
        .setOngoing(true)
        .setContentIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                },
                PendingIntent.FLAG_IMMUTABLE
            )
        )
        .build()

    override fun onDestroy() {
        Log.d(TAG, "Destroying VPN service")
        stopVpn()
        super.onDestroy()
    }

    override fun onRevoke() {
        Log.d(TAG, "VPN permission revoked")
        stopVpn()
        super.onRevoke()
    }
}
