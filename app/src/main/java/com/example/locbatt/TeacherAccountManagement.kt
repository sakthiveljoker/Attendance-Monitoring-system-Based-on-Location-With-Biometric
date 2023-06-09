package com.example.locbatt

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.locbatt.databinding.FragmentTeacherAccountManagementBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TeacherAccountManagement.newInstance] factory method to
 * create an instance of this fragment.
 */
@Suppress("DEPRECATION")
class TeacherAccountManagement : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var binding : FragmentTeacherAccountManagementBinding
    private lateinit var enteredCurrentPass : EditText
    private lateinit var enteredNewPass: EditText
    private lateinit var enteredConfirm : EditText
    private lateinit var submit : Button
    private lateinit var auth : FirebaseAuth
    private lateinit var email : TextView
    private lateinit var nav : BottomNavigationView

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
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_teacher_account_management, container, false)
        enteredCurrentPass = binding.editText2
        enteredNewPass = binding.editText3
        enteredConfirm = binding.editText4
        submit = binding.button2
        auth = FirebaseAuth.getInstance()
        email = binding.textView
        nav = binding.bottomNavigationView
        submit.setOnClickListener { changePassword() }
        email.text = FirebaseAuth.getInstance().currentUser!!.email

        nav.setOnNavigationItemReselectedListener { item->
            when(item.itemId){
                R.id.backHome->{
                    if(findNavController().currentDestination?.id == R.id.teacherAccountManagement){
                        val model = ViewModelProvider(activity!!).get(GeneralCommunicator::class.java)
                        model.setNameCommunicator(FirebaseAuth.getInstance().currentUser?.email!!)
                        val myFragment = TeacherHomePage()
                        val fragmentTransaction =
                            fragmentManager!!.beginTransaction()
                        fragmentTransaction.replace(
                            R.id.myNavHostFragment,
                            myFragment
                        )
                        fragmentTransaction.addToBackStack(null)
                        fragmentTransaction.commit()
                        findNavController().navigate(R.id.action_teacherAccountManagement_to_teacherHomePage)
                    }
                }
            }
        }



        return binding.root
    }

    private fun changePassword(){

        if(enteredCurrentPass.text.isNotEmpty()&&
            enteredNewPass.text.isNotEmpty()&&
            enteredConfirm.text.isNotEmpty()){
            if(enteredNewPass.text.toString().equals(enteredConfirm.text.toString())){
                val user = auth.currentUser
                if(user!=null){
                    val credential = EmailAuthProvider
                        .getCredential(user.email!!, enteredCurrentPass.text.toString())
                    user?.reauthenticate(credential)?.addOnCompleteListener{
                        if(it.isSuccessful){
                            user?.updatePassword(enteredNewPass.text.toString())
                                ?.addOnCompleteListener{task->
                                    if(task.isSuccessful){
                                        Log.d("success", "password updated successfully")
                                        auth.signOut()
                                        if(findNavController().currentDestination?.id == R.id.teacherAccountManagement){
                                            findNavController().navigate(R.id.mainPage)
                                        }
                                    }
                                }
                        }
                    }
                }

            }else{
                enteredConfirm.setError("Password mismatching")

            }


        }else{
            if(enteredCurrentPass.text.isEmpty()){
                enteredCurrentPass.setError("Please enter current password.")
            }
            if(enteredNewPass.text.isEmpty()){
                enteredCurrentPass.setError("Please enter new password.")
            }
            if(enteredConfirm.text.isEmpty()){
                enteredConfirm.setError("Please re-enter password to confirm.")
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
         * @return A new instance of fragment TeacherAccountManagement.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TeacherAccountManagement().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}