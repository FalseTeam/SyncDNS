package ru.falseteam.syncdns

import me.legrange.mikrotik.ApiConnection
import me.legrange.mikrotik.MikrotikApiException

class DnsApi(private val connection: ApiConnection) {
    fun getRecords() = connection.execute("/ip/dns/static/print")
            .map { it[".id"]!! to DnsRecord(it["name"]!!, it["address"]!!) }.toMap()

    fun updateRecord(id: String, address: String): List<Map<String, String>> =
            connection.execute("/ip/dns/static/set .id=$id address=$address")

    fun addRecord(record: DnsRecord): List<Map<String, String>> =
            connection.execute("/ip/dns/static/add name=${record.name} address=${record.ip}")

    fun removeRecord(id: String): List<Map<String, String>> =
            connection.execute("/ip/dns/static/remove .id=$id")
}

@Throws(MikrotikApiException::class)
fun Credential.connect(): ApiConnection {
    val apiConnection = ApiConnection.connect(this.address)
    apiConnection.login(this.username, this.password)
    return apiConnection
}

fun ApiConnection.getDnsApi(): DnsApi = DnsApi(this)
