package com.ivy.data.db.serializer

import androidx.annotation.Keep
import com.ivy.data.db.epochMilliToUtcLocalDateTime
import com.ivy.data.db.toUtcEpochMilli
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime

// Kept for legacy Room entities and backup JSON that still encode LocalDateTime as UTC epoch millis.
@Keep
object KSerializerLocalDateTime : KSerializer<LocalDateTime> {
    override val descriptor = PrimitiveSerialDescriptor(
        "LocalDateTime",
        PrimitiveKind.LONG
    )

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return decoder.decodeLong().epochMilliToUtcLocalDateTime()
    }

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeLong(value.toUtcEpochMilli())
    }
}
