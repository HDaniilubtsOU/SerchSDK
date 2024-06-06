package com.example.mapboxsearchsdk2

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mapboxsearchsdk2.databinding.FragmentMapBinding
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.search.ServiceProvider
import com.mapbox.search.autofill.*
import com.mapbox.search.common.CompletionCallback
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.ui.adapter.autofill.AddressAutofillUiAdapter
import com.mapbox.search.ui.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException


class MapFragment : Fragment(){
    private lateinit var binding: FragmentMapBinding
    private lateinit var mapView: MapView
    private lateinit var searchResultsView: SearchResultsView
    private lateinit var queryEditInputText: EditText
    private var loadingTask: Any? = null
    private lateinit var addressAutofillUiAdapter: AddressAutofillUiAdapter
    private lateinit var fullAddress: TextView
    private var ignoreNextMapIdleEvent: Boolean = false
    private var ignoreNextQueryTextUpdate: Boolean = false
    private lateinit var addressAutofill: AddressAutofill



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        val view = inflater.inflate(R.layout.fragment_map, container, false)
//        return view
        binding = FragmentMapBinding.inflate(inflater, container, false)


//        fun activateLocationComponent() {
//            // логика активации компонента местоположения
//            with(mapView) {
////                location.locationPuck = createDefault2DPuck(withBearing = true)
//                location.enabled = true
////                location.puckBearing = PuckBearing.COURSE
//                viewport.transitionTo(
//                    targetState = viewport.makeFollowPuckViewportState(),
//                    transition = viewport.makeImmediateViewportTransition()
//                )
//            }
//        }
//        val permissionsManager = PermissionsManager(object : PermissionsListener {
//            override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {}
//
//            override fun onPermissionResult(granted: Boolean) {
//                if (granted) {
//                    // Разрешения получены, можно выполнять логику, требующую разрешений
//                    activateLocationComponent()
//                } else {
//                    // Пользователь отказал в предоставлении разрешений, можно предпринять дальнейшие действия
//                }
//            }
//        })
//
//        if (PermissionsManager.areLocationPermissionsGranted(requireContext())) {
//            // Разрешения на местоположение уже предоставлены
//            // Можно выполнить логику, требующую разрешений
//            activateLocationComponent()
//        } else {
//            permissionsManager.requestLocationPermissions(requireActivity())
//        }

//        queryEditInputText = binding.searcET
//        queryEditInputText.setOnEditorActionListener { _, actionId, _ ->
//            if (actionId == EditorInfo.IME_ACTION_DONE) {
//                val address = queryEditInputText.text.toString().trim()
//                if (address.isNotEmpty()) {
//                    geocodeAddress(address)
////                    showSearchHistory()
////                    showSearchResult()
//                    // Возвращаем true, чтобы указать, что событие обработано
//                    return@setOnEditorActionListener true
//                } else {
//                    binding.searcET.error = "Введите адрес"
//                    // Возвращаем false, чтобы указать, что событие не было обработано
//                    return@setOnEditorActionListener false
//                }
//            }
//            // Если действие не IME_ACTION_DONE, возвращаем false
//            false
//        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpMap()
//        setUpSearchView()
        setUpSearchView2()




        // Initialize SearchResultsView
        val searchResultsView = view.findViewById<SearchResultsView>(R.id.searchResultsView2)
        searchResultsView.initialize(
            SearchResultsView.Configuration(
                CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL)
            )
        )
//        // Show search history
//        showSearchHistory()

        // Пример: заполнение представления элементами (заглушка)
        val searchResults = listOf("Result 1", "Result 2", "Result 3")
        setAdapterItems(searchResults)

    }


    fun setAdapterItems(items: List<String>) {

    }

//// Show search history
//    private fun showSearchHistory() {
//        val historyDataProvider = ServiceProvider.INSTANCE.historyDataProvider()
//
//        // Show `loading` item that indicates the progress of `search history` loading operation.
//        searchResultsView.setAdapterItems(listOf(SearchResultAdapterItem.Loading))
//
//        // Load `search history`
//        loadingTask = historyDataProvider.getAll(object : CompletionCallback<List<HistoryRecord>> {
//            override fun onComplete(result: List<HistoryRecord>) {
//                val viewItems = mutableListOf<SearchResultAdapterItem>().apply {
//                    // Add `Recent searches` header
//                    add(SearchResultAdapterItem.RecentSearchesHeader)
//
//                    // Add history record items
//                    addAll(result.map { history ->
//                        SearchResultAdapterItem.History(
//                            history,
//                            isFavorite = false
//                        )
//                    })
//                }
//
//                // Show prepared items
//                searchResultsView.setAdapterItems(viewItems)
//            }
//
//            override fun onError(e: Exception) {
//                // Show error in case of failure
//                val errorItem = SearchResultAdapterItem.Error(UiError.createFromException(e))
//                searchResultsView.setAdapterItems(listOf(errorItem))
//            }
//        })
//    }
//// Show search history




    private fun setUpMap() {
        mapView = binding.mapView
        mapView.scalebar.enabled = false

        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder().center(Point.fromLngLat(21.010872, 52.220370)).pitch(0.0).zoom(10.0).bearing(0.0).build()
        )
    }

//                    вывод адреса как текст
    private fun setUpSearchView() {
        queryEditInputText = binding.searcET
        queryEditInputText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val address = queryEditInputText.text.toString().trim()
                if (address.isNotEmpty()) {
                    geocodeAddress(address)
//                    showSearchHistory()
//                    showSearchResult()
                    // Возвращаем true, чтобы указать, что событие обработано
                    return@setOnEditorActionListener true
                } else {
                    binding.searcET.error = "Введите адрес"
                    // Возвращаем false, чтобы указать, что событие не было обработано
                    return@setOnEditorActionListener false
                }
            }
            // Если действие не IME_ACTION_DONE, возвращаем false
            false
        }
    }
//                    вывод адреса как текст
    private fun setUpSearchView2() {
        // Obsługa wyników wyszukiwania
        queryEditInputText = binding.searcET
        queryEditInputText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val address = queryEditInputText.text.toString()
                val results = geocodeAddress(address) // Pobierz wyniki geokodowania


//                val address = queryEditInputText.text.toString().trim()
                if (address.isNotEmpty()) {
                    geocodeAddress(address)
//                    displayResultsAsButtons(results) // Wyświetl wyniki jako przyciski

                    // Возвращаем true, чтобы указать, что событие обработано
                    return@setOnEditorActionListener true
                } else {
                    binding.searcET.error = "Введите адрес"
                    // Возвращаем false, чтобы указать, что событие не было обработано
                    return@setOnEditorActionListener false
                }
            }
            // Если действие не IME_ACTION_DONE, возвращаем false
            false
        }
    }


    private fun geocodeAddress(address: String) {
//    private fun geocodeAddress(address: String) {
        val accessToken = getString(R.string.mapbox_access_token)

        val geocodeUrl = "https://api.mapbox.com/geocoding/v5/mapbox.places/$address.json?country=pl&proximity=ip&language=pl&access_token=$accessToken"

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(geocodeUrl)
            .build()

//        val addressAutofill = AddressAutofill.create(accessToken)

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Request", "Failed to execute request: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                response.close()

                try {
                    val jsonObject = responseBody?.let { JSONObject(it) }
                    val features = jsonObject?.getJSONArray("features")
                    val results = mutableListOf<String>()


                    if (features != null) {
                        for (i in 0 until features.length()) {
                            val feature = features.getJSONObject(i)
                            val placeName = feature.getString("place_name")
                            results.add(placeName)
                        }
                    }

//                    вывод адреса как текст
//                    requireActivity().runOnUiThread {
//                        // Очищаем предыдущие результаты
//                        binding.searchResultsTextView.text = ""
//                        // Выводим новые результаты
//                        for (result in results) {
//                            binding.searchResultsTextView.append("$result\n")
//                        }
//                    }
//                    вывод адреса как текст

//                    вывод адреса кнопками
//                    requireActivity().runOnUiThread {
//                        displayResultsAsButtons(results) // Wyświetl wyniki jako przyciski
//                    }
//                    вывод адреса кнопками



//                    // автозаполнение адреса
//                    addressAutofillUiAdapter = AddressAutofillUiAdapter(
//                        view = searchResultsView,
//                        addressAutofill = addressAutofill
//                    )
//                    addressAutofillUiAdapter.addSearchListener(object : AddressAutofillUiAdapter.SearchListener {
//
//                        override fun onSuggestionSelected(suggestion: AddressAutofillSuggestion) {
//                            selectSuggestion(
//                                suggestion,
////                                        fromReverseGeocoding = true,
//                                fromReverseGeocoding = false,
//                            )
//                        }
//
//                        override fun onSuggestionsShown(suggestions: List<AddressAutofillSuggestion>) {
//                            // Nothing to do
//                        }
//
//                        override fun onError(e: Exception) {
//                            // Nothing to do
//                        }
//                    })
//
//                    queryEditInputText.addTextChangedListener(object : TextWatcher {
//
//                        override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
//                            if (ignoreNextQueryTextUpdate) {
//                                ignoreNextQueryTextUpdate = false
//                                return
//                            }
//
//                            val query = Query.create(text.toString())
//                            if (query != null) {
//                                lifecycleScope.launch {
//                                    addressAutofillUiAdapter.search(query)
//                                }
//                            }
//                            searchResultsView.isVisible = query != null
//                        }
//////                                override fun onTextChanged(s: CharSequence, start: Int, before: Int, after: Int) {
////                                override fun onTextChanged(s: CharSequence, start: Int, before: Int, after: Int) {
//////                                    val address = binding.searcET.text.toString().trim()
//////                                    searchResultsView.search(s.toString())
//////                                    searchResultsView.toString()
////                                }
//
//
//                        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { /* not implemented */ }
//                        override fun afterTextChanged(e: Editable) { /* not implemented */ }
//                    })




//                    for (i in 0 until features!!.length()) {
//                        val feature = features.getJSONObject(i)
//                        val placeName = feature.getString("place_name")
//                        results.add(placeName)
//                    }
//
//                    activity?.runOnUiThread {
//                        searchResultsView.clearResults()
//                        searchResultsView.addResults(results)
////                        showSearchResults(results)
//                    }
//                    if (features != null) {
//                        if (features.length() > 0) {
//                            val location = features.getJSONObject(0)?.getJSONArray("center")
//                            val longitude = location?.getDouble(0)
//                            val latitude = location?.getDouble(1)
//                            val placeName = features.getJSONObject(0)?.getString("place_name")
//
//                            activity?.runOnUiThread {
//                                // Очищаем старые результаты перед добавлением новых
//                                binding.searchResultsView.removeAllViews()
//                                // Обновляем интерфейс с полученными данными
//                                val resultText = ("Широта: $latitude\nДолгота: $longitude\nМестоположение: $placeName")
//                                binding.searchResultsView.addResult(resultText)
//                            }
//                        } else {
//                            Log.e("Response", "No features found in the response.")
//                        }
//                    }

                } catch (e: JSONException) {
                    Log.e("Response", "Error parsing JSON: ${e.message}")
                }
            }


        })
    }




    fun displayResultsAsButtons(results: List<String>) {
        // Inicjalizacja RecyclerView lub innego komponentu UI
        // Ustaw adapter, który wyświetli wyniki jako przyciski
        // Przykład:
//        val adapter = SearchResultAdapter(results) { selectedResult ->
//            // Obsłuż kliknięcie na przycisk (np. przekierowanie na miejsce)
//            // `selectedResult` zawiera informacje o wybranym wyniku (np. współrzędne)
//        }
//        recyclerView.adapter = adapter


        // Выводим новые результаты как кнопки
        for (result in results) {
            val button = Button(requireContext())
            button.text = result
            binding.searchResultsView2.addView(button)
//            // Добавьте обработчик нажатия на кнопку, если необходимо
//            // Например:
//            // button.setOnClickListener { handleButtonClick(result) }
//            binding.searchResultsTextView.addView(button)
        }
    }





}