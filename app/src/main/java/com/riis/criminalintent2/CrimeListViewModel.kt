package com.riis.criminalintent2

import androidx.lifecycle.ViewModel

//CrimeListViewModel is a ViewModel used to store a list of Crime objects for CrimeListFragment.kt
//this ViewModel is destroyed when the fragment from CrimeListFragment.kt is destroyed
class CrimeListViewModel : ViewModel() {

    private val crimeRepository = CrimeRepository.get() //getting an instance of the CrimeRepository
    val crimeListLiveData = crimeRepository.getCrimes() //getting a list of crimes from the CrimeRepository instance

    fun addCrime(crime: Crime) {
        crimeRepository.addCrime(crime) //adding a crime object to the CrimeRepository instance
    }


}