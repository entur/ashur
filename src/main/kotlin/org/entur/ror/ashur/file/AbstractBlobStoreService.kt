package org.entur.ror.ashur.file

import org.rutebanken.helper.storage.repository.BlobStoreRepository
import java.io.InputStream

abstract class AbstractBlobStoreService protected constructor(
    val containerName: String?,
    protected val repository: BlobStoreRepository
) {
    init {
        this.repository.setContainerName(containerName)
    }

    fun exists(name: String?): Boolean {
        return repository.exist(name)
    }

    fun getBlob(name: String): InputStream? {
        return repository.getBlob(name)
    }

    fun uploadBlob(name: String, inputStream: InputStream) {
        repository.uploadBlob(name, inputStream)
    }
}
