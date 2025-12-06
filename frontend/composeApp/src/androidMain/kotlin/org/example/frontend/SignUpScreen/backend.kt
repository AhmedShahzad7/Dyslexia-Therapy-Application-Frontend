package org.example.frontend.SignUpScreen

import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class SignUpViewModel : ViewModel() {
    fun Register(email: String, password: String,onSuccess: () -> Unit) {

        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener {
            if (it.isSuccessful) {
                onSuccess()
            }
        }


    }
}