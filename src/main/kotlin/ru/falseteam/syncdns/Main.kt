package ru.falseteam.syncdns

import me.legrange.mikrotik.MikrotikApiException
import org.ini4j.Ini
import java.io.File
import java.io.FileNotFoundException
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
    if (!records.validate()) exitProcess(1)
    credentials.forEach { updateDnsTable(it, records) }
}

fun updateDnsTable(credential: Credential, records: Map<String, List<DnsRecord>>) {
    val override = records[credential.name].orEmpty()
    val list = records["common"].orEmpty().toMutableList()
    list += credential.include.filter { it != credential.name }.map { records[it].orEmpty() }
        .flatMap { it.asIterable() }
    list.removeIf { rec -> override.map { it.name }.contains(rec.name) }
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