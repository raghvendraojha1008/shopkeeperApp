──
package com.shopkeeper.ledger.model

data class InventoryItem(
    val id: String? = null,
    val name: String = "",
    val unit: String = "",
    val category: String? = null,
    val purchase_rate: Double? = null,
    val selling_rate: Double? = null,
    val gst_percent: Double? = null,
    val barcode: String? = null,
    val current_stock: Double = 0.0,
    val low_stock_alert: Double? = null,
    val image_url: String? = null,
    val is_active: Boolean = true,
    val created_at: String = ""
)
kotlin// ───