package com.example.softplanningapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.softplanningapp.R
import com.example.softplanningapp.data.entities.PlaceSearchResult
import com.google.android.material.card.MaterialCardView

class PlaceSearchAdapter(
    private val onPlaceClick: (PlaceSearchResult) -> Unit
) : ListAdapter<PlaceSearchResult, PlaceSearchAdapter.PlaceViewHolder>(PlaceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_place_search, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.card_place)
        private val textName: TextView = itemView.findViewById(R.id.text_place_name)
        private val textAddress: TextView = itemView.findViewById(R.id.text_place_address)
        private val textType: TextView = itemView.findViewById(R.id.text_place_type)
        private val textRating: TextView = itemView.findViewById(R.id.text_place_rating)

        fun bind(place: PlaceSearchResult) {
            textName.text = place.name
            textAddress.text = place.address

            // Show place type
            val type = place.types.firstOrNull()?.replace("_", " ")?.replaceFirstChar { it.uppercase() } ?: ""
            textType.text = type

            // Show rating if available
            if (place.rating != null) {
                textRating.text = "⭐ ${String.format("%.1f", place.rating)}"
                textRating.visibility = View.VISIBLE
            } else {
                textRating.visibility = View.GONE
            }

            cardView.setOnClickListener {
                onPlaceClick(place)
            }
        }
    }

    class PlaceDiffCallback : DiffUtil.ItemCallback<PlaceSearchResult>() {
        override fun areItemsTheSame(oldItem: PlaceSearchResult, newItem: PlaceSearchResult): Boolean {
            return oldItem.placeId == newItem.placeId
        }

        override fun areContentsTheSame(oldItem: PlaceSearchResult, newItem: PlaceSearchResult): Boolean {
            return oldItem == newItem
        }
    }
}