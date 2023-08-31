package dev.lucasnlm.external.model

sealed class PurchaseInfo {
    data class PurchaseResult(
        val isFreeUnlock: Boolean,
        val unlockStatus: Boolean,
    ) : PurchaseInfo()

    data object PurchaseFail : PurchaseInfo()
}
