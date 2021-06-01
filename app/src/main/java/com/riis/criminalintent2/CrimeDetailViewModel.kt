package com.riis.criminalintent2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.util.*

//viewModel which handles database queries for CrimeFragment.kt
//
class CrimeDetailViewModel() : ViewModel() {
    private val crimeRepository = CrimeRepository.get() //getting an instance of the CrimeRepository
    private val crimeIdLiveData = MutableLiveData<UUID>() //variable which stores the crimeId that will be loaded by CrimeFragment
                                                          //the crimeId is wrapped in LiveData so that the Transformation statement below can listen to changes to it

    /*NOTE: live data transformation
    --------------------------------
    --> is a way to set up a trigger-response relationship between two LiveData objects
    --> input 1: LiveData object used as a trigger
    --> input 2: mapping function that must return a new LiveData object
    --> The transformation function returns a new LiveData object (called the transformation result),
        ... whose value gets updated every time a new value gets set on the trigger LiveData instance
    --> This creates a live data stream which only has to be observed once
    */

    //crimeLiveData exposes an updated Crime object each time crimeIdLiveData is updated with a new crimeId
    var crimeLiveData: LiveData<Crime?> = //Crime object is the transformation result
        Transformations.switchMap(crimeIdLiveData) { crimeId -> //crimeId is the trigger
            crimeRepository.getCrime(crimeId) //getCrime() is the mapping function
        }

    //function called upon by CrimeFragment to load the current crimeId
    fun loadCrime(crimeId: UUID) {
        crimeIdLiveData.value = crimeId //Sets the value. If there are active observers, the value will be dispatched to them.
    }

    //function called upon by CrimeFragment to update the current Crime object being accessed by the user
    fun saveCrime(crime: Crime) {
        crimeRepository.updateCrime(crime)
    }
}
