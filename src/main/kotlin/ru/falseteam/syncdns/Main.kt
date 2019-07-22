package ru.falseteam.syncdns

import me.legrange.mikrotik.MikrotikApiException
import org.ini4j.Ini
import java.io.File
import java.util.regex.Pattern
import kotlin.system.exitProcess

fun main() {
    val credentials = Ini(File("routers.ini")).toListOfCredential()
    val records = Ini(File("records.ini")).toListOfDNSRecord()
    val duplicates = records.getDuplicates()
    if (duplicates.isNotEmpty()) {
        println("Found duplicates: ${duplicates.joinToString(", ")}")
        exitProcess(1)
    }
    val ip = Pattern.compile("^(?:(?:25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(?:25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$")
    val domain = Pattern.compile("^[a-z0-9](?:[a-z0-9-_]{0,61}[a-z0-9])?(?:\\.[a-z0-9](?:[a-z0-9-_]{0,61}[a-z0-9])?)*$")
    records.forEach {
        var matcher = ip.matcher(it.ip)
        if (!matcher.find()) {
            println("IP address '${it.ip}' does not match the RegEx")
            exitProcess(1)
        }
        matcher = domain.matcher(it.name)
        if (!matcher.find()) {
            println("Hostname '${it.name}' does not match the RegEx")
            exitProcess(1)
        }
    }
    credentials.forEach { updateDnsTable(it, records) }
}

fun updateDnsTable(credential: Credential, newRecords: List<DnsRecord>) {
    try {
        val dnsApi = credential.connect().getDnsApi()
        val currentRecords = dnsApi.getRecords()
        currentRecords.forEach { (id, v) ->
            val record = newRecords.find { it.name == v.name }
            if (record == null) {
                dnsApi.removeRecord(id)
                return@forEach
            }
            if (record.ip == v.ip) return@forEach
            dnsApi.updateRecord(id, record.ip)
        }
        newRecords.filter { rec -> currentRecords.values.find { it.name == rec.name } == null }.forEach { dnsApi.addRecord(it) }
    } catch (e: MikrotikApiException) {
        println("${credential.name} (${credential.address}): ${e.message}")
    }
}