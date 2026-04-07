──
package com.shopkeeper.ledger.model

data class Transaction(
    val id: String? = null,
    val date: String = "",
    val type: String = "",
    val party_name: String = "",
    val amount: Double = 0.0,
    val payment_mode: String? = null,
    val payment_purpose: String? = null,
    val bill_no: String? = null,
    val notes: String? = null,
    val received_by: String? = null,
    val paid_by: String? = null,
    val transaction_id: String? = null,
    val created_at: String = ""
)
kotlin// ───