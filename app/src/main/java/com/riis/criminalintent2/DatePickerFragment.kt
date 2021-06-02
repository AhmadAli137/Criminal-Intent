package com.riis.criminalintent2

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

private const val ARG_DATE = "date"

//DatePickerFragment is a fragment class that displays a dialog, particularly a date picker
//At first the dialog will appear with the current data and time, but the user can then interact with and change the dialog values
class DatePickerFragment : DialogFragment() {

    //CREATING A CALLBACKS INTERFACE
    //------------------------------
    interface Callbacks {
        fun onDateSelected(date: Date) //returns changes made to the crime date through the DatePicker dialog back to CrimeFragment.kt
    }

    //CREATING THE DIALOG
    //---------------------
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        //Listener for when the user has finished selecting a date
        val dateListener = DatePickerDialog.OnDateSetListener {
                _: DatePicker, year: Int, month: Int, day: Int ->
            //Creating a Date object from the values provided by the DatePicker
            val resultDate : Date = GregorianCalendar(year, month, day).time
            //getting the target fragment (fragment instance that started the DatePickerFragment), and passing it the new Date
            targetFragment?.let { fragment ->
                (fragment as Callbacks).onDateSelected(resultDate)
            }
        }

        val date = arguments?.getSerializable(ARG_DATE) as Date //getting the Date object sent to the bundle from CrimeFragment.kt
        val calendar = Calendar.getInstance() //returns a Calendar object with the default time zone and locale
        calendar.time = date //the Calendar object is now set to the give date
        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)
        return DatePickerDialog(
            requireContext(), //context fragment is currently using
            dateListener, //date listener
            initialYear,
            initialMonth,
            initialDay
        )
    }

    //GETTING AND INITIALIZING A NEW INSTANCE OF THE FRAGMENT
    //-------------------------------------------------------
    companion object {
        fun newInstance(date: Date): DatePickerFragment { //a Date object is given to a new instance of the DatePickerFragment
            val args = Bundle().apply {
                putSerializable(ARG_DATE, date) //the date along with a string is saved to the fragment's arguments bundle as an argument (key-value pair)
            }
            return DatePickerFragment().apply { //attaching the bundle to the fragment's arguments
                arguments = args
            }
        }
    }

}
