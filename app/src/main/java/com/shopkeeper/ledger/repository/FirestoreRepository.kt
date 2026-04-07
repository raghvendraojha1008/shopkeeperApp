──
package com.shopkeeper.ledger.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.shopkeeper.ledger.model.*
import com.shopkeeper.ledger.service.AuditService
import com.shopkeeper.ledger.service.TrashService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.time.Instant

object FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()

    private val DEFAULT_SETTINGS = AppSettings(
        shop_name = "",
        owner_name = "",
        currency_symbol = "₹",
        date_format = "YYYY-MM-DD",
        low_stock_alerts_enabled = true,
        auto_backup_enabled = true,
        app_lock_enabled = false,
        theme = "system",
        invoice_template = "default",
        custom_lists = emptyMap()
    )

    // ─── Generic collection Flow ───────────────────────────────────────────────

    fun getCollection(uid: String, col: String): Flow<List<Map<String, Any>>> = callbackFlow {
        val ref = db.collection("users").document(uid).collection(col)
        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.data?.toMutableMap()?.also { it["id"] = doc.id }
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    // ─── LedgerEntry Flow with filters ────────────────────────────────────────

    fun getLedgerFlow(uid: String, filters: LedgerFilter): Flow<List<LedgerEntry>> = callbackFlow {
        var query: Query = db.collection("users").document(uid).collection("ledger_entries")

        filters.type?.let { query = query.whereEqualTo("type", it) }
        filters.partyName?.let { query = query.whereEqualTo("party_name", it) }
        filters.dateFrom?.let { query = query.whereGreaterThanOrEqualTo("date", it) }
        filters.dateTo?.let { query = query.whereLessThanOrEqualTo("date", it) }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                mapToLedgerEntry(doc.id, data)
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    // ─── InventoryItem Flow ────────────────────────────────────────────────────

    fun getInventoryFlow(uid: String): Flow<List<InventoryItem>> = callbackFlow {
        val ref = db.collection("users").document(uid).collection("inventory")
        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                mapToInventoryItem(doc.id, data)
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    // ─── Transaction Flow ──────────────────────────────────────────────────────

    fun getTransactionsFlow(uid: String): Flow<List<Transaction>> = callbackFlow {
        val ref = db.collection("users").document(uid).collection("transactions")
        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                mapToTransaction(doc.id, data)
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    // ─── Party Flow ───────────────────────────────────────────────────────────

    fun getPartiesFlow(uid: String): Flow<List<Party>> = callbackFlow {
        val ref = db.collection("users").document(uid).collection("parties")
        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                mapToParty(doc.id, data)
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    // ─── One-shot get ──────────────────────────────────────────────────────────

    suspend fun getOne(uid: String, col: String, id: String): Map<String, Any>? {
        val doc = db.collection("users").document(uid).collection(col).document(id).get().await()
        return doc.data?.toMutableMap()?.also { it["id"] = doc.id }
    }

    // ─── Add ──────────────────────────────────────────────────────────────────

    suspend fun add(uid: String, col: String, data: Map<String, Any>): String {
        val ref = db.collection("users").document(uid).collection(col).add(data).await()
        AuditService.log(
            uid = uid,
            action = "create",
            collection = col,
            documentId = ref.id
        )
        return ref.id
    }

    // ─── Update ───────────────────────────────────────────────────────────────

    suspend fun update(uid: String, col: String, id: String, data: Map<String, Any>) {
        val existing = getOne(uid, col, id)
        val diff = buildDiff(existing, data)
        db.collection("users").document(uid).collection(col).document(id).set(data).await()
        AuditService.log(
            uid = uid,
            action = "update",
            collection = col,
            documentId = id,
            diff = diff
        )
    }

    // ─── Soft delete ──────────────────────────────────────────────────────────

    suspend fun delete(uid: String, col: String, id: String, data: Map<String, Any>) {
        TrashService.moveToTrash(
            uid = uid,
            originalCollection = col,
            originalId = id,
            data = data
        )
        db.collection("users").document(uid).collection(col).document(id).delete().await()
        AuditService.log(
            uid = uid,
            action = "delete",
            collection = col,
            documentId = id
        )
    }

    // ─── Batch add (chunked at 450) ────────────────────────────────────────────

    suspend fun batchAdd(uid: String, col: String, items: List<Map<String, Any>>) {
        val colRef = db.collection("users").document(uid).collection(col)
        items.chunked(450).forEach { chunk ->
            val batch = db.batch()
            chunk.forEach { item ->
                val docRef = colRef.document()
                batch.set(docRef, item)
            }
            batch.commit().await()
        }
    }

    // ─── Settings ─────────────────────────────────────────────────────────────

    suspend fun getSettings(uid: String): AppSettings {
        val doc = db.collection("users").document(uid)
            .collection("settings").document("config")
            .get().await()
        val data = doc.data ?: return DEFAULT_SETTINGS
        return mergeWithDefaults(data)
    }

    suspend fun saveSettings(uid: String, data: AppSettings) {
        val map = appSettingsToMap(data)
        db.collection("users").document(uid)
            .collection("settings").document("config")
            .set(map, com.google.firebase.firestore.SetOptions.merge())
            .await()
    }

    // ─── Restore backup ───────────────────────────────────────────────────────

    suspend fun restoreBackup(uid: String, json: String) {
        val root = JSONObject(json)
        val collections = listOf(
            "ledger_entries", "transactions", "inventory", "parties",
            "vehicles", "expenses", "waste_entries", "audit_logs", "trash"
        )
        collections.forEach { col ->
            if (root.has(col)) {
                val arr = root.getJSONArray(col)
                val items = (0 until arr.length()).map { i ->
                    val obj = arr.getJSONObject(i)
                    val map = mutableMapOf<String, Any>()
                    obj.keys().forEach { key -> map[key] = obj.get(key) }
                    map
                }
                batchAdd(uid, col, items)
            }
        }
        if (root.has("settings")) {
            val settingsObj = root.getJSONObject("settings")
            val settingsMap = mutableMapOf<String, Any>()
            settingsObj.keys().forEach { key -> settingsMap[key] = settingsObj.get(key) }
            db.collection("users").document(uid)
                .collection("settings").document("config")
                .set(settingsMap, com.google.firebase.firestore.SetOptions.merge())
                .await()
        }
    }

    // ─── Factory reset ────────────────────────────────────────────────────────

    suspend fun factoryReset(uid: String) {
        val collections = listOf(
            "ledger_entries", "transactions", "inventory", "parties",
            "vehicles", "expenses", "waste_entries", "trash", "settings"
        )
        collections.forEach { col ->
            val docs = db.collection("users").document(uid).collection(col).get().await()
            docs.documents.chunked(450).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { doc -> batch.delete(doc.reference) }
                batch.commit().await()
            }
        }
        val profileRef = db.collection("users").document(uid).collection("profile").get().await()
        profileRef.documents.chunked(450).forEach { chunk ->
            val batch = db.batch()
            chunk.forEach { doc -> batch.delete(doc.reference) }
            batch.commit().await()
        }
    }

    // ─── Find admin by email ──────────────────────────────────────────────────

    suspend fun findAdminByEmail(email: String): String? {
        val result = db.collectionGroup("profile")
            .whereEqualTo("email", email)
            .whereEqualTo("role", "owner")
            .limit(1)
            .get()
            .await()
        return result.documents.firstOrNull()?.getString("uid")
    }

    // ─── Private mappers ──────────────────────────────────────────────────────

    @Suppress("UNCHECKED_CAST")
    private fun mapToLedgerEntry(id: String, data: Map<String, Any>): LedgerEntry {
        val rawItems = data["items"] as? List<Map<String, Any>> ?: emptyList()
        val items = rawItems.map { item ->
            LedgerItem(
                name = item["name"] as? String ?: "",
                quantity = (item["quantity"] as? Number)?.toDouble() ?: 0.0,
                unit = item["unit"] as? String ?: "",
                rate = (item["rate"] as? Number)?.toDouble() ?: 0.0,
                gst_percent = (item["gst_percent"] as? Number)?.toDouble() ?: 0.0,
                price_type = item["price_type"] as? String ?: "exclusive",
                total = (item["total"] as? Number)?.toDouble() ?: 0.0
            )
        }
        return LedgerEntry(
            id = id,
            date = data["date"] as? String ?: "",
            type = data["type"] as? String ?: "",
            party_name = data["party_name"] as? String ?: "",
            invoice_no = data["invoice_no"] as? String,
            bill_no = data["bill_no"] as? String,
            items = items,
            total_amount = (data["total_amount"] as? Number)?.toDouble() ?: 0.0,
            discount_amount = (data["discount_amount"] as? Number)?.toDouble(),
            vehicle = data["vehicle"] as? String,
            vehicle_rent = (data["vehicle_rent"] as? Number)?.toDouble(),
            address = data["address"] as? String,
            notes = data["notes"] as? String,
            payment_received_by = data["payment_received_by"] as? String,
            paid_to = data["paid_to"] as? String,
            created_at = data["created_at"] as? String ?: ""
        )
    }

    private fun mapToInventoryItem(id: String, data: Map<String, Any>): InventoryItem {
        return InventoryItem(
            id = id,
            name = data["name"] as? String ?: "",
            unit = data["unit"] as? String ?: "",
            category = data["category"] as? String,
            purchase_rate = (data["purchase_rate"] as? Number)?.toDouble(),
            selling_rate = (data["selling_rate"] as? Number)?.toDouble(),
            gst_percent = (data["gst_percent"] as? Number)?.toDouble(),
            barcode = data["barcode"] as? String,
            current_stock = (data["current_stock"] as? Number)?.toDouble() ?: 0.0,
            low_stock_alert = (data["low_stock_alert"] as? Number)?.toDouble(),
            image_url = data["image_url"] as? String,
            is_active = data["is_active"] as? Boolean ?: true,
            created_at = data["created_at"] as? String ?: ""
        )
    }

    private fun mapToTransaction(id: String, data: Map<String, Any>): Transaction {
        return Transaction(
            id = id,
            date = data["date"] as? String ?: "",
            type = data["type"] as? String ?: "",
            party_name = data["party_name"] as? String ?: "",
            amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
            payment_mode = data["payment_mode"] as? String,
            payment_purpose = data["payment_purpose"] as? String,
            bill_no = data["bill_no"] as? String,
            notes = data["notes"] as? String,
            received_by = data["received_by"] as? String,
            paid_by = data["paid_by"] as? String,
            transaction_id = data["transaction_id"] as? String,
            created_at = data["created_at"] as? String ?: ""
        )
    }

    private fun mapToParty(id: String, data: Map<String, Any>): Party {
        return Party(
            id = id,
            name = data["name"] as? String ?: "",
            role = data["role"] as? String ?: "",
            phone = data["phone"] as? String,
            email = data["email"] as? String,
            address = data["address"] as? String,
            state = data["state"] as? String,
            gstin = data["gstin"] as? String,
            opening_balance = (data["opening_balance"] as? Number)?.toDouble() ?: 0.0,
            balance = (data["balance"] as? Number)?.toDouble() ?: 0.0,
            notes = data["notes"] as? String,
            created_at = data["created_at"] as? String ?: ""
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun mergeWithDefaults(data: Map<String, Any>): AppSettings {
        return AppSettings(
            shop_name = data["shop_name"] as? String ?: DEFAULT_SETTINGS.shop_name,
            owner_name = data["owner_name"] as? String ?: DEFAULT_SETTINGS.owner_name,
            phone = data["phone"] as? String,
            email = data["email"] as? String,
            address = data["address"] as? String,
            state = data["state"] as? String,
            gstin = data["gstin"] as? String,
            currency_symbol = data["currency_symbol"] as? String ?: DEFAULT_SETTINGS.currency_symbol,
            date_format = data["date_format"] as? String ?: DEFAULT_SETTINGS.date_format,
            low_stock_alerts_enabled = data["low_stock_alerts_enabled"] as? Boolean
                ?: DEFAULT_SETTINGS.low_stock_alerts_enabled,
            auto_backup_enabled = data["auto_backup_enabled"] as? Boolean
                ?: DEFAULT_SETTINGS.auto_backup_enabled,
            app_lock_enabled = data["app_lock_enabled"] as? Boolean
                ?: DEFAULT_SETTINGS.app_lock_enabled,
            app_lock_pin = data["app_lock_pin"] as? String,
            theme = data["theme"] as? String ?: DEFAULT_SETTINGS.theme,
            invoice_template = data["invoice_template"] as? String
                ?: DEFAULT_SETTINGS.invoice_template,
            custom_lists = (data["custom_lists"] as? Map<String, List<String>>)
                ?: DEFAULT_SETTINGS.custom_lists
        )
    }

    private fun appSettingsToMap(settings: AppSettings): Map<String, Any?> {
        return mapOf(
            "shop_name" to settings.shop_name,
            "owner_name" to settings.owner_name,
            "phone" to settings.phone,
            "email" to settings.email,
            "address" to settings.address,
            "state" to settings.state,
            "gstin" to settings.gstin,
            "currency_symbol" to settings.currency_symbol,
            "date_format" to settings.date_format,
            "low_stock_alerts_enabled" to settings.low_stock_alerts_enabled,
            "auto_backup_enabled" to settings.auto_backup_enabled,
            "app_lock_enabled" to settings.app_lock_enabled,
            "app_lock_pin" to settings.app_lock_pin,
            "theme" to settings.theme,
            "invoice_template" to settings.invoice_template,
            "custom_lists" to settings.custom_lists
        )
    }

    private fun buildDiff(
        existing: Map<String, Any>?,
        updated: Map<String, Any>
    ): Map<String, Any> {
        val diff = mutableMapOf<String, Any>()
        updated.forEach { (key, newValue) ->
            val oldValue = existing?.get(key)
            if (oldValue != newValue) {
                diff[key] = mapOf("from" to (oldValue ?: "null"), "to" to newValue)
            }
        }
        return diff
    }
}