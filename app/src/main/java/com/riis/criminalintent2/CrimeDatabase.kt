package com.riis.criminalintent2

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

//@Database annotation tells Room that this class defines a database
//@TypeConverters annotation allows CrimeDatabase.kt to use the functions from CrimeTypeConverters.kt when converting types

// Class parameter 1: list of entity classes
// Class parameter 2: database version which increments when the database properties are changed

@Database(entities = [ Crime::class ], version=2)
@TypeConverters(CrimeTypeConverters::class)
abstract class CrimeDatabase : RoomDatabase() {

    //when this function is called, a reference to the DAO from CrimeDAO.kt is created
     abstract fun crimeDao(): CrimeDao
}

//Migration object that contains instructions for updating your database from version 1 to 2
val migration_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        //adding  a new column in the table to hold the "suspect" attribute string
        database.execSQL(
            "ALTER TABLE Crime ADD COLUMN suspect TEXT NOT NULL DEFAULT ''"
        )
    }
}
