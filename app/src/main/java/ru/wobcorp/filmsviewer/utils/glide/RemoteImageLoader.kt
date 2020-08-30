package ru.wobcorp.filmsviewer.utils.glide

import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.*
import com.bumptech.glide.load.model.stream.BaseGlideUrlLoader
import java.io.InputStream

class RemoteImageLoader(
    loader: ModelLoader<GlideUrl, InputStream>,
    modelCache: ModelCache<RemoteImage, GlideUrl>
) : BaseGlideUrlLoader<RemoteImage>(loader, modelCache) {

    override fun getUrl(model: RemoteImage, width: Int, height: Int, options: Options?): String =
        "$BASE_IMAGE_URL${model.path}"

    override fun handles(model: RemoteImage): Boolean = true

    companion object Factory : ModelLoaderFactory<RemoteImage, InputStream> {

        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<RemoteImage, InputStream> =
            RemoteImageLoader(
                multiFactory.build(GlideUrl::class.java, InputStream::class.java),
                ModelCache()
            )

        override fun teardown() = Unit

        private const val BASE_IMAGE_URL = "https://image.tmdb.org/t/p/w500/"
    }
}