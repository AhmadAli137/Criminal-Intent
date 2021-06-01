package com.riis.criminalintent2

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

private const val TAG = "CrimeListFragment"

//CrimeListFragment is a fragment which controls the recycler view, fragment_crime_list.xml, when hosted by MainActivity.kt
class CrimeListFragment : Fragment() {

    //REQUIRED INTERFACE FOR HOSTING ACTIVITIES TO USE
    //------------------------------------------------
    //The fragment can use this interface to call functions on its hosting activity, MainActivity.kt
    interface Callbacks {
        fun onCrimeSelected(crimeId: UUID) //function notifies the Callbacks interface with a crimeId from a Crime object
    }

    //DEFINING CLASS VARIABLES
    //------------------------
    private lateinit var crimeRecyclerView: RecyclerView
    private var adapter: CrimeAdapter? = CrimeAdapter(emptyList())
    private var callbacks: Callbacks? = null //holds Callbacks objects

    private val crimeListViewModel: CrimeListViewModel by lazy { //associating the fragment with the ViewModel CrimeListViewModel.kt
        ViewModelProvider(this).get(CrimeListViewModel::class.java)
    }

    //WHEN THE FRAGMENT IS FIRST ATTACHED TO ITS ACTIVITY
    //---------------------------------------------------
    override fun onAttach(context: Context) { //In this case the Context object is the activity instance hosting the fragment
        super.onAttach(context)
        callbacks = context as Callbacks? //storing context in the callbacks property
    }

    //WHEN THE FRAGMENT IS NO LONGER ATTACHED TO ITS PREVIOUSLY ATTACHED ACTIVITY
    //---------------------------------------------------------------------------
    override fun onDetach() {
        super.onDetach()
        callbacks = null //can no longer access activity, so don't call back
    }

    //WHAT HAPPENS WHEN THE FRAGMENT VIEW designed using fragment_crime_list.xml IS BEING CREATED
    //-------------------------------------------------------------------------------------------
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false) //inflating the view layout

        //referencing the RecyclerView from the layout fragment_crime_list.xml using its View ID
        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view) as RecyclerView

        //giving the RecyclerView a LayoutManager which is required to make it work
        //LinearLayoutManager positions the items in its list vertically on the screen
        crimeRecyclerView.layoutManager = LinearLayoutManager(context)

        crimeRecyclerView.adapter = adapter

        return view
    }

    //WHAT HAPPENS AFTER THE FRAGMENT VIEW IS COMPLETED
    //-------------------------------------------------
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        crimeListViewModel.crimeListLiveData.observe( //registering crimeListViewModel.crimeListLiveData as a LiveData observer
            viewLifecycleOwner, //scoping the observer to the life of the fragment’s view
            Observer { crimes -> //observer object reacts whenever the LiveData's list of Crimes is updated
                crimes?.let { //if the crimes list is not null ...
                    Log.i(TAG, "Got crimes ${crimes.size}") //logs size of crimes list
                    updateUI(crimes) //updates UI with the new crimes data
                }
            })
    }

    //UPDATING UI WHEN UI DATA CHANGES
    //--------------------------------
    private fun updateUI(crimes: List<Crime>) {//accepts list of Crime Objects as input

        adapter = CrimeAdapter(crimes) //creates an adapter which creates the necessary ViewHolders and populates their item views with
                                       //...the correct model layer data when asked by the RecyclerView

        crimeRecyclerView.adapter = adapter //Connecting the RecyclerView to the adapter
    }

    //CREATING AND IMPLEMENTING A ViewHolder TO WRAP THE ITEM VIEWS FOR A RecyclerView
    //--------------------------------------------------------------------------------
    private inner class CrimeHolder(view: View)
        : RecyclerView.ViewHolder(view),View.OnClickListener { //ViewHolder class will hold on to the provided view in a property named itemView

        private lateinit var crime: Crime

        //referencing child views in each item view defined by list_item_crime.xml using their view IDs
        private val titleTextView: TextView = itemView.findViewById(R.id.crime_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.crime_date)
        private val solvedImageView: ImageView = itemView.findViewById(R.id.crime_solved)


        init {
            itemView.setOnClickListener(this) //setting a click listener on each item view
        }

        //function used to tell the ViewHolder to bind to a crime
        fun bind(crime: Crime) {
            this.crime = crime //when a ViewHolder is given a crime to bind to, it updates its internal crime variable to become the input crime

            //setting the text for the child textViews of the itemView for this particular ViewHolder
            titleTextView.text = this.crime.title
            dateTextView.text = this.crime.date.toString()

            solvedImageView.visibility = if (crime.isSolved) { //setting the visibility of the child ImageView of the itemView
                View.VISIBLE                                   //...according to whether the itemView's Crime object's attribute, isSolved
            } else {
                View.GONE
            }
            //if a crime is solved, display the ImageView (image of handcuffs)
        }

        override fun onClick(v: View) {
            callbacks?.onCrimeSelected(crime.id) //when an ItemView on screen is clicked, send the selected crime's Crime ID to the Callbacks interface

        }

    }

    //CREATING AND IMPLEMENTING AN Adapter TO POPULATE THE RecyclerView
    //-----------------------------------------------------------------
    private inner class CrimeAdapter(var crimes: List<Crime>)//accepts and stores a list of Crime objects as input
        : RecyclerView.Adapter<CrimeHolder>() {

        //creates a viewHolder, inflates and wraps an item view in the view holder, and returns the viewHolder.
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
                : CrimeHolder {
            //inflates list_item_view.xml and passes the resulting view to a new instance of CrimeHolder
            val view = layoutInflater.inflate(R.layout.list_item_crime, parent, false)
            return CrimeHolder(view)
        }

        //returns the number of items in the list of crimes
        override fun getItemCount() = crimes.size

        //populates a given holder with the crime from a given position in the crimes list
        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = crimes[position] //selecting the crime
            holder.bind(crime)//binding the holder to the crime
        }
    }

    //FUNCTION THAT ACTIVITIES CAN CALL TO GET AN INSTANCE OF THE FRAGMENT
    //--------------------------------------------------------------------
    companion object {
        fun newInstance(): CrimeListFragment {
            return CrimeListFragment()
        }
    }
}