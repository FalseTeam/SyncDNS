package ru.falseteam.syncdns

import org.ini4j.Ini
import org.ini4j.Profile

data class Credential(
    val name: String,
    val address: String,
    val username: String,
    val password: String,
    val include: List<String>
)

fun Profile.Section.toCredential() = Credential(
    this.name,
    this["address"]!!,
    this["username"]!!,
    this["password"]!!,
    this["include"]!!.split(",")
)

fun Ini.toListOfCredential() = this.map { it.value.toCredential() }