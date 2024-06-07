package com.example.mapboxsearchsdk2

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.example.mapboxsearchsdk2.databinding.FragmentMapBinding
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.scalebar.scalebar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException


class MapFragment : Fragment() {
    private lateinit var binding: FragmentMapBinding
    private lateinit var mapView: MapView
    private lateinit var queryEditInputText: EditText
    private lateinit var searchResultsAdapter: SearchResultsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpMap()
        setUpSearchView()
        setUpSearchResultsView()
    }

    private fun setUpMap() {
        mapView = binding.mapView
        mapView.scalebar.enabled = false

        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder().center(Point.fromLngLat(21.010872, 52.220370)).pitch(0.0)
                .zoom(10.0).bearing(0.0).build()
        )
    }

    //                    вывод адреса как текст
    private fun setUpSearchView() {
        queryEditInputText = binding.searcET
        val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        queryEditInputText.doAfterTextChanged { editable ->
            scope.launch {
                delay(500)
                val address = editable.toString()
                if (address.isBlank()) {
                    binding.searcET.error = "Введите адрес"
                    searchResultsAdapter.setSearchResults(emptyList())
                } else {
                    geocodeAddress(address)
                }
            }
        }
    }

    private fun geocodeAddress(address: String) {
        val accessToken = getString(R.string.mapbox_access_token)
        val geocodeUrl =
            "https://api.mapbox.com/geocoding/v5/mapbox.places/$address.json?country=pl&proximity=ip&language=pl&access_token=$accessToken"
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(geocodeUrl)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                clearSearchResults()
                Log.e("Request", "Failed to execute request: ${e.message}")
            }

            private fun clearSearchResults() {
                Handler(Looper.getMainLooper()).post {
                    searchResultsAdapter.setSearchResults(emptyList())
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (responseBody.isNullOrBlank()) {
                    clearSearchResults()
                } else {
                    Handler(Looper.getMainLooper()).post {
                        handleGeocodingSuccessResponse(responseBody)
                    }
                }
            }
        })
    }

    private fun handleGeocodingSuccessResponse(
        responseBody: String,
    ) {
        try {
            val jsonObject = JSONObject(responseBody)
            val features = jsonObject.getJSONArray("features")
            val results = mutableListOf<SearchResult>()

            for (i in 0 until features.length()) {
                val feature = features.getJSONObject(i)
                results.add(
                    SearchResult(
                        address = feature.getString("place_name"),
                    )
                )
            }

            searchResultsAdapter.setSearchResults(results)
        } catch (e: JSONException) {
            Log.e("Response", "Error parsing JSON: ${e.message}")
        }
    }

    private fun setUpSearchResultsView() {
        searchResultsAdapter = SearchResultsAdapter { selectedResult ->
            // TODO Do something with the selected result, eg. show location on the map.
        }
        binding.searchResultsView.adapter = searchResultsAdapter
    }
}