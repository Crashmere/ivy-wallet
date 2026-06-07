package com.ivy.data.api

import com.ivy.data.model.Tag
import com.ivy.data.model.TagAssociation
import com.ivy.data.model.TagId
import com.ivy.data.model.primitive.AssociationId

interface TagStore {
    suspend fun findById(id: TagId): Tag?

    suspend fun findByIds(ids: List<TagId>): List<Tag>

    suspend fun findByAssociatedId(id: AssociationId): List<Tag>

    suspend fun findByAssociatedId(ids: List<AssociationId>): Map<AssociationId, List<Tag>>

    suspend fun findAll(): List<Tag>

    suspend fun findByText(text: String): List<Tag>

    suspend fun findByAllAssociatedIdForTagId(
        tagIds: List<TagId>
    ): Map<TagId, List<TagAssociation>>

    suspend fun findByAllTagsForAssociations(): Map<AssociationId, List<TagAssociation>>

    suspend fun associateTagToEntity(associationId: AssociationId, tagId: TagId)

    suspend fun removeTagAssociation(associationId: AssociationId, tagId: TagId)

    suspend fun save(value: Tag)

    suspend fun deleteById(id: TagId)

    suspend fun deleteAll()
}
