package com.example.locbatt

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.locbatt.databinding.FragmentTeacherLoginBinding
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TeacherLogin.newInstance] factory method to
 * create an instance of this fragment.
 */
@Suppress("DEPRECATION")
class TeacherLogin : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var binding : FragmentTeacherLoginBinding
    lateinit var loadingpanel1 : ConstraintLayout
    lateinit var email : EditText
    lateinit var password : EditText
    lateinit var logInBtn : Button
    lateinit var mAuth : FirebaseAuth
    lateinit var model : GeneralCommunicator
    lateinit var forgetPass : TextView
    lateinit var nav : BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  DataBindingUtil.inflate(inflater,R.layout.fragment_teacher_login, container, false)
        email = binding.simpleEditText
        password = binding.simpleEditText3
        logInBtn = binding.button
        forgetPass = binding.forgetPass
        nav = binding.nav4
        loadingpanel1 = binding.loadingpanel1
        model = ViewModelProvider(activity!!).get(GeneralCommunicator::class.java)
        mAuth = FirebaseAuth.getInstance()
        logInBtn.setOnClickListener { view: View ->
            if(email.text.toString().isNotEmpty()&&
                password.text.toString().isNotEmpty()){
                loadingpanel1.visibility = View.VISIBLE
                val emailA = email.text.toString().trim()
                val pass = password.text.toString().trim()
                val refTeacher = FirebaseDatabase.getInstance().getReference("Teacher")
                if (emailA.isNotEmpty() && pass.isNotEmpty()) {
                    this.mAuth.signInWithEmailAndPassword(emailA, pass)
                        .addOnCompleteListener { task: Task<AuthResult> ->
                            if (task.isSuccessful) {
                                refTeacher.orderByChild("email").equalTo(emailA).addValueEventListener(
                                    object : ValueEventListener {
                                        override fun onDataChange(p0: DataSnapshot) {
                                            if (p0.exists()) {
                                                if (findNavController().currentDestination?.id == R.id.teacher_login) {
                                                    model.setMsgCommunicator(emailA)
                                                    val myFragment = TeacherHomePage()
                                                    val fragmentTransaction =
                                                        fragmentManager!!.beginTransaction()
                                                    fragmentTransaction.replace(
                                                        R.id.myNavHostFragment,
                                                        myFragment
                                                    )
                                                    fragmentTransaction.addToBackStack(null)
                                                    fragmentTransaction.commit()
                                                    view.findNavController()
                                                        .navigate(R.id.action_teacher_login_to_teacherHomePage)
                                                }
                                            } else {
                                                this@TeacherLogin.loadingpanel1.visibility = View.GONE
                                                email.setError("Teacher Email Not Exists")
                                                password.setError("Teacher Email Not Exists")
//                                            val builder = AlertDialog.Builder(context)
//                                            builder.setTitle("ERROR")
//                                            builder.setMessage("Teacher Email Not Exists")
//                                            builder.setPositiveButton("Ok") { dialog, which ->
//
//                                            }
//                                            val alert = builder.create()
//                                            alert.show()
                                            }
                                        }

                                        override fun onCancelled(p0: DatabaseError) {
                                        }
                                    }
                                )
                            } else {
                                this.loadingpanel1.visibility = View.GONE
//                            val builder = AlertDialog.Builder(context)
//                            builder.setTitle("ERROR")
//                            builder.setMessage("Incorrect Credentials")
//                            builder.setPositiveButton("Ok") { dialog, which ->
//
//                            }
//                            val alert = builder.create()
//                            alert.show()
                                email.setError("Incorrect Credentials")
                                password.setError("Incorrect Credentials")
                            }
                        }
                }
            }else{
                email.setError("Please enter teacher email")
                password.setError("Please enter teacher password")
            }
        }

        forgetPass.setOnClickListener{view : View->
            var bundle: Bundle = bundleOf("Type" to "Teacher")
            view.findNavController().navigate(R.id.action_teacher_login_to_resetPassword, bundle)
        }
        nav.setOnNavigationItemReselectedListener {
                item->
            when(item.itemId){
                R.id.backHome->{
                    if(findNavController().currentDestination?.id == R.id.teacher_login){
                        findNavController().navigate(R.id.mainPage)
                    }
                }
            }
        }

        return binding.root
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
         * @return A new instance of fragment TeacherLogin.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TeacherLogin().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}