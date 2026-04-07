──
package com.shopkeeper.ledger.model

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val role: String = "",
    val admin_uid: String? = null,
    val display_name: String? = null,
    val created_at: String = ""
)
kotlin// ───