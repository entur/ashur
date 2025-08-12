package org.entur.ror.ashur.gcp

import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions

open class GcsClient(
    val storage: Storage = StorageOptions.getDefaultInstance().service
)