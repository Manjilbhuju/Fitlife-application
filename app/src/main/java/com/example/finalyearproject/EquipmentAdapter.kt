package com.example.finalyearproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EquipmentAdapter(private val equipmentList: List<Equipment>) : RecyclerView.Adapter<EquipmentAdapter.EquipmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipmentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_equipment, parent, false)
        return EquipmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: EquipmentViewHolder, position: Int) {
        val equipment = equipmentList[position]
        holder.bind(equipment)
    }

    override fun getItemCount() = equipmentList.size

    class EquipmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkbox_equipment)
        private val name: TextView = itemView.findViewById(R.id.tv_equipment_name)
        private val usage: TextView = itemView.findViewById(R.id.tv_equipment_usage)

        fun bind(equipment: Equipment) {
            name.text = equipment.name
            usage.text = equipment.usageInfo
            checkBox.isChecked = equipment.isChecked
        }
    }
}