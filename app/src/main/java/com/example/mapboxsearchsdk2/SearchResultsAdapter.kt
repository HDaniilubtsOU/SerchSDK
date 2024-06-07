package com.example.mapboxsearchsdk2

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class SearchResultsAdapter(
    private var searchResults: List<SearchResult> = emptyList(),
    private val itemClickCallback: (SearchResult) -> Unit,
) : RecyclerView.Adapter<SearchResultItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultItemViewHolder =
        SearchResultItemViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_search_result, parent, false)
        )

    override fun getItemCount(): Int = searchResults.size

    override fun onBindViewHolder(holder: SearchResultItemViewHolder, position: Int) {
        val searchResult = searchResults[position]
        holder.addressTextView.text = searchResult.address
        holder.itemView.setOnClickListener {
            itemClickCallback(searchResult)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSearchResults(searchResults: List<SearchResult>) {
        this.searchResults = searchResults
        notifyDataSetChanged()
    }
}