package com.ivy.data.store.fake

import com.ivy.data.DataWriteEventBus
import com.ivy.data.store.StoreCacheFactory
import org.jetbrains.annotations.VisibleForTesting

@VisibleForTesting
internal fun fakeStoreCacheFactory(): StoreCacheFactory = StoreCacheFactory(
    dataChangePublisher = DataWriteEventBus(),
)
