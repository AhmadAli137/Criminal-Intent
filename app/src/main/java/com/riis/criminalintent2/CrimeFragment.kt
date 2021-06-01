package com.riis.criminalintent2

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import java.util.*

private const val TAG = "CrimeFragment"
private const val ARG_CRIME_ID = "crime_id"

//CrimeFragment is a fragment which controls the detail view when hosted by MainActivity
//It is also the controller that interacts with the Crime model objects and the view objects
class CrimeFragment : Fragment() {

    //DEFINING CLASS VARIABLES
    private lateinit var crime: Crime
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox

    //ASSOCIATING THE FRAGMENT WITH THE viewModel CrimeDetailViewModel.kt
    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProvider(this).get(CrimeDetailViewModel::class.java)
    }

    //WHAT HAPPENS WHEN THE FRAGMENT CrimeFragment IS CREATED
    override fun onCreate(savedInstanceState: Bundle?) { //passes information saved in the bundle or null if empty
        super.onCreate(savedInstanceState) //creates the fragment using the info passed to savedInstanceState
        crime = Crime() //an empty crime object is created from the Crime.kt class

        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID //retrieving the crimeId from the fragment's arguments bundle
        crimeDetailViewModel.loadCrime(crimeId) //load the retrieved crimeId into the viewModel to send a query for the corresponding Crime object in the database

    }

    //WHAT HAPPENS WHEN THE FRAGMENT VIEW made using fragment_crime.xml IS CREATED
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //defining and inflating the fragment view which is then hosted in an activity's container view
        //The third parameter tells the layout inflater whether to immediately add the inflated view to the view’s parent
        val view = inflater.inflate(R.layout.fragment_crime, container, false)

        //referencing views from fragment_crime.xml using their View IDs
        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox

        dateButton.apply {
            text = crime.date.toString()//sets the button text to the crime object's date attribute
            isEnabled = false//disables the button, it will not respond to being pressed
        }
        return view
    }

    //WHEN A NEW VIEW IS CREATED, OBTAIN THE UPDATED CRIME OBJECT
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeDetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner,
            Observer { crime ->
                crime?.let {
                    this.crime = crime
                    updateUI()
                }
            })
    }


    //WHAT HAPPENS WHENEVER THE APP STARTS UP
    override fun onStart() {
        super.onStart()

        //EditText Listener
        val titleWatcher = object : TextWatcher { //using TextWatcher listener to react to user input
            override fun beforeTextChanged(
                sequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // This space intentionally left blank (not interested in this function)
            }
            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                crime.title = sequence.toString() //setting the title attribute of the crime object to the new sequence
            }
            override fun afterTextChanged(sequence: Editable?) {
                // This one too (not interested in this function)
            }
        }
        titleField.addTextChangedListener(titleWatcher)//whenever the text of titleField changes, titleWatcher reacts accordingly

        //CheckBox Listener
        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked //sets isSolved attribute of the crime object to the boolean isChecked (T/F)
            }
        }
    }

    //WHEN THE CRIME OBJECT HAS BEEN UPDATED, UPDATE THE UI VIEWS
    private fun updateUI() {
        titleField.setText(crime.title)
        dateButton.text = crime.date.toString()
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState() //skips the checkbox animation
        }
    }

    //WHEN THE FRAGMENT HAS BEEN STOPPED
    //happens anytime the fragment moves entirely out of view
    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime) //saves the data of the current Crime object being accessed by the user before stopping the fragment
    }



    //FUNCTION THAT ACTIVITIES CAN CALL TO GET AN INSTANCE OF THE FRAGMENT
    //--------------------------------------------------------------------
    companion object {
        fun newInstance(crimeId: UUID): CrimeFragment { //a crimeId is given to a new instance of the CrimeFragment
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId) //the crimeId along with a string is saved to the fragment's arguments bundle as an argument (key-value pair)
            }
            return CrimeFragment().apply { //attaching the arguments bundle to the fragment
                arguments = args
            }
        }
    }



}