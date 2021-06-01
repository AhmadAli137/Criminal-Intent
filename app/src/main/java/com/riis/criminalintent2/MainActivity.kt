package com.riis.criminalintent2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import java.util.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), CrimeListFragment.Callbacks {

    //CREATING THE ACTIVITY
    //---------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) //creating a new activity using whatever data is stored in savedInstanceState
        setContentView(R.layout.activity_main) //setting content to be displayed in activity_main.xml

        //asking the FragmentManager for the current fragment being used in the fragment_container
        val currentFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container)

        //if there is no current fragment, create a new fragment from CrimeListFragment.kt
        if (currentFragment == null) {
            val fragment = CrimeListFragment.newInstance()

            //Create a new fragment transaction, include one add operation in it, and then commit it
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, fragment)//adds the fragment to the container
                .commit()
        }
    }

    //CALLBACK FUNCTION FOR WHEN A CRIME IS SELECTED IN THE CrimeListFragment.kt FRAGMENT
    override fun onCrimeSelected(crimeId: UUID) {
        //Log.d(TAG, "MainActivity.onCrimeSelected: $crimeId") //logging the crime that was selected

        //replacing the current fragment with the CrimeFragment.kt fragment
        val fragment = CrimeFragment.newInstance(crimeId)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null) //the back stack state can be named, but is not necessary so String name = null
            .commit()
        //.addToBackStack ensures that the user is returned to CrimeListFragment if they exit out of CrimeFragment
    }
}