package com.localwatch.server.network

import java.net.Inet4Address
import java.net.NetworkInterface

object NetworkUtils {
    fun bestLocalIp(): String? {
        val candidates = NetworkInterface.getNetworkInterfaces()?.toList().orEmpty()
            .filter { runCatching { it.isUp && !it.isLoopback }.getOrDefault(false) }
            .flatMap { network ->
                network.inetAddresses.toList()
                    .filterIsInstance<Inet4Address>()
                    .map { network.name.orEmpty() to it }
            }
            .filter { (_, address) -> !address.isLoopbackAddress && !address.isLinkLocalAddress }
            .map { (name, address) -> name to address.hostAddress.orEmpty() }

        return candidates.sortedBy { (name, address) ->
            val interfaceRank = when {
                name.contains("wlan", true) || name.contains("wifi", true) -> 0
                name.contains("ap", true) || name.contains("swlan", true) -> 1
                name.contains("eth", true) -> 2
                name.contains("rmnet", true) -> 8
                else -> 4
            }
            val addressRank = when {
                address.startsWith("192.168.43.") -> 0
                address.startsWith("192.168.") -> 1
                address.startsWith("10.") -> 2
                address.startsWith("172.") -> 3
                else -> 6
            }
            interfaceRank * 10 + addressRank
        }.firstOrNull()?.second
    }

    fun serverUrl(port: Int): String? = bestLocalIp()?.let { "http://$it:$port" }
}
