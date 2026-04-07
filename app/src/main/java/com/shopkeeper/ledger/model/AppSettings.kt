──
package com.shopkeeper.ledger.model

data class AppSettings(
    val shop_name: String = "",
    val owner_name: String = "",
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val state: String? = null,
    val gstin: String? = null,
    val currency_symbol: String = "₹",
    val date_format: String = "YYYY-MM-DD",
    val low_stock_alerts_enabled: Boolean = true,
    val auto_backup_enabled: Boolean = true,
    val app_lock_enabled: Boolean = false,
    val app_lock_pin: String? = null,
    val theme: String = "system",
    val invoice_template: String = "default",
    val custom_lists: Map<String, List<String>> = emptyMap()
)
kotlin// ───