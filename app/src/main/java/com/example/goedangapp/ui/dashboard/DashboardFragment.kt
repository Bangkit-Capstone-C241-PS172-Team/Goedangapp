package com.example.goedangapp.ui.dashboard

import DashboardViewModel
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.goedangapp.ViewModelFactory
import com.example.goedangapp.data.Forecast
import com.example.goedangapp.databinding.FragmentDashboardBinding
import com.example.goedangapp.util.ResultState
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter


class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<DashboardViewModel> {
        ViewModelFactory.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        viewModel.getUser().observe(viewLifecycleOwner) { user ->
            binding.username.text = user.name
        }
        viewModel.getLowInStock().observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is ResultState.Loading -> {
                    // Show loading indicator
                }

                is ResultState.Success -> {
                    val filteredAndSortedItems = result.data
                    val firstItem = filteredAndSortedItems.firstOrNull()
                    val lowStockQtyText: String

                    firstItem?.let {
                        val lowStockQty = it.quantity
                        val lowStockMeasuringUnit = it.measuringUnit
                        lowStockQtyText = "$lowStockQty $lowStockMeasuringUnit"

                        binding.lowestInStock.visibility = View.VISIBLE
                        binding.lowestInStockItem.text = it.name

                        binding.lowestInStockQty.text = lowStockQtyText
                    } ?: run {
                        binding.lowestInStock.visibility = View.GONE
                    }
                }

                is ResultState.Error -> {
                    Toast.makeText(requireContext(), result.error, Toast.LENGTH_SHORT).show()
                }

                else -> {}
            }
        })

        viewModel.getSortedItemEntries().observe(viewLifecycleOwner, Observer { result ->
            when (result) {

                is ResultState.Loading -> {}
                is ResultState.Success -> {
                    val sortedItem = result.data
                    val firstItem = sortedItem.firstOrNull()
                    var recentlyAddText: String

                    firstItem?.let {
                        viewModel.getItemById(it.itemId ?: "")
                            .observe(viewLifecycleOwner, Observer { itemDetailResult ->
                                when (itemDetailResult) {
                                    is ResultState.Success -> {
                                        val itemDetail = itemDetailResult.data
                                        val recentlyAddQty = it.quantity
                                        val recentlyAddMeasuringUnit = itemDetail.measuringUnit
                                        val recentlyAddName = itemDetail.name
                                        val recentlyAddDate = formatDateString(it.createdAt)

                                        recentlyAddText =
                                            "$recentlyAddQty $recentlyAddMeasuringUnit"

                                        binding.recentlyAdd.visibility = View.VISIBLE
                                        binding.recentlyAddItem.text = recentlyAddName
                                        binding.recentlyAddQty.text = recentlyAddText
                                        binding.recentlyAddDate.text = recentlyAddDate

                                    }

                                    is ResultState.Error -> {
                                        Log.e(
                                            "DashboardFragment",
                                            "Error fetching item detail: ${itemDetailResult.error}"
                                        )
                                    }

                                    is ResultState.Loading -> {
                                        // Handle loading state if needed
                                    }

                                    else -> {}
                                }
                            })
                    } ?: run {
                        binding.lowestInStock.visibility = View.GONE
                    }
                }

                else -> {}
            }
        })

        setupLineChart()
        loadChartData()

        return binding.root
    }

    private fun formatDateString(dateString: String?): String {
        return if (dateString != null) {
            val offsetDateTime = OffsetDateTime.parse(dateString)
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            offsetDateTime.format(formatter)
        } else {
            ""
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupLineChart() {
        binding.lineChart.apply {
            setTouchEnabled(true)
            setPinchZoom(true)
            description.isEnabled = false
        }
    }

    private fun loadChartData() {
        val apiClient = YourApiClient()
        val requestBody = """
        {
            "item": "Cabai Merah Keriting Tanpa Tangkai",
            "tanggal": [
                "2024-04-25", "2024-04-26", "2024-04-27", "2024-04-28", "2024-04-29", 
                "2024-04-30", "2024-05-01", "2024-05-02", "2024-05-03", "2024-05-04", 
                "2024-05-05", "2024-05-06", "2024-05-07", "2024-05-08", "2024-05-09", 
                "2024-05-10", "2024-05-11", "2024-05-12", "2024-05-13", "2024-05-24" 
            ],
            "harga": [
                31000.0, 31000.0, 48000.0, 65000.0, 65000.0, 
                65000.0, 65000.0, 65000.0, 65000.0, 59000.0, 
                57666.67, 56333.33, 55000.0, 52555.56, 50111.11, 
                47666.67, 45222.22, 42777.78, 40333.33, 36833.33
            ]
        }
    """.trimIndent()

        apiClient.fetchForecastData(requestBody) { response, error ->
            if (response != null) {
                // Parse the response JSON into your ForecastData object using Gson
                val gson = Gson()
                val forecastData = gson.fromJson(response,Forecast::class.java)

                // Update the chart with the forecast data
                updateChartWithForecast(forecastData)
            } else {
                // Handle the error
                Log.e("RequestError", "Error: $error")
            }
        }
    }

    private fun updateChartWithForecast(forecastData: Forecast) {
        val entries = forecastData.tanggal.mapIndexed { index, date ->
            Entry(index.toFloat(), forecastData.harga[index].toFloat())
        }

        val dataSet = LineDataSet(entries, forecastData.item).apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
        }

        val lineData = LineData(dataSet)
        binding.lineChart.data = lineData
        binding.lineChart.invalidate() // Refresh chart
    }

}

class YourApiClient {

    private val client = OkHttpClient()

    fun fetchForecastData(
        requestBody: String,
        callback: (response: String?, error: String?) -> Unit
    ) {
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val request = Request.Builder()
            .url("https://dep-prep-lh5lfcq2da-et.a.run.app/forecast")
            .post(requestBody.toRequestBody(mediaType))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    callback(null, "Unexpected code: $response")
                    return
                }
                val responseData = response.body?.string()
                callback(responseData, null)
            }
        })
    }
}