package com.example.za_app

data class lieu(val nom: String,val specialite: String,val budget: String,val jourOuv: String?,val heureOuv: String,val heureFerm: String,
                val longitude: Double,val latitude: Double, val createur: String, val isValid: Boolean)

data class lieuRecu(val id: String, val nom: String,val specialite: String,val budget: String,val jourOuv: String?,val heureOuv: String,val heureFerm: String,
                val longitude: Double,val latitude: Double, val createur: String, val isValid: Boolean)