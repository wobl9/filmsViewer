package ru.wobcorp.filmsviewer.presentation.filmslist.di

import android.content.Context
import androidx.lifecycle.ViewModel
import com.chuckerteam.chucker.api.ChuckerInterceptor
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.wobcorp.filmsviewer.data.api.FilmsApiService
import ru.wobcorp.filmsviewer.data.mappers.FilmsMapper
import ru.wobcorp.filmsviewer.data.mappers.FilmsMapperImpl
import ru.wobcorp.filmsviewer.data.repositories.FilmsRepository
import ru.wobcorp.filmsviewer.data.repositories.FilmsRepositoryImpl
import ru.wobcorp.filmsviewer.di.ViewModelModule.ViewModelKey
import ru.wobcorp.filmsviewer.domain.FilmModel
import ru.wobcorp.filmsviewer.presentation.filmslist.FilmsListViewModelImpl
import ru.wobcorp.filmsviewer.presentation.filmslist.FilmsRequestFactory
import ru.wobcorp.filmsviewer.utils.pagination.RequestFactory

@Module
class FilmsListNetwork {
    @Provides
    fun provideHttp(client: OkHttpClient): FilmsApiService {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .baseUrl(BASE_URL)
            .build()
            .create(FilmsApiService::class.java)
    }

    @Provides
    fun provideClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(ChuckerInterceptor(context))
            .build()
    }

    @Provides
    fun provideRequestFactory(repository: FilmsRepository): RequestFactory<FilmModel> {
        return FilmsRequestFactory(repository)
    }

    companion object {
        const val BASE_URL = "https://api.themoviedb.org/"
    }
}

@Module
abstract class FilmsListDataModule {
    @Binds
    abstract fun provideMapper(mapper: FilmsMapperImpl): FilmsMapper

    @Binds
    abstract fun provideRepository(repository: FilmsRepositoryImpl): FilmsRepository
}

@Module
abstract class FilmsListPresentationModule {

    @Binds
    @IntoMap
    @ViewModelKey(FilmsListViewModelImpl::class)
    abstract fun provideViewModel(viewModelImpl: FilmsListViewModelImpl): ViewModel
}