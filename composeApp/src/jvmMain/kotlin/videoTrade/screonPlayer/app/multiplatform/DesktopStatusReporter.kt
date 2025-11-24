/*
 * Copyright (c) LLC "Centr Distribyucii"
 * All rights reserved.
 */
package videoTrade.screonPlayer.app.multiplatform

import com.sun.management.OperatingSystemMXBean
import videoTrade.screonPlayer.app.domain.repository.StatusReporter
import videoTrade.screonPlayer.app.domain.model.StatusInfo
import java.io.File
import java.lang.management.ManagementFactory
import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.*


data class AppMemoryUsage(
    val usedMb: Int,
    val percent: Int
)


class DesktopStatusReporter : StatusReporter {

    private val bootMillis = System.currentTimeMillis()
    private var lastSyncAt: String? = null

    override fun markSynced() {
        lastSyncAt = now()
    }

    override fun getStatus(): StatusInfo {
        val osBean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean
        val ramPercent = getRamUsage(osBean)

        return StatusInfo(
            uptimeSec = (System.currentTimeMillis() - bootMillis) / 1000,
            storageFreeMb = File(".").usableSpace / 1024.0 / 1024.0,
            lastSyncAt = lastSyncAt,
            volumeLevel = 100, // заглушка
            networkState = if (InetAddress.getByName("8.8.8.8").isReachable(100)) "online" else "offline",
            softwareVersion = System.getProperty("os.name") + " " + System.getProperty("os.version"),
            ramUsagePercent = ramPercent
        )
    }

    private fun getRamUsage(osBean: OperatingSystemMXBean): Int {
        val used = osBean.totalPhysicalMemorySize - osBean.freePhysicalMemorySize
        return (used.toDouble() / osBean.totalPhysicalMemorySize * 100).toInt()
    }

    private fun now(): String {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
        return df.format(Date()).replace(Regex("([+-]\\d{2})(\\d{2})$"), "$1:$2")
    }
}
