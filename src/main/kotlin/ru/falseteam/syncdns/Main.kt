package ru.falseteam.syncdns

import me.legrange.mikrotik.MikrotikApiException
import org.ini4j.Ini
import java.io.File
import java.io.FileNotFoundException
import java.util.regex.Pattern
import kotlin.system.exitProcess

fun main() {
    val credentials = try {
        Ini(File("routers.ini")).toListOfCredential()
    } catch (e: FileNotFoundException) {
        println(e.message)
        exitProcess(1)
    }
    val records = try {
        Ini(File("records.ini")).toMapOfDnsRecord()
    } catch (e: FileNotFoundException) {
        println(e.message)
        exitProcess(1)
    }
    val ip = Pattern.compile("^(?:(?:25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(?:25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$")
    val domain = Pattern.compile("^[a-z0-9](?:[a-z0-9-_]{0,61}[a-z0-9])?(?:\\.[a-z0-9](?:[a-z0-9-_]{0,61}[a-z0-9])?)*$")
    records.forEach { (s, list) ->
        val duplicates = list.getDuplicates()
        if (duplicates.isNotEmpty()) {
            println("[$s] Found duplicates: ${duplicates.joinToString(", ")}")
            exitProcess(1)
        }
        list.forEach {
            var matcher = ip.matcher(it.ip)
            if (!matcher.find()) {
                println("[$s] IP address '${it.ip}' does not match the RegEx")
                exitProcess(1)
            }
            matcher = domain.matcher(it.name)
            if (!matcher.find()) {
                println("[$s] Hostname '${it.name}' does not match the RegEx")
                exitProcess(1)
            }
        }
    }
    credentials.forEach { updateDnsTable(it, records) }
}

fun updateDnsTable(credential: Credential, records: Map<String, List<DnsRecord>>) {
    val override = records[credential.name].orEmpty()
    val list = records["common"].orEmpty().toMutableList()
    list += credential.include.filter { it != credential.name }.map { records[it].orEmpty() }
        .flatMap { it.asIterable() }
    list.removeIf { override.map { it.name }.contains(it.name) }
    list += override
    val duplicates = list.getDuplicates()
    if (duplicates.isNotEmpty()) {
        println("Found duplicates for ${credential.name}: ${duplicates.joinToString(", ")}")
        return
    }
    try {
        val dnsApi = credential.connect().getDnsApi()
        val currentRecords = dnsApi.getRecords()
        currentRecords.forEach { (id, v) ->
            val record = list.find { it.name == v.name }
            if (record == null) {
                dnsApi.removeRecord(id)
                return@forEach
            }
            if (record.ip == v.ip) return@forEach
            dnsApi.updateRecord(id, record.ip)
        }
        list.filter { rec -> currentRecords.values.find { it.name == rec.name } == null }
            .forEach { dnsApi.addRecord(it) }
    } catch (e: MikrotikApiException) {
        println("${credential.name} (${credential.address}): ${e.message}")
    }
}