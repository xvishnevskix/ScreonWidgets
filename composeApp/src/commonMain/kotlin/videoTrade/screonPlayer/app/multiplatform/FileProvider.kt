/*
 * Copyright (c) LLC "Centr Distribyucii"
 * All rights reserved.
 */
package videoTrade.screonPlayer.app.multiplatform

import videoTrade.screonPlayer.app.domain.model.FileItem
import videoTrade.screonPlayer.app.domain.model.VideoItem

expect class FileProvider {


    suspend fun downloadVideoById(
        fileItem: FileItem,
        saveToFolder: String = "videos_downloaded",
        onProgress: (Float) -> Unit

    ): VideoItem?

}

expect object FileProviderFactory {
    fun create(): FileProvider
}

