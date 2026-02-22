package com.antigravity.aegis.data.cloud

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleDriveSyncManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = DriveScopes.DRIVE_APPDATA

    fun getGoogleSignInOptions(): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(com.google.android.gms.common.api.Scope(scope))
            .build()
    }

    fun getLastSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    fun getDriveService(account: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, Collections.singleton(scope)
        )
        credential.selectedAccount = account.account

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory(),
            credential
        )
            .setApplicationName("Aegis Core")
            .build()
    }
}
