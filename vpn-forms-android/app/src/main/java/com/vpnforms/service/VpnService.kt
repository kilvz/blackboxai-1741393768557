package com.vpnforms.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.vpnforms.MainActivity
import com.vpnforms.R
import com.vpnforms.utils.Constants
import com.vpnforms.utils.PacketFilter
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

class VpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null
    private val isRunning = AtomicBoolean(false)
    private var vpnJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val packetFilter = PacketFilter()

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "vpn_forms_channel"
        private const val NOTIFICATION_ID = 1
        private const val BUFFER_SIZE = 32767
        private const val MTU = 1500
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (isRunning.get()) {
            return START_STICKY
        }

        startForeground(NOTIFICATION_ID, createNotification())
        establishVpn()
        return START_STICKY
    }

    private fun establishVpn() {
        try {
            vpnInterface = Builder()
                .setSession("VPN Forms")
                .addAddress("10.0.0.2", 24)
                .addDnsServer("8.8.8.8")
                .addRoute("0.0.0.0", 0)
                .setMtu(MTU)
                .establish()

            isRunning.set(true)
            startVpnThread()
        } catch (e: Exception) {
            stopSelf()
        }
    }

    private fun startVpnThread() {
        vpnJob = scope.launch {
            try {
                val buffer = ByteBuffer.allocate(BUFFER_SIZE)
                val inputStream = FileInputStream(vpnInterface?.fileDescriptor)
                val outputStream = FileOutputStream(vpnInterface?.fileDescriptor)

                while (isRunning.get() && isActive) {
                    val length = inputStream.read(buffer.array())
                    if (length > 0) {
                        if (packetFilter.shouldAllowPacket(buffer.array(), length)) {
                            outputStream.write(buffer.array(), 0, length)
                        }
                    }
                    buffer.clear()
                }
            } catch (e: Exception) {
                stopSelf()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "VPN Forms Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "VPN Forms Service Channel"
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification() = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        .setContentTitle("VPN Forms Active")
        .setContentText("VPN is running to filter website access")
        .setSmallIcon(R.drawable.ic_vpn_connected)
        .setContentIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
        .build()

    override fun onDestroy() {
        isRunning.set(false)
        vpnJob?.cancel()
        vpnInterface?.close()
        vpnInterface = null
        scope.cancel()
        super.onDestroy()
    }
}
