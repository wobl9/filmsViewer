package ru.wobcorp.filmsviewer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commitNow
import ru.wobcorp.filmsviewer.presentation.filmslist.FilmsListFragment
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Timber.plant(Timber.DebugTree())
        if (savedInstanceState == null) {
            supportFragmentManager.commitNow {
                replace(R.id.main_container, FilmsListFragment())
            }
        }
    }
}