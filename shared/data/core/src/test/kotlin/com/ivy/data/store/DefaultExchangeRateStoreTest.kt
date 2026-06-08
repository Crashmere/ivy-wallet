package com.ivy.data.store

import arrow.core.Either
import arrow.core.left
import com.ivy.data.db.dao.read.ExchangeRatesDao
import com.ivy.data.db.dao.write.WriteExchangeRatesDao
import com.ivy.data.model.ExchangeRate
import com.ivy.data.remote.impl.RemoteExchangeRatesDataSourceImpl
import com.ivy.data.remote.responses.ExchangeRatesResponse
import com.ivy.data.mapper.ExchangeRateMapper
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DefaultExchangeRateStoreTest {
    private val mapper = mockk<ExchangeRateMapper>()
    private val exchangeRatesDao = mockk<ExchangeRatesDao>()
    private val writeExchangeRatesDao = mockk<WriteExchangeRatesDao>()
    private val remoteExchangeRatesDataSource = mockk<RemoteExchangeRatesDataSourceImpl>()

    private lateinit var store: DefaultExchangeRateStore

    @Before
    fun setup() {
        store = DefaultExchangeRateStore(
            mapper = mapper,
            exchangeRatesDao = exchangeRatesDao,
            writeExchangeRatesDao = writeExchangeRatesDao,
            remoteExchangeRatesDataSource = remoteExchangeRatesDataSource,
        )
    }

    @Test
    fun `fetchExchangeRates - successful network responses`() = runTest {
        // given
        val mockResponse = ExchangeRatesResponse(
            date = "",
            rates = emptyMap(),
        )
        val mockRates = mockk<List<ExchangeRate>>()
        with(mapper) {
            every { mockResponse.toDomain() } returns Either.Right(mockRates)
        }
        coEvery {
            remoteExchangeRatesDataSource.fetchEurExchangeRates()
        } returns Either.Right(mockResponse)

        // when
        val result = store.fetchEurExchangeRates()

        // then
        result.shouldBeRight() shouldBe mockRates
    }

    @Test
    fun `fetchExchangeRates - unsuccessful network responses`() = runTest {
        // given
        val errResponse = "Network Error"
        coEvery {
            remoteExchangeRatesDataSource.fetchEurExchangeRates()
        } returns errResponse.left()

        // when
        val result = store.fetchEurExchangeRates()

        // then
        result.shouldBeLeft() shouldBe errResponse
    }
}
