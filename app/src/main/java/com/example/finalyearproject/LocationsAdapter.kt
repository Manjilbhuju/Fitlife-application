package com.example.finalyearproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LocationsAdapter(
    private val locations: List<Location>,
    private val onLocationClicked: (Location) -> Unit
    ) : RecyclerView.Adapter<LocationsAdapter.LocationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_location, parent, false)
        return LocationViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val location = locations[position]
        holder.bind(location)
    }

    override fun getItemCount() = locations.size

    inner class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.tv_location_name)
        private val coordinates: TextView = itemView.findViewById(R.id.tv_location_coordinates)

        init {
            itemView.setOnClickListener {
                 onLocationClicked(locations[adapterPosition])
            }
        }

        fun bind(location: Location) {
            name.text = location.name
            val coordinateText = "${location.latitude}° N, ${location.longitude}° E"
            coordinates.text = coordinateText
        }
    }
}