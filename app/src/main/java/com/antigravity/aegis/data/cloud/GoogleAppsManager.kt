package com.antigravity.aegis.data.cloud

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Arrays
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAppsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Scopes combinados: Drive + Calendar
    private val scopes = Arrays.asList(
        DriveScopes.DRIVE_APPDATA,
        CalendarScopes.CALENDAR_EVENTS
    )

    fun getGoogleSignInOptions(): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(
                com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_APPDATA),
                com.google.android.gms.common.api.Scope(CalendarScopes.CALENDAR_EVENTS)
            )
            .build()
    }

    fun getLastSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    fun getDriveService(account: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(context, scopes)
        credential.selectedAccount = account.account
        return Drive.Builder(NetHttpTransport(), GsonFactory(), credential)
            .setApplicationName("Aegis Core")
            .build()
    }

    fun getCalendarService(account: GoogleSignInAccount): Calendar {
        val credential = GoogleAccountCredential.usingOAuth2(context, scopes)
        credential.selectedAccount = account.account
        return Calendar.Builder(NetHttpTransport(), GsonFactory(), credential)
            .setApplicationName("Aegis Core")
            .build()
    }
}
