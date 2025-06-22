package com.example.lab6


import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    private lateinit var etAmount: EditText
    private lateinit var spinnerFrom: Spinner
    private lateinit var spinnerTo: Spinner
    private lateinit var btnConvert: Button
    private lateinit var btnSwap: Button
    private lateinit var tvResult: TextView
    private lateinit var tvRate: TextView
    private lateinit var progressBar: ProgressBar

    private val currencies = arrayOf(
        "USD", "EUR", "UAH", "GBP", "JPY", "CAD", "AUD", "CHF", "CNY"
    )

    private val currencyNames = mapOf(
        "USD" to "Долар США",
        "EUR" to "Євро",
        "UAH" to "Українська гривня",
        "GBP" to "Британський фунт",
        "JPY" to "Японська єна",
        "CAD" to "Канадський долар",
        "AUD" to "Австралійський долар",
        "CHF" to "Швейцарський франк",
        "CNY" to "Китайський юань",
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupSpinners()
        setupClickListeners()
    }

    private fun initViews() {
        etAmount = findViewById(R.id.etAmount)
        spinnerFrom = findViewById(R.id.spinnerFrom)
        spinnerTo = findViewById(R.id.spinnerTo)
        btnConvert = findViewById(R.id.btnConvert)
        btnSwap = findViewById(R.id.btnSwap)
        tvResult = findViewById(R.id.tvResult)
        tvRate = findViewById(R.id.tvRate)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupSpinners() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerFrom.adapter = adapter
        spinnerTo.adapter = adapter

        spinnerFrom.setSelection(0) // USD
        spinnerTo.setSelection(2)   // UAH
    }

    private fun setupClickListeners() {
        btnConvert.setOnClickListener {
            convertCurrency()
        }

        btnSwap.setOnClickListener {
            swapCurrencies()
        }
    }

    private fun convertCurrency() {
        val amountText = etAmount.text.toString().trim()

        if (amountText.isEmpty()) {
            Toast.makeText(this, "Будь ласка, введіть суму для конвертації", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = try {
            amountText.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Неправильний формат числа", Toast.LENGTH_SHORT).show()
            return
        }

        if (amount <= 0) {
            Toast.makeText(this, "Сума повинна бути більше нуля", Toast.LENGTH_SHORT).show()
            return
        }

        val fromCurrency = currencies[spinnerFrom.selectedItemPosition]
        val toCurrency = currencies[spinnerTo.selectedItemPosition]

        if (fromCurrency == toCurrency) {
            displayResult(amount, amount, 1.0, fromCurrency, toCurrency)
            return
        }

        fetchExchangeRate(fromCurrency, toCurrency, amount)
    }

    private fun swapCurrencies() {
        val fromPosition = spinnerFrom.selectedItemPosition
        val toPosition = spinnerTo.selectedItemPosition

        spinnerFrom.setSelection(toPosition)
        spinnerTo.setSelection(fromPosition)
    }

    private fun fetchExchangeRate(fromCurrency: String, toCurrency: String, amount: Double) {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val rate = withContext(Dispatchers.IO) {
                    getExchangeRateFromAPI(fromCurrency, toCurrency)
                }

                if (rate != null) {
                    val convertedAmount = amount * rate
                    displayResult(amount, convertedAmount, rate, fromCurrency, toCurrency)
                } else {
                    Toast.makeText(this@MainActivity, "Не вдалося отримати курс валют", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Помилка мережі: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private suspend fun getExchangeRateFromAPI(from: String, to: String): Double? {
        return try {
            val urlString = "https://api.exchangerate-api.com/v4/latest/$from"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                val jsonObject = JSONObject(response)
                val rates = jsonObject.getJSONObject("rates")

                if (rates.has(to)) {
                    rates.getDouble(to)
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun displayResult(originalAmount: Double, convertedAmount: Double, rate: Double,
                              fromCurrency: String, toCurrency: String) {
        val df = DecimalFormat("#,##0.00")
        val rateFormat = DecimalFormat("#,##0.0000")

        val fromName = currencyNames[fromCurrency] ?: fromCurrency
        val toName = currencyNames[toCurrency] ?: toCurrency

        tvResult.text = "${df.format(originalAmount)} $fromCurrency = ${df.format(convertedAmount)} $toCurrency"
        tvRate.text = "Курс: 1 $fromCurrency = ${rateFormat.format(rate)} $toCurrency\n" +
                "$fromName → $toName"
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            progressBar.visibility = ProgressBar.VISIBLE
            btnConvert.isEnabled = false
            btnConvert.text = "Завантаження..."
        } else {
            progressBar.visibility = ProgressBar.GONE
            btnConvert.isEnabled = true
            btnConvert.text = "Конвертувати"
        }
    }
}