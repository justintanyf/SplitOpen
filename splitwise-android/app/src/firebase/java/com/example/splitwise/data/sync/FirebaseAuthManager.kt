package com.example.splitwise.data.sync

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthManager @Inject constructor(
    private val auth: FirebaseAuth
) {
    suspend fun ensureAuthenticated(): String {
        return auth.currentUser?.uid
            ?: auth.signInAnonymously().await().user!!.uid
    }
}
