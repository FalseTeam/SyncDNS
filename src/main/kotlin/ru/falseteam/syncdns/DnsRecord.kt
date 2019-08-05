package ru.falseteam.syncdns

import org.ini4j.Ini
import java.util.regex.Pattern

data class DnsRecord(
    val name: String,
    val ip: String
)

fun Ini.toMapOfDnsRecord() = this.map { it.key to it.value.map { record -> DnsRecord(record.key, record.value) } }.toMap()

fun List<DnsRecord>.getDuplicates() = this.groupingBy { it.name }.eachCount().filter { it.value > 1 }.map { it.key }

val ip = Pattern.compile("^(?:(?:25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(?:25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$")!!
val domain = Pattern.compile("^[a-zA-Z0-9](?:[a-zA-Z0-9-_]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-_]{0,61}[a-zA-Z0-9])?)*$")!!

fun Map<String, List<DnsRecord>>.validate(): Boolean {
    var result = true
    this.forEach { (s, list) ->
        val duplicates = list.getDuplicates()
        if (duplicates.isNotEmpty()) {
            println("[$s] Found duplicates: ${duplicates.joinToString(", ")}")
            result = false
        }
        list.forEach {
            var matcher = ip.matcher(it.ip)
            if (!matcher.find()) {
                println("[$s] IP address '${it.ip}' does not match the RegEx")
                result = false
            }
            matcher = domain.matcher(it.name)
            if (!matcher.find()) {
                println("[$s] Hostname '${it.name}' does not match the RegEx")
                result = false
            }
        }
    }
    return result
}