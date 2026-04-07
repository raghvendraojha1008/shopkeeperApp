──
package com.shopkeeper.ledger.model

data class LedgerEntry(
    val id: String? = null,
    val date: String = "",
    val type: String = "",
    val party_name: String = "",
    val invoice_no: String? = null,
    val bill_no: String? = null,
    val items: List<LedgerItem> = emptyList(),
    val total_amount: Double = 0.0,
    val discount_amount: Double? = null,
    val vehicle: String? = null,
    val vehicle_rent: Double? = null,
    val address: String? = null,
    val notes: String? = null,
    val payment_received_by: String? = null,
    val paid_to: String? = null,
    val created_at: String = ""
)
kotlin// ───