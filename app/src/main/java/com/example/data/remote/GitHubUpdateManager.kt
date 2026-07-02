package com.example.data.remote

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

data class GitHubRelease(
    val tagName: String,
    val name: String,
    val body: String,
    val apkUrl: String,
    val publishedAt: String
)

sealed class UpdateUiState {
    object Idle : UpdateUiState()
    object Checking : UpdateUiState()
    data class NewUpdateAvailable(val release: GitHubRelease) : UpdateUiState()
    object NoUpdateAvailable : UpdateUiState()
    data class Downloading(val progress: Int) : UpdateUiState()
    data class DownloadCompleted(val apkFile: File) : UpdateUiState()
    data class Error(val message: String) : UpdateUiState()
}

class GitHubUpdateManager(private val context: Context) {
    private val TAG = "GitHubUpdateManager"
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences("portfolio_sync_prefs", Context.MODE_PRIVATE)

    private val _updateState = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val updateState: StateFlow<UpdateUiState> = _updateState.asStateFlow()

    // Getters and Setters for Owner and Repo configuration
    fun getGitHubOwner(): String = sharedPrefs.getString("github_owner", "andreluiz2431") ?: "andreluiz2431"
    fun getGitHubRepo(): String = sharedPrefs.getString("github_repo", "DevFolioPro") ?: "DevFolioPro"

    fun saveGitHubConfig(owner: String, repo: String) {
        sharedPrefs.edit()
            .putString("github_owner", owner.trim())
            .putString("github_repo", repo.trim())
            .apply()
    }

    /**
     * Queries the latest release of the configured repository
     */
    suspend fun checkForUpdates(): GitHubRelease? = withContext(Dispatchers.IO) {
        _updateState.value = UpdateUiState.Checking
        val owner = getGitHubOwner()
        val repo = getGitHubRepo()
        val urlString = "https://api.github.com/repos/$owner/$repo/releases/latest"

        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.setRequestProperty("User-Agent", "DevFolioPro-Updater-v1.0")
            connection.connectTimeout = 8000
            connection.readTimeout = 8000

            val responseCode = connection.responseCode
            if (responseCode == 200) {
                val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(jsonText)
                val tagName = root.optString("tag_name", "")
                val name = root.optString("name", "")
                val body = root.optString("body", "")
                val publishedAt = root.optString("published_at", "")

                // Find the first asset ending in .apk
                var apkUrl = ""
                val assets = root.optJSONArray("assets")
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val assetName = asset.optString("name", "")
                        if (assetName.endsWith(".apk")) {
                            apkUrl = asset.optString("browser_download_url", "")
                            break
                        }
                    }
                }

                if (apkUrl.isBlank()) {
                    _updateState.value = UpdateUiState.Error("Nenhum arquivo APK (.apk) foi encontrado nesta release. Verifique se o GitHub Actions concluiu o build e anexou o arquivo à release.")
                    return@withContext null
                }

                val release = GitHubRelease(
                    tagName = tagName,
                    name = name,
                    body = body,
                    apkUrl = apkUrl,
                    publishedAt = publishedAt
                )

                // Simple check: Compare if current version name is different from tagName
                val currentVersion = context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
                
                // Clean version strings for robust comparison
                val cleanCurrent = currentVersion.replace("v", "").trim()
                val cleanLatest = tagName.replace("v", "").trim()

                Log.d(TAG, "Current: $cleanCurrent, Latest: $cleanLatest")

                if (cleanLatest.isNotBlank() && cleanLatest != cleanCurrent) {
                    _updateState.value = UpdateUiState.NewUpdateAvailable(release)
                    release
                } else {
                    _updateState.value = UpdateUiState.NoUpdateAvailable
                    null
                }
            } else {
                val errorMessage = when (responseCode) {
                    404 -> "Nenhuma release encontrada no repositório '$owner/$repo' ou repositório privado. Crie uma tag/release para disparar o GitHub Actions!"
                    403 -> "Acesso negado ou limite de requisições do GitHub API excedido para o seu IP. Tente novamente mais tarde."
                    else -> "Erro na API do GitHub (Código HTTP $responseCode)"
                }
                _updateState.value = UpdateUiState.Error(errorMessage)
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking updates: ${e.localizedMessage}", e)
            _updateState.value = UpdateUiState.Error("Falha de conexão: ${e.localizedMessage}")
            null
        }
    }

    /**
     * Downloads the APK file from GitHub and reports progress
     */
    suspend fun downloadAndInstallApk(downloadUrl: String) = withContext(Dispatchers.IO) {
        if (downloadUrl.isBlank()) {
            _updateState.value = UpdateUiState.Error("URL do APK inválida.")
            return@withContext
        }

        try {
            _updateState.value = UpdateUiState.Downloading(0)
            val url = URL(downloadUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", "DevFolioPro-Updater-v1.0")
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.connect()

            // Handle redirection if necessary (all 3xx status codes)
            var redirectConn = connection
            var status = connection.responseCode
            var redirectCount = 0
            while (status in 300..399 && redirectCount < 5) {
                val newUrl = redirectConn.getHeaderField("Location") ?: break
                Log.d(TAG, "Redirecting ($status) to: $newUrl")
                redirectConn = URL(newUrl).openConnection() as HttpURLConnection
                redirectConn.setRequestProperty("User-Agent", "DevFolioPro-Updater-v1.0")
                redirectConn.connectTimeout = 15000
                redirectConn.readTimeout = 15000
                status = redirectConn.responseCode
                redirectCount++
            }

            if (status != HttpURLConnection.HTTP_OK) {
                throw IOException("Server returned HTTP response code: $status for URL: ${redirectConn.url}")
            }

            val fileLength = redirectConn.contentLength
            val inputStream: InputStream = redirectConn.inputStream

            // Use externalCacheDir if available, otherwise fallback to cacheDir
            val updateFolder = context.externalCacheDir?.let { File(it, "app_updates") } ?: File(context.cacheDir, "app_updates")
            if (!updateFolder.exists()) {
                updateFolder.mkdirs()
            }
            val apkFile = File(updateFolder, "update.apk")
            if (apkFile.exists()) {
                apkFile.delete()
            }

            val outputStream = FileOutputStream(apkFile)
            val buffer = ByteArray(4096)
            var total: Long = 0
            var count: Int

            while (inputStream.read(buffer).also { count = it } != -1) {
                total += count
                if (fileLength > 0) {
                    val progress = ((total * 100) / fileLength).toInt()
                    _updateState.value = UpdateUiState.Downloading(progress)
                } else {
                    // Fallback progress if file length is unknown
                    _updateState.value = UpdateUiState.Downloading(-1)
                }
                outputStream.write(buffer, 0, count)
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            // Set file permissions to be readable by installer
            try {
                apkFile.setReadable(true, false)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set apk file readable: ${e.localizedMessage}")
            }

            Log.d(TAG, "APK successfully downloaded to ${apkFile.absolutePath}, size: $total bytes")
            _updateState.value = UpdateUiState.DownloadCompleted(apkFile)
            
            // Auto install
            installApk(apkFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading APK: ${e.localizedMessage}", e)
            _updateState.value = UpdateUiState.Error("Falha ao baixar arquivo: ${e.localizedMessage}")
        }
    }

    /**
     * Triggers the Android Native Package Installer Dialog
     */
    fun installApk(file: File) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    Log.d(TAG, "Requesting install permission from settings")
                    val settingsIntent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(settingsIntent)
                    _updateState.value = UpdateUiState.Error("Permissão necessária: Habilite a permissão de instalar aplicativos desconhecidos nas configurações e tente instalar novamente.")
                    return
                }
            }

            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error installing APK: ${e.localizedMessage}", e)
            _updateState.value = UpdateUiState.Error("Não foi possível abrir o instalador: ${e.localizedMessage}")
        }
    }

    fun resetState() {
        _updateState.value = UpdateUiState.Idle
    }
}
