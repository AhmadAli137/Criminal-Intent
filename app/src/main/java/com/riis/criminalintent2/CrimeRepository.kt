package com.riis.criminalintent2

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "crime-database"
private const val DATABASE_DIR = "databases/crime-database.db"

class CrimeRepository private constructor(context: Context) {

    //creates a concrete database using three parameters"
    private val database : CrimeDatabase = Room.databaseBuilder(
        context.applicationContext, //application context is provided since database needs access to the filesystem throughout the application lifecycle
        CrimeDatabase::class.java, //the database class being created
        DATABASE_NAME //name of database
    ).createFromAsset(DATABASE_DIR).build()
    private val crimeDao = database.crimeDao() //referencing the DAO

    //An Executor is an object that references a thread
    private val executor = Executors.newSingleThreadExecutor() //returns an executor instance that points to a new thread

    //Including a function in the repository to receive the LiveData provided by each function in the DAO
    //Room automatically executes database queries on a background thread so LiveData is used to bring the data to the main thread
    fun getCrimes(): LiveData<List<Crime>> = crimeDao.getCrimes()
    fun getCrime(id: UUID): LiveData<Crime?> = crimeDao.getCrime(id)

    //Using the Executor to safely perform database operations on a new background thread
    fun updateCrime(crime: Crime) {
        executor.execute {
            crimeDao.updateCrime(crime)
        }
    }
    fun addCrime(crime: Crime) {
        executor.execute {
            crimeDao.addCrime(crime)
        }
    }


    companion object { //allows other classes to call upon the CrimeRepository class without having to create an actual instance of it
        private var INSTANCE: CrimeRepository? = null //creating a variable to contain an instance of CrimeRepository

        //If INSTANCE is null, set it to contain a new instance of CrimeRepository that is initialized with a provided Context
        //In this case, the context is lifecycle information of the application itself from CriminalIntentApplication.kt
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = CrimeRepository(context)
            }
        }
        fun get(): CrimeRepository {
            return INSTANCE ?: //returns an instance of the CrimeRepository
            throw IllegalStateException("CrimeRepository must be initialized") //throws an exception if CrimeRepository is null
        }
    }

}
