package com.example.data.remote

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.local.entities.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException

data class UserSession(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val isSimulated: Boolean
)

@JsonClass(generateAdapter = true)
data class PortfolioSyncData(
    val profile: ProfileEntity?,
    val skills: List<SkillEntity>?,
    val experiences: List<ExperienceEntity>?,
    val themeSettings: ThemeSettingsEntity?,
    val sectionOrders: List<SectionOrderEntity>?,
    val certificates: List<CertificateEntity>? = null
)

class FirebaseSyncManager(private val context: Context) {
    private val TAG = "FirebaseSyncManager"
    
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences("portfolio_sync_prefs", Context.MODE_PRIVATE)
    private val moshi: Moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val syncDataAdapter = moshi.adapter(PortfolioSyncData::class.java)

    private val _currentUser = MutableStateFlow<UserSession?>(null)
    val currentUser: StateFlow<UserSession?> = _currentUser.asStateFlow()

    private val _isFirebaseAvailable = MutableStateFlow(false)
    val isFirebaseAvailable: StateFlow<Boolean> = _isFirebaseAvailable.asStateFlow()

    init {
        checkFirebaseAvailability()
        loadPersistedSession()
    }

    private fun checkFirebaseAvailability() {
        try {
            // Test if Firebase is initialized
            val app = FirebaseApp.getInstance()
            val auth = FirebaseAuth.getInstance()
            val firestore = FirebaseFirestore.getInstance()
            _isFirebaseAvailable.value = true
            Log.d(TAG, "Firebase está disponível e inicializado corretamente.")
        } catch (e: Exception) {
            _isFirebaseAvailable.value = false
            Log.w(TAG, "Firebase não está disponível: ${e.localizedMessage}. O app usará o modo Simulado para sincronização de nuvem.")
        }
    }

    private fun loadPersistedSession() {
        val uid = sharedPrefs.getString("session_uid", null)
        val email = sharedPrefs.getString("session_email", null)
        val displayName = sharedPrefs.getString("session_name", null)
        val photoUrl = sharedPrefs.getString("session_photo", null)
        val isSimulated = sharedPrefs.getBoolean("session_simulated", true)

        if (uid != null) {
            _currentUser.value = UserSession(
                uid = uid,
                email = email,
                displayName = displayName,
                photoUrl = photoUrl,
                isSimulated = isSimulated
            )
        }
    }

    private fun persistSession(session: UserSession) {
        sharedPrefs.edit().apply {
            putString("session_uid", session.uid)
            putString("session_email", session.email)
            putString("session_name", session.displayName)
            putString("session_photo", session.photoUrl)
            putBoolean("session_simulated", session.isSimulated)
            apply()
        }
        _currentUser.value = session
    }

    fun clearSession() {
        sharedPrefs.edit().apply {
            remove("session_uid")
            remove("session_email")
            remove("session_name")
            remove("session_photo")
            remove("session_simulated")
            apply()
        }
        
        if (_isFirebaseAvailable.value) {
            try {
                FirebaseAuth.getInstance().signOut()
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao deslogar do Firebase Auth: ${e.localizedMessage}")
            }
        }
        
        _currentUser.value = null
    }

    // Google Sign-In with real Firebase Auth
    suspend fun signInWithGoogleIdToken(idToken: String): UserSession {
        if (!_isFirebaseAvailable.value) {
            throw IllegalStateException("Firebase não disponível.")
        }
        
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = FirebaseAuth.getInstance().signInWithCredential(credential).await()
        val firebaseUser = authResult.user ?: throw Exception("Nenhum usuário retornado do Firebase.")

        val session = UserSession(
            uid = firebaseUser.uid,
            email = firebaseUser.email,
            displayName = firebaseUser.displayName,
            photoUrl = firebaseUser.photoUrl?.toString(),
            isSimulated = false
        )
        
        persistSession(session)
        return session
    }

    // Simulated Sign-In (for Testing and environments without Google Play services)
    fun signInSimulated(email: String, displayName: String): UserSession {
        // Generate a pseudo-uid based on email hash
        val safeEmail = email.trim().lowercase()
        val uid = "simulated_uid_" + safeEmail.hashCode()
        // Generate placeholder initial letter avatar or cool developer avatar
        val photoUrl = "https://api.dicebear.com/7.x/bottts/png?seed=${safeEmail}"

        val session = UserSession(
            uid = uid,
            email = safeEmail,
            displayName = displayName,
            photoUrl = photoUrl,
            isSimulated = true
        )

        persistSession(session)
        return session
    }

    // Push data to cloud (Firestore or Simulated SharedPrefs)
    suspend fun uploadPortfolio(uid: String, isSimulated: Boolean, data: PortfolioSyncData, resumeId: String) {
        val jsonString = syncDataAdapter.toJson(data)
        val normalizedId = if (resumeId == "Principal") "main" else resumeId
        
        if (isSimulated || !_isFirebaseAvailable.value) {
            // Save in simulated cloud (SharedPreferences matching uid)
            sharedPrefs.edit().putString("cloud_data_${uid}_$normalizedId", jsonString).apply()
            
            // Update simulated list
            val currentList = listSavedResumes(uid, isSimulated).toMutableSet()
            currentList.add(resumeId)
            val type = com.squareup.moshi.Types.newParameterizedType(List::class.java, String::class.java)
            val listAdapter = moshi.adapter<List<String>>(type)
            sharedPrefs.edit().putString("cloud_resumes_list_$uid", listAdapter.toJson(currentList.toList())).apply()
            
            Log.d(TAG, "Dados enviados para a nuvem simulada para o UID: $uid, currículo: $resumeId")
        } else {
            // Save in Real Firestore with timeout
            try {
                withTimeout(8000) {
                    val db = FirebaseFirestore.getInstance()
                    val docRef = db.collection("users").document(uid).collection("portfolio").document(normalizedId)
                    
                    val dataMap = mapOf(
                        "jsonData" to jsonString,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    docRef.set(dataMap).await()
                }
                Log.d(TAG, "Dados enviados para o Firebase Firestore com sucesso!")
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Falha por limite de tempo ao enviar para o Firestore.")
                throw Exception("Tempo esgotado (timeout) ao conectar ao Firebase Firestore. Certifique-se de que:\n1. O 'Cloud Firestore' está ATIVADO no Console do Firebase.\n2. O banco de dados foi criado (modo de teste ou produção).\n3. Suas regras de segurança (Security Rules) permitem gravação.")
            } catch (e: Exception) {
                Log.e(TAG, "Falha ao enviar para o Firestore: ${e.localizedMessage}")
                val msg = e.localizedMessage ?: ""
                if (msg.contains("PERMISSION_DENIED", ignoreCase = true)) {
                    throw Exception("Acesso Negado (Permission Denied). Verifique as regras de segurança (Rules) do seu Firestore no Console do Firebase.")
                }
                throw e
            }
        }
    }

    // Pull data from cloud (Firestore or Simulated SharedPrefs)
    suspend fun downloadPortfolio(uid: String, isSimulated: Boolean, resumeId: String): PortfolioSyncData? {
        val normalizedId = if (resumeId == "Principal") "main" else resumeId
        
        if (isSimulated || !_isFirebaseAvailable.value) {
            // Retrieve from simulated cloud
            var jsonString = sharedPrefs.getString("cloud_data_${uid}_$normalizedId", null)
            if (jsonString == null && normalizedId == "main") {
                // Fallback to legacy single document path
                jsonString = sharedPrefs.getString("cloud_data_$uid", null)
            }
            return if (jsonString != null) {
                syncDataAdapter.fromJson(jsonString)
            } else {
                null
            }
        } else {
            // Retrieve from Real Firestore with timeout
            try {
                return withTimeout(8000) {
                    val db = FirebaseFirestore.getInstance()
                    val docRef = db.collection("users").document(uid).collection("portfolio").document(normalizedId)
                    val snapshot = docRef.get().await()
                    
                    if (snapshot.exists()) {
                        val jsonString = snapshot.getString("jsonData")
                        if (jsonString != null) {
                            syncDataAdapter.fromJson(jsonString)
                        } else {
                            null
                        }
                    } else if (normalizedId == "main") {
                        // Fallback to check legacy doc in firestore if needed, though they should be equivalent
                        null
                    } else {
                        null
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Falha por limite de tempo ao baixar do Firestore.")
                throw Exception("Tempo esgotado (timeout) ao conectar ao Firebase Firestore. Certifique-se de que:\n1. O 'Cloud Firestore' está ATIVADO no Console do Firebase.\n2. Suas regras de segurança (Security Rules) permitem leitura.")
            } catch (e: Exception) {
                Log.e(TAG, "Falha ao baixar do Firestore: ${e.localizedMessage}")
                val msg = e.localizedMessage ?: ""
                if (msg.contains("PERMISSION_DENIED", ignoreCase = true)) {
                    throw Exception("Acesso Negado (Permission Denied). Verifique as regras de segurança (Rules) do seu Firestore no Console do Firebase.")
                }
                throw e
            }
        }
    }

    // List all saved resumes for the user
    suspend fun listSavedResumes(uid: String, isSimulated: Boolean): List<String> {
        if (isSimulated || !_isFirebaseAvailable.value) {
            val listJson = sharedPrefs.getString("cloud_resumes_list_$uid", null)
            if (listJson != null) {
                try {
                    val type = com.squareup.moshi.Types.newParameterizedType(List::class.java, String::class.java)
                    val adapter = moshi.adapter<List<String>>(type)
                    val list = adapter.fromJson(listJson) ?: listOf("Principal")
                    return if (list.isEmpty()) listOf("Principal") else list
                } catch (e: Exception) {
                    return listOf("Principal")
                }
            } else {
                // Migrate any legacy data
                val legacy = sharedPrefs.getString("cloud_data_$uid", null)
                if (legacy != null) {
                    sharedPrefs.edit()
                        .putString("cloud_data_${uid}_main", legacy)
                        .remove("cloud_data_$uid")
                        .apply()
                }
                return listOf("Principal")
            }
        } else {
            return try {
                withTimeout(8000) {
                    val db = FirebaseFirestore.getInstance()
                    val querySnapshot = db.collection("users").document(uid).collection("portfolio").get().await()
                    val list = querySnapshot.documents.map { it.id }.filter { it != "catalog" }
                    if (list.isEmpty()) {
                        listOf("Principal")
                    } else {
                        list.map { if (it == "main") "Principal" else it }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao listar currículos do Firestore: ${e.localizedMessage}")
                listOf("Principal")
            }
        }
    }

    // Delete a specific resume
    suspend fun deletePortfolio(uid: String, isSimulated: Boolean, resumeId: String) {
        val normalizedId = if (resumeId == "Principal") "main" else resumeId
        if (isSimulated || !_isFirebaseAvailable.value) {
            sharedPrefs.edit().remove("cloud_data_${uid}_$normalizedId").apply()
            val currentList = listSavedResumes(uid, isSimulated).toMutableSet()
            currentList.remove(resumeId)
            val type = com.squareup.moshi.Types.newParameterizedType(List::class.java, String::class.java)
            val listAdapter = moshi.adapter<List<String>>(type)
            sharedPrefs.edit().putString("cloud_resumes_list_$uid", listAdapter.toJson(currentList.toList())).apply()
        } else {
            try {
                withTimeout(8000) {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("users").document(uid).collection("portfolio").document(normalizedId).delete().await()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao deletar currículo do Firestore: ${e.localizedMessage}")
                throw e
            }
        }
    }
}
