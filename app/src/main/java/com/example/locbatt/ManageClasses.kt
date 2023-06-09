
package com.example.locbatt

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.locbatt.databinding.FragmentManageClassesBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ManageClasses.newInstance] factory method to
 * create an instance of this fragment.
 */
@Suppress("DEPRECATION")
class ManageClasses : Fragment(), OnMapReadyCallback {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var binding : FragmentManageClassesBinding
    private lateinit var addClass : Button
    private lateinit var courseName : EditText
    private lateinit var addBtn : Button
    private lateinit var backBtn : Button
    private lateinit var deleteBtn : Button
    private lateinit var seeResulBtn : Button
    private lateinit var courseID : TextView
    private lateinit var studentList : RecyclerView
    private lateinit var courseDescription : EditText
    private lateinit var courseNameToDisplay : TextView
    private lateinit var courseDescriptionToDisplay : TextView
    private lateinit var showAttendance : Button
    private lateinit var databaseReference: DatabaseReference
    lateinit var form: ConstraintLayout
    lateinit var courseInfo: ConstraintLayout
    private var courseDescriptionString : String?=null
    private var courseNameString : String?= null
    private lateinit var timer : TextView
    private var professorName : String?=null
    private var id : String?=null
    private var arrayList = ArrayList<Student>()
    private lateinit var startAttendance : Button
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var btnNav : BottomNavigationView

    private lateinit var mMap: GoogleMap

    private lateinit var mCircle: Circle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("UseRequireInsteadOfGet", "FragmentLiveDataObserve")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_manage_classes, container, false )
//        addClass = binding.button4
        courseName = binding.editText
        addBtn = binding.button5
//        deleteBtn = binding.button7
        courseDescription = binding.editText6
//        backBtn = binding.button6
        courseNameToDisplay = binding.textView9
        studentList = binding.studentList
        btnNav = binding.bottomNavigationView2
        courseDescriptionToDisplay = binding.textView10
        startAttendance = binding.attendance
        seeResulBtn = binding.seeResult
//        showAttendance = binding.button2
        timer = binding.timer
        courseID = binding.courseID
        form = binding.form
        courseInfo = binding.courseInfo
        seeResulBtn.visibility = View.GONE

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)





//        addClass.setOnClickListener {
//
//        }
        courseNameString = courseName.text.toString()
//        courseIdString = courseId.text.toString()
        courseDescriptionString = courseDescription.text.toString()
        val model = ViewModelProvider(activity!!).get(GeneralCommunicator::class.java)

        model.message.observe(this,object: Observer<Any> {
            override fun onChanged(t: Any?) {

                professorName = t!!.toString()
                Log.d("Professor name is " , professorName!!)

            }})

        model.id.observe(this,object : Observer<Any>{
            override fun onChanged(t: Any?) {
                id = t!!.toString()
                Log.d("the id is is is ", id.toString())

                if(id == "-1.0"){
                    startAttendance.visibility = View.GONE
                }else{
                    startAttendance.visibility = View.VISIBLE
                }

                if(id!="-1.0") {
                    val ordersRef =
                        FirebaseDatabase.getInstance().getReference("Course")
                            .orderByChild("courseId")
                            .equalTo(id!!)
                    val valueEventListener = object : ValueEventListener {
                        override fun onDataChange(p0: DataSnapshot) {
                            for (ds in p0.children) {
                                Log.d("loooooooool", ds.toString())
                                val courseName = ds.child("courseName").getValue(String::class.java)
                                val courseDescription =
                                    ds.child("courseDescription").getValue(String::class.java)
                                val courseIDS = ds.child("courseId").getValue(String::class.java)
                                courseID.text = "Course ID:  " + courseIDS
                                courseNameToDisplay.text = courseName
                                courseDescriptionToDisplay.text = "Description:  " + courseDescription
                            }
                        }

                        override fun onCancelled(p0: DatabaseError) {}
                    }
                    ordersRef.addValueEventListener(valueEventListener)
                }
                getkey(id!!)
                startAttendanceFun(id!!)
                seeResult(id!!)
                //showAttendance(id!!)

                btnNav.setOnNavigationItemReselectedListener { item->
                    when(item.itemId) {
                        R.id.backHome1 -> {
                            if (findNavController().currentDestination?.id == R.id.manageClasses) {
                                sendTeacherNameBackHome()
                                findNavController().navigate(R.id.action_manageClasses_to_teacherHomePage)
                            }
                        }
                        R.id.seeAttendance -> {
                            if(findNavController().currentDestination?.id == R.id.manageClasses){
                                val bundle : Bundle = bundleOf("courseId" to id!!, "professorName" to professorName)
                                findNavController().navigate(R.id.action_manageClasses_to_attendanceRecord,bundle)
                            }
                        }

                        R.id.add1 -> {
                            form.visibility =View.VISIBLE
                            courseInfo.visibility = View.GONE
//                            courseInfo.visibility = View.GONE
                            studentList.visibility = View.GONE
//                            showAttendance.visibility = View.GONE
                            startAttendance.visibility = View.GONE
                        }
                        R.id.delete1 -> {
                            if (id == "-1.0") {

                            } else {
                                val builder = AlertDialog.Builder(context)
                                builder.setTitle("Deleted")
                                builder.setMessage("This course is successfully deleted!")
                                builder.setIcon(R.drawable.sad)
                                builder.setPositiveButton("Ok") { dialog, which ->
                                    val refDelete =
                                        FirebaseDatabase.getInstance().getReference("Course")
                                    refDelete.child(id!!).removeValue()
                                    if (findNavController().currentDestination?.id == R.id.manageClasses) {
                                        sendTeacherNameBackHome()
                                        findNavController().navigate(R.id.teacherHomePage)
                                    }
                                    val resultRef =
                                        FirebaseDatabase.getInstance()
                                            .getReference("AttendanceResult")
                                    resultRef.addListenerForSingleValueEvent(
                                        object : ValueEventListener {
                                            override fun onCancelled(p0: DatabaseError) {}
                                            override fun onDataChange(p0: DataSnapshot) {
                                                for (e in p0.children) {
                                                    Log.d("e key key is ", e.key!!)

                                                    if (e.getValue(AttendanceResult::class.java)?.courseId == id) {
                                                        resultRef.child(e.key!!).removeValue()
                                                    }
                                                }
                                            }
                                        }
                                    )

                                    val refAttendanceIndicate = FirebaseDatabase.getInstance().getReference("AttendanceIndicator")
                                    refAttendanceIndicate.orderByChild("courseId").equalTo(id!!).addValueEventListener(
                                        object : ValueEventListener {
                                            override fun onCancelled(p0: DatabaseError) {}
                                            override fun onDataChange(p0: DataSnapshot) {
                                                if(p0.exists()){
                                                    for(e in p0.children){
                                                        refAttendanceIndicate.child(e.key!!).removeValue()
                                                    }
                                                }
                                            }
                                        }
                                    )
                                    val refRecord = FirebaseDatabase.getInstance().getReference("Record")
                                    refRecord.orderByChild("courseId").equalTo(id!!).addValueEventListener(
                                        object: ValueEventListener {
                                            override fun onCancelled(p0: DatabaseError) {}
                                            override fun onDataChange(p0: DataSnapshot) {
                                                if(p0.exists()){
                                                    for(e in p0.children){
                                                        refRecord.child(e.key!!).removeValue()
                                                    }
                                                }
                                            }
                                        }
                                    )

                                }
                                val alert = builder.create()
                                alert.show()
                            }
                        }
                    }
                }
            }

        }
        )
        addBtnFun()


        val builder = AlertDialog.Builder(context)


        studentList.addOnItemTouchListener(RecyclerItemClickListener(context!!, studentList, object : RecyclerItemClickListener.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val studentRef = FirebaseDatabase.getInstance().getReference("Student")
                studentRef.orderByChild("id").equalTo(StudentAdapter(arrayList).getCIN(position)).addValueEventListener(
                    object : ValueEventListener{
                        override fun onCancelled(p0: DatabaseError) {}
                        override fun onDataChange(p0: DataSnapshot) {
                            for(e in p0.children){

                                val student = e.getValue(Student::class.java)
                                val fullName = student?.firstName + " " + student?.lastName
                                builder.setTitle(student?.firstName + "' information")
                                builder.setMessage("Name: " + fullName + "\nCIN: " + student?.id)
//                                builder.setPositiveButton("Ok"){dialog, which ->
//
//                                }
                                val alert = builder.create()
                                alert.setIcon(R.drawable.studenticon)
                                alert.show()
                            }
                        }
                    }
                )

            }
            override fun onItemLongClick(view: View?, position: Int) {}
        }))

        // setHasOptionsMenu(true)

        return binding.root

    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap ?: return
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun getLocation(){
        mFusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        mFusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if(location!= null){
                    val teacherLocation = TeacherLocation(
                        arguments?.getString("courseId")!!,
                        location.longitude,
                        location.latitude,
                    )
                    Log.d("The Teacher Location is ",location.longitude.toString()+" - " +location.latitude.toString())
                    val teacherLocationRef = FirebaseDatabase.getInstance().getReference("TeacherLocation")
                    val key = teacherLocationRef.push().key!!
                    teacherLocationRef.child(key).setValue(teacherLocation)

                    if (::mMap.isInitialized) {
                        mMap.clear()
                        val latLng = LatLng(teacherLocation.latitude,teacherLocation.longtitude)
                        drawCircle(latLng)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))

                    }
                }
                else{
                    Toast.makeText(context!!,"Location is null", Toast.LENGTH_LONG).show()
                }


            }

    }
    private fun drawCircle(latLng: LatLng) {
        // Add a marker at the specified location
        val markerOptions = MarkerOptions().position(latLng).title("My Location")
        mMap.addMarker(markerOptions)

        val circleOptions = CircleOptions()
            .center(latLng)
            .radius(100.0) // radius in meters
            .strokeColor(Color.RED)
            .fillColor(Color.argb(70, 150, 50, 50))
        mMap.addCircle(circleOptions)
    }

    private fun seeResult(courseId: String){
        seeResulBtn.setOnClickListener {view : View->
            sendCourseId(courseId)
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun sendCourseId(courseId: String){
        if(findNavController().currentDestination?.id == R.id.manageClasses){
            val model = ViewModelProvider(activity!!).get(GeneralCommunicator::class.java)
            model.setIdCommunicator(courseId)
            val myFragment = SeeAttendanceResult()
            val fragmentTransaction = fragmentManager!!.beginTransaction()
            fragmentTransaction.replace(R.id.myNavHostFragment, myFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
            view!!.findNavController().navigate(R.id.action_manageClasses_to_seeAttendanceResult)
        }
    }

    private fun closeAttendanceFun(courseId: String){
        val attendanceIndicatorRef = FirebaseDatabase.getInstance().getReference("AttendanceIndicator")
        attendanceIndicatorRef.orderByChild("courseId").equalTo(courseId).addListenerForSingleValueEvent(
            object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.exists()){
                        for(e in p0.children){
                            Log.d("first key",e.key!!)
                            attendanceIndicatorRef.child(e.key!!).child("status").setValue(false)

                        }
                    }
                }

            }
        )

    }

    private fun startAttendanceFun(courseId : String) {
        startAttendance.setOnClickListener {
            startAttendance.visibility = View.GONE
            timer.visibility = View.VISIBLE
            seeResulBtn.visibility = View.GONE
            val ref = FirebaseDatabase.getInstance().getReference("TeacherLocation")
            ref.orderByChild("courseId").equalTo(courseId).addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()) {
                            ref.removeValue()
                            getLocation()
                        } else {
                            getLocation()
                        }
                    }

                    override fun onCancelled(p0: DatabaseError) {}
                }
            )
            val resultRef = FirebaseDatabase.getInstance().getReference("AttendanceResult")
            resultRef.addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {}
                    override fun onDataChange(p0: DataSnapshot) {
                        for (e in p0.children) {
                            Log.d("e key key is ", e.key!!)

                            if (e.getValue(AttendanceResult::class.java)?.courseId == courseId) {
                                resultRef.child(e.key!!).removeValue()
                            }
                        }
                    }
                }
            )
            // showAttendance.visibility = View.GONE
            studentList.visibility = View.GONE
            val attendanceIndicatorRef =
                FirebaseDatabase.getInstance().getReference("AttendanceIndicator")
            attendanceIndicatorRef.orderByChild("courseId").equalTo(courseId)
                .addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {}
                        override fun onDataChange(p0: DataSnapshot) {
                            if (p0.exists()) {
                                for (e in p0.children) {
                                    Log.d("first key", e.key!!)
                                    attendanceIndicatorRef.child(e.key!!).child("status")
                                        .setValue(true)

                                }
                            }
                        }

                    }
                )
            val timer = object : CountDownTimer(20000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timer.text =
                        "seconds remaining: \n                " + millisUntilFinished / 1000
                }

                override fun onFinish() {
                    closeAttendanceFun(courseId)
                    timer.visibility = View.GONE
                    seeResulBtn.visibility = View.VISIBLE
                    startAttendance.visibility = View.VISIBLE
                }
            }
            timer.start()
        }
    }

    private fun getkey(id : String) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Student")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {

                for (e in p0.children) {
                    Log.d("FIRST level key ", e.key!!)

                    databaseReference.child(e.key!!).child("courseId")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(p1: DataSnapshot) {
                                arrayList.clear()
                                for (e1 in p1.children) {
                                    Log.d("second level key ", e1.key!!)
                                    val query = databaseReference.orderByChild("courseId/" + e1.key)
                                        .equalTo(id)
                                    query.addListenerForSingleValueEvent(
                                        object : ValueEventListener {
                                            override fun onDataChange(p2: DataSnapshot) {

                                                if (p2.exists()) {

                                                    for (e2 in p2.children) {
                                                        Log.d("helloooo", e2.getValue().toString())
                                                        val student =
                                                            e2.getValue(Student::class.java)
                                                        arrayList.add(student!!)

                                                    }
                                                    for (i in arrayList) {
                                                        Log.d("The array is ", i.firstName)
                                                    }
                                                    val adapter = StudentAdapter(arrayList)
                                                    studentList.adapter = adapter
                                                }
                                            }

                                            override fun onCancelled(p2: DatabaseError) {
                                            }
                                        }
                                    )

                                }
                            }

                            override fun onCancelled(p0: DatabaseError) {
                            }


                        })

                }
            }


        })
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun sendTeacherNameBackHome(){

        val model = ViewModelProvider(activity!!).get(GeneralCommunicator::class.java)
        val user = FirebaseAuth.getInstance().currentUser
        Log.d("auth user current", user?.email!!)
        model.setMsgCommunicator(user.email.toString())
        val myFragment = TeacherHomePage()
        val fragmentTransaction = fragmentManager!!.beginTransaction()
        fragmentTransaction.replace(R.id.myNavHostFragment,myFragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    private fun addBtnFun(){
        addBtn.setOnClickListener {view : View->
            studentList.visibility = View.VISIBLE
//            form.visibility = View.GONE
//            courseInfo.visibility = View.VISIBLE
            val ref = FirebaseDatabase.getInstance().getReference("Course")
            val attendanceIndicatorRef = FirebaseDatabase.getInstance()
                .getReference("AttendanceIndicator")
            val key = attendanceIndicatorRef.push().key
            val cId = ref.push().key!!
            val attendanceIndicatorObject = AttendanceIndicator(cId, false)
            attendanceIndicatorRef.child(key!!).setValue(attendanceIndicatorObject)
            val course = Course(courseName.text.toString(),cId,courseDescription.text.toString(),professorName!!)
            ref.child(cId).setValue(course)
            Toast.makeText(context, "add class successfully", Toast.LENGTH_LONG).show()
            if(findNavController().currentDestination?.id == R.id.manageClasses) {
                sendTeacherNameBackHome()
                view.findNavController().navigate(R.id.teacherHomePage)
            }
        }
    }

    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
    }
    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ManageClasses.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ManageClasses().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


}

private fun SupportMapFragment.getMapAsync(manageClasses: ManageClasses) {

}
