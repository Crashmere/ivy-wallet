package com.ivy.legacy.ui.theme.system

import androidx.compose.foundation.shape.CornerBasedShape
import com.ivy.legacy.ui.theme.LegacyShapes

internal abstract class IvyShapes : LegacyShapes {
    abstract override val r1: CornerBasedShape
    abstract override val r1Top: CornerBasedShape
    abstract override val r1Bot: CornerBasedShape

    abstract override val r2: CornerBasedShape
    abstract override val r2Top: CornerBasedShape
    abstract override val r2Bot: CornerBasedShape

    abstract override val r3: CornerBasedShape
    abstract override val r3Top: CornerBasedShape
    abstract override val r3Bot: CornerBasedShape

    abstract override val r4: CornerBasedShape
    abstract override val r4Top: CornerBasedShape
    abstract override val r4Bot: CornerBasedShape
}
