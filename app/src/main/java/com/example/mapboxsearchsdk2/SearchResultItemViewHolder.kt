package com.example.mapboxsearchsdk2

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SearchResultItemViewHolder(
    view: View,
) : RecyclerView.ViewHolder(view) {

    val addressTextView: TextView = view.findViewById(R.id.address_text_view)
}