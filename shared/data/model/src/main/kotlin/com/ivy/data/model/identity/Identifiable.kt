package com.ivy.data.model.identity

interface Identifiable<ID : UniqueId> {
    val id: ID
}
