package com.ivy.data.model.primitive

import com.ivy.data.model.identity.UniqueId
import java.util.UUID

@JvmInline
value class AssociationId(override val value: UUID) : UniqueId
