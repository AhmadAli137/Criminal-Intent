package com.riis.criminalintent2

import androidx.room.TypeConverter
import java.util.*


//Room does not know how to store non-primitive types such as the Data and UUID objects in the Crime class
//A type converter tells Room how to convert a specific type to the format it needs (primitive) to store in the database
//...It also tells Room how to convert the database representation of a type back to its original form

class CrimeTypeConverters {
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time //Date --> Long
    }
    @TypeConverter
    fun toDate(millisSinceEpoch: Long?): Date? {
        return millisSinceEpoch?.let {
            Date(it) //Long --> Date
        }
    }
    @TypeConverter
    fun toUUID(uuid: String?): UUID? {
        return UUID.fromString(uuid) //String --> UUID
    }
    @TypeConverter
    fun fromUUID(uuid: UUID?): String? {
        return uuid?.toString() //UUID --> String
    }
}
