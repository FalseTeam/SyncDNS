package ru.falseteam.syncdns

import org.ini4j.Ini

data class DnsRecord(val name: String, val ip: String)

fun Ini.toListOfDNSRecord() = this.values.flatMap { it.asIterable() }.map { DnsRecord(it.key, it.value) }

fun List<DnsRecord>.getDuplicates() = this.groupingBy { it.name }.eachCount().filter { it.value > 1 }.map { it.key }
