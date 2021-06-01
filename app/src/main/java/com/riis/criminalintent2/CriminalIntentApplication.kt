package com.riis.criminalintent2

import android.app.Application

//CriminalIntentApplication is an Application subclass
//This gives the class access to the lifecycle information of the application itself

class CriminalIntentApplication : Application() {

    //When the application is created, it initializes the repository class in CrimeRepository.kt with the context from its application instance
    override fun onCreate() {
        super.onCreate()
        CrimeRepository.initialize(this)
    }
}

//The application instance is valid as long as the application process is in memory (destroyed only when application is destroyed),
// ... so it is safe to hold a reference to it in the repository class

//For this application class to work, it must be registered in the manifest
