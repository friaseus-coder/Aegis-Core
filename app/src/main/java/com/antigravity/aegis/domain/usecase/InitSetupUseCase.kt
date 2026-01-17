package com.antigravity.aegis.domain.usecase

import com.antigravity.aegis.domain.repository.AuthRepository
import javax.inject.Inject

data class SetupState(
    val masterKey: ByteArray,
    val seedPhrase: List<String>
)

class InitSetupUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): SetupState {
        val masterKey = authRepository.createMasterKey()
        // In a real app, use a BIP39 library for Mnemonic generation.
        // For this demo, we use a static list or dummy generator.
        // DO NOT USE THIS FOR PRODUCTION CRYPTO WALLETS.
        val seedPhrase = generateDummyMnemonic() 
        return SetupState(masterKey, seedPhrase)
    }

    private fun generateDummyMnemonic(): List<String> {
        // Simplified list for demo purposes.
        val words = listOf("apple", "brave", "cloud", "delta", "eagle", "flame", "grape", "house", "image", "joker", "kite", "lemon", "moon", "neon", "ocean", "piano", "queen", "river", "stone", "tiger", "unity", "vivid", "water", "zebra")
        return words.shuffled().take(12)
    }
}
