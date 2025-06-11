package com.example.softplanningapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.softplanningapp.R
import com.example.softplanningapp.data.entities.Location
import com.google.android.material.card.MaterialCardView

class LocationsAdapter(
    private val onLocationClick: (Location) -> Unit,
    private val onLocationToggle: (Location) -> Unit,
    private val onLocationDelete: (Location) -> Unit
) : ListAdapter<Location, LocationsAdapter.LocationViewHolder>(LocationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_location, parent, false)
        return LocationViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.card_location)
        private val textName: TextView = itemView.findViewById(R.id.text_location_name)
        private val textAddress: TextView = itemView.findViewById(R.id.text_location_address)
        private val textCoordinates: TextView = itemView.findViewById(R.id.text_location_coordinates)
        private val textRadius: TextView = itemView.findViewById(R.id.text_location_radius)
        private val switchActive: Switch = itemView.findViewById(R.id.switch_location_active)
        private val buttonDelete: ImageButton = itemView.findViewById(R.id.button_delete_location)

        fun bind(location: Location) {
            textName.text = location.name
            textAddress.text = location.address
            textCoordinates.text = String.format("%.6f, %.6f", location.latitude, location.longitude)
            textRadius.text = "${location.radius}m radius"

            // Set switch without triggering listener
            switchActive.setOnCheckedChangeListener(null)
            switchActive.isChecked = location.isActive
            switchActive.setOnCheckedChangeListener { _, _ ->
                onLocationToggle(location)
            }

            // Update card appearance based on active state
            cardView.alpha = if (location.isActive) 1.0f else 0.6f

            // Click listeners
            cardView.setOnClickListener {
                onLocationClick(location)
            }

            buttonDelete.setOnClickListener {
                onLocationDelete(location)
            }
        }
    }

    class LocationDiffCallback : DiffUtil.ItemCallback<Location>() {
        override fun areItemsTheSame(oldItem: Location, newItem: Location): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Location, newItem: Location): Boolean {
            return oldItem == newItem
        }
    }
}