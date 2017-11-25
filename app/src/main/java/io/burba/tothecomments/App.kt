package io.burba.tothecomments

import android.app.Application
import com.facebook.stetho.Stetho
import com.jakewharton.threetenabp.AndroidThreeTen
import com.squareup.leakcanary.LeakCanary

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return
        }
        LeakCanary.install(this)

        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this)
        }

        AndroidThreeTen.init(this)
    }
}