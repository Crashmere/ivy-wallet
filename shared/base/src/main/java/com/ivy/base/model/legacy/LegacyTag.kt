package com.ivy.base.model.legacy

import java.util.UUID

@Deprecated("Use Tag Data Model")
@Suppress("DataClassTypedIDs")
data class LegacyTag(val id: UUID, val name: String)
