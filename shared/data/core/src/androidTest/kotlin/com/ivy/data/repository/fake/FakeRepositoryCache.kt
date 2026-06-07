package com.ivy.data.repository.fake

import com.ivy.data.DataWriteEventBus
import com.ivy.data.repository.RepositoryCacheFactory
import org.jetbrains.annotations.VisibleForTesting

@VisibleForTesting
fun fakeRepositoryCacheFactory(): RepositoryCacheFactory = RepositoryCacheFactory(
    dataChangePublisher = DataWriteEventBus(),
)
