package com.riis.criminalintent2

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import java.util.*

//@Dao tells Room that CrimeDao is a data access object (DAO), which defines the functions (Queries)
//... which will be used to perform database operations
@Dao
interface CrimeDao {

    //Returning an instance of LiveData tells Room to run queries in a background thread
    //When the query completes, the LiveData object will handle sending the collected data
    //...over to the main thread and notify any observers

    @Query("SELECT * FROM crime")
    fun getCrimes(): LiveData<List<Crime>> //returns the whole list of Crime objects from the database

    @Query("SELECT * FROM crime WHERE id=(:id)")
    fun getCrime(id: UUID): LiveData<Crime?> //returns a Crime object from the database whose id matches the provided id

    @Update
    fun updateCrime(crime: Crime) //takes a Crime object and uses its Id to find the associated row in the database and update the data

    @Insert
    fun addCrime(crime: Crime) //adds a Crime object to a new row in the database
}