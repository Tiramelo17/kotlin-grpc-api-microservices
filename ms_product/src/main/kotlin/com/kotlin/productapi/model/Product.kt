package com.kotlin.productapi.model

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
data class Product (
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val id : Long = 0,
    var name: String,
    var price: Double,
    var stock: Int,
    var description: String){
}