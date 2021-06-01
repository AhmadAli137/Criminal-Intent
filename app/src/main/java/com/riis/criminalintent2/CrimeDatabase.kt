package com.riis.criminalintent2

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

//@Database annotation tells Room that this class defines a database
//@TypeConverters annotation allows CrimeDatabase.kt to use the functions from CrimeTypeConverters.kt when converting types

// Class parameter 1: list of entity classes
// Class parameter 2: database version which increments when the database properties are changed

@Database(entities = [ Crime::class ], version=1)
@TypeConverters(CrimeTypeConverters::class)
abstract class CrimeDatabase : RoomDatabase() {

    //when this function is called, a reference to the DAO from CrimeDAO.kt is created
     abstract fun crimeDao(): CrimeDao
}
