package com.riis.criminalintent2

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

//CREATING THE MODEL LAYER FOR THE APP

//Crime class creates Crime objects which each hold information about a crime

// @Entity used to define Crime class as Room entity
// defines the structure of a table or set of tables in a database

@Entity
data class Crime(@PrimaryKey val id: UUID = UUID.randomUUID(), //random unique id, primary key
                 var title: String = "", //descriptive title
                 var date: Date = Date(), //date
                 var isSolved: Boolean = false, //boolean indicating whether or not the crime is solved
                 var suspect: String = "") //name of suspect

// Each crime object represents a row and each attribute represents a column in a database
// Each entry in a database is distinguished by its primary key
