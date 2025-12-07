package org.example.frontend.SignUpScreen

import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class SignUpViewModel : ViewModel() {
    fun Register(email: String, password: String,onSuccess: (String?) -> Unit) {

        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener {task ->
            if (task.isSuccessful) {
                val userId = task.result?.user?.uid
                onSuccess(userId)
            }
        }


    }
}