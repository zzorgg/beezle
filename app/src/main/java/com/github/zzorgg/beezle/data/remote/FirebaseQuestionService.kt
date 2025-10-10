package com.github.zzorgg.beezle.data.remote

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class FirebaseMathQuestion(
    val question: String = "",
    val answer: Int = 0
)

@Singleton
class FirebaseQuestionService @Inject constructor(
    private val database: FirebaseDatabase
) {

    companion object {
        private const val TAG = "FirebaseQuestionService"
        private const val MATH_QUESTIONS_PATH = "mathQuestions"
    }

    /**
     * Fetch all math questions from Firebase Realtime Database
     */
    suspend fun fetchMathQuestions(): List<FirebaseMathQuestion> = suspendCancellableCoroutine { continuation ->
        val questionsRef = database.getReference(MATH_QUESTIONS_PATH)

        questionsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val questions = mutableListOf<FirebaseMathQuestion>()

                    for (childSnapshot in snapshot.children) {
                        val question = childSnapshot.getValue(FirebaseMathQuestion::class.java)
                        if (question != null) {
                            questions.add(question)
                        }
                    }

                    Log.d(TAG, "Successfully fetched ${questions.size} math questions from Firebase")
                    continuation.resume(questions)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing questions from Firebase", e)
                    continuation.resumeWithException(e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to fetch questions from Firebase: ${error.message}")
                continuation.resumeWithException(error.toException())
            }
        })

        continuation.invokeOnCancellation {
            Log.d(TAG, "Question fetch cancelled")
        }
    }

    /**
     * Fetch a random math question from Firebase
     */
    suspend fun fetchRandomMathQuestion(): FirebaseMathQuestion? {
        return try {
            val questions = fetchMathQuestions()
            questions.randomOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching random question", e)
            null
        }
    }

    /**
     * Observe math questions in real-time
     */
    fun observeMathQuestions(): Flow<List<FirebaseMathQuestion>> = callbackFlow {
        val questionsRef = database.getReference(MATH_QUESTIONS_PATH)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val questions = mutableListOf<FirebaseMathQuestion>()

                    for (childSnapshot in snapshot.children) {
                        val question = childSnapshot.getValue(FirebaseMathQuestion::class.java)
                        if (question != null) {
                            questions.add(question)
                        }
                    }

                    Log.d(TAG, "Real-time update: ${questions.size} math questions")
                    trySend(questions)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing real-time questions", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Real-time listener cancelled: ${error.message}")
                close(error.toException())
            }
        }

        questionsRef.addValueEventListener(listener)

        awaitClose {
            questionsRef.removeEventListener(listener)
        }
    }
}
