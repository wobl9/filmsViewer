package ru.wobcorp.filmsviewer.presentation.filmslist.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import ru.wobcorp.filmsviewer.di.ViewModelModule
import ru.wobcorp.filmsviewer.presentation.filmslist.FilmsListFragment
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class FilmsListScope

@FilmsListScope
@Component(
    modules = [
        FilmsListNetwork::class,
        FilmsListDataModule::class,
        FilmsListPresentationModule::class,
        ViewModelModule::class
    ]
)
interface FilmsListComponent {

    fun inject(fragment: FilmsListFragment)

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance context: Context
        ): FilmsListComponent
    }
}