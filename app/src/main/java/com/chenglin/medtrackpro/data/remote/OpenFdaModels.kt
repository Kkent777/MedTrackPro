package com.chenglin.medtrackpro.data.remote

import com.google.gson.annotations.SerializedName

data class OpenFdaLabelResponse(
    val results: List<OpenFdaDrugLabel>? = null
)

data class OpenFdaDrugLabel(
    val purpose: List<String>? = null,
    val warnings: List<String>? = null,
    @SerializedName("dosage_and_administration")
    val dosageAndAdministration: List<String>? = null,
    @SerializedName("active_ingredient")
    val activeIngredient: List<String>? = null,
    @SerializedName("indications_and_usage")
    val indicationsAndUsage: List<String>? = null,
    val openfda: OpenFdaProductInfo? = null
)

data class OpenFdaProductInfo(
    @SerializedName("brand_name")
    val brandName: List<String>? = null,
    @SerializedName("generic_name")
    val genericName: List<String>? = null
)
