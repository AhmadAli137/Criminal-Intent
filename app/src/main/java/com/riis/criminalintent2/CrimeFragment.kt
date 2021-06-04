package com.riis.criminalintent2

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
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
import android.text.format.DateFormat
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import java.util.*

private const val TAG = "CrimeFragment"
private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = 0
private const val REQUEST_CONTACT = 1
private const val DATE_FORMAT = "EEE, MMM, dd"

//CrimeFragment is a fragment which controls the detail view when hosted by MainActivity
//It is also the controller that interacts with the Crime model objects and the view objects
class CrimeFragment : Fragment(),  DatePickerFragment.Callbacks {

    //DEFINING CLASS VARIABLES
    private lateinit var crime: Crime
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var pickContactContract: ActivityResultContract<Uri, Uri?>
    private lateinit var pickContactCallback: ActivityResultCallback<Uri?>
    private lateinit var pickContactLauncher: ActivityResultLauncher<Uri>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

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

        //============================Using Activity Result APIs to get results from an Android activity==========================================================

        /* NOTE: To start an activity and receive a result back, we will use the Activity Result APIs.
           The Activity Result APIs provide components for registering for a result, launching the result,
           ... and handling the result once it is dispatched by the system.

           A Contract:
           --> defines an intent which will start an activity
           --> Receive the result intent from the started activity and parses it.

           We will be using ActivityResultContract<Uri, Uri?> to create a generic contract that starts a new activity,
           ... allows the user to pick an item from the activity's data, and return what was selected.

           The input is the URI containing a directory of data from which to pick an item. The output (can be null) is the
           ... URI of the item that was picked. A URI (Uniform resource identifier) is used to identify a resource */

        //Creating a generic pick contract
        pickContactContract = object : ActivityResultContract<Uri, Uri?>() {
            //creates an intent that starts an activity and allows the user to pick from the content found in the input Uri
            override fun createIntent(context: Context, input: Uri): Intent {
                return Intent(Intent.ACTION_PICK, input) //returns a result intent from the started activity or null
            }
            //if a result intent from the started activity is given, return the intent's Uri. Otherwise, return null.
            override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
                if(resultCode != Activity.RESULT_OK || intent == null)
                    return null
                return intent.data //The URI of the data this intent is targeting
            }
        }

        //ActivityResultCallback is A type-safe callback to be called when an activity result is available, it handles
        // ...what happens with the result after. In this case, the result is a Uri from the activity

        //creating a callback to handle the result of a pick contract
        pickContactCallback = ActivityResultCallback<Uri?> { contactUri: Uri? ->
            // Specify which fields you want your query to return values for
            val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
            // Perform your query - the contactUri is like a "where" clause here
            val cursor = contactUri?.let {
                requireActivity().contentResolver
                    .query(it, queryFields, null, null, null)
            }
            cursor?.use {
                // Verify cursor contains at least one result
                if (it.count > 0) {
                    // Pull out the first column of the first row of data - that is your suspect's name
                    it.moveToFirst()
                    val suspect = it.getString(0)
                    crime.suspect = suspect
                    crimeDetailViewModel.saveCrime(crime)
                    suspectButton.text = suspect
                }
            }
        }

        //Creating a launcher to handle the request for contacts and the result.
        // This step also registers the fragment with the ActivityManager as the listener
        pickContactLauncher = registerForActivityResult(pickContactContract, pickContactCallback)

        //Before launching the intent to get the contacts data, we should as the user for permission
        // ... which is a whole other intent and already exists in the Activity Results APIs

        //Creating a launcher to handle the request for permissions and the result. If permission is granted,
        // ... launch the contacts launcher
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission())
        { isGranted ->
            if(isGranted){
                pickContactLauncher.launch(ContactsContract.Contacts.CONTENT_URI)
            }
        }

        //=================================================================================================================================


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
        reportButton = view.findViewById(R.id.crime_report) as Button
        suspectButton = view.findViewById(R.id.crime_suspect) as Button

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

        //dateButton Listener
        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(crime.date).apply { //getting an instance of the date picker fragment
                setTargetFragment(this@CrimeFragment, REQUEST_DATE)
                show(this@CrimeFragment.parentFragmentManager, DIALOG_DATE) //Display the dialog, adding the fragment to the given FragmentManager.
            }
        }

        //reportButton Listener
        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply { //creating an Intent and giving it the action to deliver data to someone else
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport()) //getting crime report string and supplying it as the literal data to be sent
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    getString(R.string.crime_report_subject)) //supplying the subject line of message
            }.also { intent ->
                //Give user a choice for what app to use
                val chooserIntent = Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent) //start intent
            }
        }

        //suspectButton
        suspectButton.apply {
            setOnClickListener {
                //The permission is defined in the AndroidManifest.xml file
                requestPermissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
            }

            //Guarding against no contacts app
            val pickContactIntent = pickContactContract.createIntent(requireContext(), ContactsContract.Contacts.CONTENT_URI)

            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(pickContactIntent,
                    PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) {
                isEnabled = false
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

    //CREATING A CRIME REPORT USING FORMAT STRING
    //-------------------------------------------
    private fun getCrimeReport(): String {
        //1$s: crime title (string already known)

        //%2$s: crime date?
        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()

        //%3$s: crime solved?
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }
        //%4$s: crime suspect?
        var suspect = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }
        //R.string.crime_report: "%1$s! The crime was discovered on %2$s. %3$s, and %4$s"
        return getString(R.string.crime_report,
            crime.title, dateString, solvedString, suspect)
    }

    //WHEN THE FRAGMENT HAS BEEN STOPPED
    //----------------------------------
    //happens anytime the fragment moves entirely out of view
    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime) //saves the data of the current Crime object being accessed by the user before stopping the fragment
    }

    //updating the crime date from the callbacks interface in DatePickerFragment.kt
    override fun onDateSelected(date: Date) {
        crime.date = date
        updateUI()
    }



    //FUNCTION THAT ACTIVITIES CAN CALL TO GET AN INSTANCE OF THE FRAGMENT
    //--------------------------------------------------------------------
    companion object {
        fun newInstance(crimeId: UUID): CrimeFragment { //a crimeId is given to a new instance of the CrimeFragment
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId) //the crimeId along with a string is saved to the fragment's arguments bundle as an argument (key-value pair)
            }
            return CrimeFragment().apply { //attaching the bundle to the fragment's arguments
                arguments = args
            }
        }
    }



}