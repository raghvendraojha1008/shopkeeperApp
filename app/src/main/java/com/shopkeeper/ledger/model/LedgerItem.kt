──
package com.shopkeeper.ledger.model

data class LedgerItem(
    val name: String = "",
    val quantity: Double = 0.0,
    val unit: String = "",
    val rate: Double = 0.0,
    val gst_percent: Double = 0.0,
    val price_type: String = "exclusive",
    val total: Double = 0.0
)
kotlin// ───