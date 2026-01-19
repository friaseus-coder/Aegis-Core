package com.antigravity.aegis.presentation.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay
import com.antigravity.aegis.R

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
        delay(1500)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // Assuming the image 'logo_concept_1_modern_vault' is renamed to 'ic_logo_app' or similar in drawable
        // For now, I'll use a placeholder logic if I can't find the resource ID yet, 
        // but the plan said I should use the one I generated.
        // I will assume the resource R.drawable.logo_concept_1_modern_vault exists 
        // (I will need to move/rename it or use coil to load from file if it's not a resource, 
        // but standard way is resource. I'll ask to move it or just reference it if I can).
        // Wait, I copied it to 'logo_designs' folder, NOT res/drawable.
        // I should probably copy it to res/drawable/ic_logo_app.png first!
        
        // Changing strategy: reference by file path is hard in Compose image resource.
        // I'll assume for this step I will execute a copy command to put it in drawable.
        
        Image(
            painter = painterResource(id = R.drawable.ic_logo_app),
            contentDescription = "App Logo",
            alpha = alpha.value
        )
    }
}
