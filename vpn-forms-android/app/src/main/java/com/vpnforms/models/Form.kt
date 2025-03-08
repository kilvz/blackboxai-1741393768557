package com.vpnforms.models

data class Form(
    val title: String,
    val description: String,
    val url: String
)

data class FormsResponse(
    val forms: List<Form>
)
