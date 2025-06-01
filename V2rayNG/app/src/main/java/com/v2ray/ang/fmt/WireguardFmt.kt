package com.v2ray.ang.fmt

import com.v2ray.ang.AppConfig
import com.v2ray.ang.AppConfig.WIREGUARD_LOCAL_ADDRESS_V4
import com.v2ray.ang.dto.EConfigType
import com.v2ray.ang.dto.ProfileItem
import com.v2ray.ang.dto.V2rayConfig.OutboundBean
import com.v2ray.ang.extension.idnHost
import com.v2ray.ang.extension.isNotNullEmpty
import com.v2ray.ang.util.Utils
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.text.orEmpty

object WireguardFmt : FmtBase() {
    fun parse(str: String): ProfileItem? {
        val config = ProfileItem.create(EConfigType.WIREGUARD)

        val uri = URI(Utils.fixIllegalUrl(str))
        if (uri.rawQuery.isNullOrEmpty()) return null
        val queryParam = getQueryParam(uri)

        config.remarks = Utils.urlDecode(uri.fragment.orEmpty())
        config.server = uri.idnHost
        config.serverPort = uri.port.toString()

        config.secretKey = uri.userInfo.orEmpty()
        config.localAddress = (queryParam["address"] ?: WIREGUARD_LOCAL_ADDRESS_V4)
        config.publicKey = queryParam["publickey"].orEmpty()
        config.preSharedKey = queryParam["presharedkey"].orEmpty()
        config.mtu = Utils.parseInt(queryParam["mtu"] ?: AppConfig.WIREGUARD_LOCAL_MTU)
        config.reserved = (queryParam["reserved"] ?: "0,0,0")

        config.keepAlive = Utils.parseInt(queryParam["keepalive"] ?: AppConfig.WIREGUARD_keep_alive)
        config.wnoise = queryParam["wnoise"] ?: AppConfig.WIREGUARD_wnoise
        config.wnoisecount = queryParam["wnoisecount"] ?: AppConfig.WIREGUARD_wnoisecount
        config.wnoisedelay = queryParam["wnoisedelay"] ?: AppConfig.WIREGUARD_wnoisedelay
        config.wpayloadsize = queryParam["wpayloadsize"] ?: AppConfig.WIREGUARD_wpayloadsize

        return config
    }

    fun parseWireguardConfFile(str: String): ProfileItem? {
        val config = ProfileItem.create(EConfigType.WIREGUARD)

        val interfaceParams: MutableMap<String, String> = mutableMapOf()
        val peerParams: MutableMap<String, String> = mutableMapOf()
        var my_remark = ""

        var currentSection: String? = null

        str.lines().forEach { line ->
            val trimmedLine = line.trim()


            if (trimmedLine.isEmpty()) {
                return@forEach
            }

            if(trimmedLine.startsWith("#")){
                my_remark = if(trimmedLine.length>30){
                    trimmedLine.substring(1,30).trim()
                }else{
                    trimmedLine.substring(1).trim()
                }
                return@forEach
            }

            when {
                trimmedLine.startsWith("[Interface]", ignoreCase = true) -> currentSection = "Interface"
                trimmedLine.startsWith("[Peer]", ignoreCase = true) -> currentSection = "Peer"
                else -> {
                    if (currentSection != null) {
                        val parts = trimmedLine.split("=", limit = 2).map { it.trim() }
                        if (parts.size == 2) {
                            val key = parts[0].lowercase()
                            val value = parts[1]
                            when (currentSection) {
                                "Interface" -> interfaceParams[key] = value
                                "Peer" -> peerParams[key] = value
                            }
                        }
                    }
                }
            }
        }

        if(my_remark.isEmpty()){
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            val currentDate = dateFormat.format(Date())
            config.remarks = "WG $currentDate"
        }else{
            config.remarks = my_remark
        }


        config.secretKey = interfaceParams["privatekey"].orEmpty()
        config.localAddress = interfaceParams["address"] ?: WIREGUARD_LOCAL_ADDRESS_V4
        config.mtu = Utils.parseInt(interfaceParams["mtu"] ?: AppConfig.WIREGUARD_LOCAL_MTU)
        config.publicKey = peerParams["publickey"].orEmpty()
        config.preSharedKey = peerParams["presharedkey"].orEmpty()
        val endpoint = peerParams["endpoint"].orEmpty()
        val endpointParts = endpoint.split(":", limit = 2)
        if (endpointParts.size == 2) {
            config.server = endpointParts[0]
            config.serverPort = endpointParts[1]
        } else {
            config.server = endpoint
            config.serverPort = ""
        }
        config.reserved = peerParams["reserved"] ?: "0,0,0"

        config.keepAlive = Utils.parseInt(peerParams["keepalive"] ?: AppConfig.WIREGUARD_keep_alive)
        config.wnoise = interfaceParams["wnoise"] ?: AppConfig.WIREGUARD_wnoise
        config.wnoisecount = interfaceParams["wnoisecount"] ?: AppConfig.WIREGUARD_wnoisecount
        config.wnoisedelay = interfaceParams["wnoisedelay"] ?: AppConfig.WIREGUARD_wnoisedelay
        config.wpayloadsize = interfaceParams["wpayloadsize"] ?: AppConfig.WIREGUARD_wpayloadsize

        return config
    }

    fun toOutbound(profileItem: ProfileItem): OutboundBean? {
        val outboundBean = OutboundBean.create(EConfigType.WIREGUARD)

        outboundBean?.settings?.let { wireguard ->
            wireguard.secretKey = profileItem.secretKey
            wireguard.address = (profileItem.localAddress ?: WIREGUARD_LOCAL_ADDRESS_V4).split(",")
            wireguard.peers?.firstOrNull()?.let { peer ->
                peer.publicKey = profileItem.publicKey.orEmpty()
                peer.preSharedKey = profileItem.preSharedKey.orEmpty()
                peer.endpoint = Utils.getIpv6Address(profileItem.server) + ":${profileItem.serverPort}"
                peer.keepAlive = profileItem.keepAlive ?: Utils.parseInt(AppConfig.WIREGUARD_keep_alive)
            }
            wireguard.mtu = profileItem.mtu

            try {
                wireguard.reserved = profileItem.reserved?.split(",")?.map { it.toInt() }
            }catch (_:Exception){
                wireguard.reserved = listOf(0,0,0)
            }

            wireguard.wnoise = profileItem.wnoise
            wireguard.wnoisecount = profileItem.wnoisecount
            wireguard.wnoisedelay = profileItem.wnoisedelay
            wireguard.wpayloadsize = profileItem.wpayloadsize

        }

        return outboundBean
    }

    fun toUri(config: ProfileItem): String {
        val dicQuery = HashMap<String, String>()

        dicQuery["publickey"] = config.publicKey.orEmpty()

        if (config.reserved != null) {
            dicQuery["reserved"] = Utils.removeWhiteSpace(config.reserved).orEmpty()
        }
        dicQuery["address"] = Utils.removeWhiteSpace(config.localAddress).orEmpty()

        if (config.mtu != null) {
            dicQuery["mtu"] = config.mtu.toString()
        }

        if (config.preSharedKey.isNotNullEmpty()) {
            dicQuery["presharedkey"] = Utils.removeWhiteSpace(config.preSharedKey).orEmpty()
        }

        //----------
        if (config.keepAlive != null) {
            dicQuery["keepalive"] = config.keepAlive.toString()
        }
        if (config.wnoise.isNotNullEmpty()) {
            dicQuery["wnoise"] = Utils.removeWhiteSpace(config.wnoise).orEmpty()
        }
        if (config.wnoisecount.isNotNullEmpty()) {
            dicQuery["wnoisecount"] = Utils.removeWhiteSpace(config.wnoisecount).orEmpty()
        }
        if (config.wnoisedelay.isNotNullEmpty()) {
            dicQuery["wnoisedelay"] = Utils.removeWhiteSpace(config.wnoisedelay).orEmpty()
        }
        if (config.wpayloadsize.isNotNullEmpty()) {
            dicQuery["wpayloadsize"] = Utils.removeWhiteSpace(config.wpayloadsize).orEmpty()
        }
        //---------

        return toUri(config, config.secretKey, dicQuery)
    }
}
