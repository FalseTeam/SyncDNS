package ru.falseteam.syncdns

import org.ini4j.Ini
import java.util.regex.Pattern

data class DnsRecord(
    val name: String,
    val ip: String
)

fun Ini.toMapOfDnsRecord() = this.map { it.key to it.value.map { record -> DnsRecord(record.key, record.value) } }.toMap()

fun List<DnsRecord>.getDuplicates() = this.groupingBy { it.name }.eachCount().filter { it.value > 1 }.map { it.key }

val domain = Pattern.compile("^[a-zA-Z0-9](?:[a-zA-Z0-9-_]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-_]{0,61}[a-zA-Z0-9])?)*$")!!
val ip = Pattern.compile("^(?:(?:25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(?:25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$")!!

fun Map<String, List<DnsRecord>>.validate(): Boolean {
    var result = true
    this.forEach { (s, list) ->
        val duplicates = list.getDuplicates()
        if (duplicates.isNotEmpty()) {
            println("[$s] Found duplicates: ${duplicates.joinToString(", ")}")
            result = false
        }
        list.forEach rec@{
            var name = false
            var addr = false
            if (domain.matcher(it.name).find()) name = true
            if (ip.matcher(it.ip).find()) addr = true
            if (name && addr) return@rec
            result = false
            print("[$s] ${it.name} -> ${it.ip}: ")
            if (addr) println("Hostname does not match the RegEx")
            else if (name) println("IP address does not match the RegEx")
            else println("Hostname and IP address does not match the RegEx")
        }
    }
    return result
}