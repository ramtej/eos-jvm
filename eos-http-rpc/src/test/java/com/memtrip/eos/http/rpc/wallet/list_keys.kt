package com.memtrip.eos.http.rpc.wallet

import com.memtrip.eos.core.crypto.EosPrivateKey
import com.memtrip.eos.http.rpc.Api
import com.memtrip.eos.http.rpc.Config
import com.memtrip.eos.http.rpc.generateUniqueWalletName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.util.Arrays.asList
import java.util.concurrent.TimeUnit

@RunWith(JUnitPlatform::class)
class WalletListKeysTest : Spek({

    given("an Api.Wallet") {

        val okHttpClient by memoized {
            OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build()
        }

        val walletApi by memoized { Api(Config.WALLET_API_BASE_URL, okHttpClient).wallet }

        on("v1/wallet/list_keys") {

            val walletName = generateUniqueWalletName()

            val walletPassword = walletApi.create(walletName).blockingGet().body()!!

            val privateKey = EosPrivateKey()

            walletApi
                .importKey(listOf(walletName, privateKey.toString()))
                .blockingGet()

            val listOfKeys = walletApi.listKeys(asList(walletName, walletPassword)).blockingGet()

            it("should list the keys associated with the wallet") {
                assertTrue(listOfKeys.isSuccessful)
                assertEquals(listOfKeys.body()!![0][0], privateKey.publicKey.toString())
                assertEquals(listOfKeys.body()!![0][1], privateKey.toString())
            }
        }
    }
})