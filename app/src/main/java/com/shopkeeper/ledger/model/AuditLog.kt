──
package com.shopkeeper.ledger.model

data class AuditLog(
    val id: String? = null,
    val action: String = "",
    val collection: String = "",
    val document_id: String = "",
    val changed_by: String = "",
    val diff: Map<String, Any>? = null,
    val created_at: String = ""
)
kotlin// ───