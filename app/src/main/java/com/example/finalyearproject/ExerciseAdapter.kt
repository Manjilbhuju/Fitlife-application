package com.example.finalyearproject

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class ExerciseAdapter(
    private val exercises: MutableList<Exercise>,
    private val listener: OnExerciseActionListener
    ) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    interface OnExerciseActionListener {
        fun onExerciseEdit(position: Int)
        fun onExerciseDelete(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exercise, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = exercises[position]
        holder.bind(exercise)
    }

    override fun getItemCount() = exercises.size

    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.tv_exercise_item_name)
        private val details: TextView = itemView.findViewById(R.id.tv_exercise_item_details)
        private val image: ImageView = itemView.findViewById(R.id.iv_exercise_image)
        private val menu: ImageView = itemView.findViewById(R.id.iv_exercise_menu)

        fun bind(exercise: Exercise) {
            name.text = exercise.name
            details.text = "${exercise.sets} sets, ${exercise.reps} reps"
            if (exercise.imagePath != null) {
                val imageFile = File(exercise.imagePath!!)
                if (imageFile.exists()) {
                    image.setImageURI(Uri.fromFile(imageFile))
                }
            } else {
                image.setImageResource(R.drawable.fitlife) // Default image
            }
            
            menu.setOnClickListener { showPopupMenu(it) }
        }

        private fun showPopupMenu(view: View) {
            val popup = PopupMenu(view.context, view)
            popup.inflate(R.menu.exercise_item_menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_edit_exercise -> {
                        listener.onExerciseEdit(adapterPosition)
                        true
                    }
                    R.id.action_delete_exercise -> {
                        listener.onExerciseDelete(adapterPosition)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }
}