package dev.lucasnlm.external.model

sealed class PurchaseInfo {
    data class PurchaseResult(
        val isFreeUnlock: Boolean,
        val unlockStatus: Boolean,
    ) : PurchaseInfo()

    object PurchaseFail : PurchaseInfo()
}
