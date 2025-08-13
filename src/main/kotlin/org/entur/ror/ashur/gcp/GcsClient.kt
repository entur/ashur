package org.entur.ror.ashur.gcp

import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import org.springframework.stereotype.Component

@Component
open class GcsClient(
    val storage: Storage = StorageOptions.getDefaultInstance().service
)