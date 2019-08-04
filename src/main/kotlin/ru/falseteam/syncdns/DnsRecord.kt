package ru.falseteam.syncdns

import org.ini4j.Ini

data class DnsRecord(val name: String, val ip: String)

fun Ini.toMapOfDnsRecord() =
    this.map { it.key to it.value.map { record -> DnsRecord(record.key, record.value) } }.toMap()

fun List<DnsRecord>.getDuplicates() = this.groupingBy { it.name }.eachCount().filter { it.value > 1 }.map { it.key }
