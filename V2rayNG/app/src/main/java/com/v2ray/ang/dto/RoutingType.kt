package com.v2ray.ang.dto

enum class RoutingType(val fileName: String) {
    WHITE("custom_routing_white"),
    BLACK("custom_routing_black"),
    GLOBAL("custom_routing_global"),
    IRAN_LITE("custom_routing_iran_lite"),
    IRAN_MEDIUM("custom_routing_iran_medium"),
    IRAN_HEAVY("custom_routing_iran_heavy");

    companion object {
        fun fromIndex(index: Int): RoutingType {
            return when (index) {
                0 -> WHITE
                1 -> BLACK
                2 -> GLOBAL
                3 -> IRAN_LITE
                4 -> IRAN_MEDIUM
                5 -> IRAN_HEAVY
                else -> WHITE
            }
        }
    }
}
