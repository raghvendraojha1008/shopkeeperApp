──
package com.shopkeeper.ledger.model

data class WasteEntry(
    val id: String? = null,
    val date: String = "",
    val item_name: String = "",
    val quantity: Double = 0.0,
    val unit: String = "",
    val reason: String = "",
    val notes: String? = null,
    val created_at: String = ""
)
kotlin// ───