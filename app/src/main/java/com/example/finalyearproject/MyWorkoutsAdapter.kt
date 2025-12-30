package com.example.finalyearproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class MyWorkoutsAdapter(
    private val workouts: MutableList<Workout>,
    private val listener: OnWorkoutActionListener
) : RecyclerView.Adapter<MyWorkoutsAdapter.WorkoutViewHolder>() {

    interface OnWorkoutActionListener {
        fun onWorkoutCompleted(position: Int)
        fun onWorkoutDeleted(position: Int)
        fun onWorkoutClicked(position: Int)
        fun onWorkoutEdit(position: Int)
        fun onWorkoutShare(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_my_workout, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val workout = workouts[position]
        holder.bind(workout)
    }

    override fun getItemCount() = workouts.size

    inner class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.tv_workout_title)
        private val subtitle: TextView = itemView.findViewById(R.id.tv_workout_subtitle)
        private val progress: ProgressBar = itemView.findViewById(R.id.progress_workout)
        private val chipGroup: ChipGroup = itemView.findViewById(R.id.chip_group_tags)
        private val editButton: ImageButton = itemView.findViewById(R.id.btn_edit_workout)
        private val shareButton: ImageButton = itemView.findViewById(R.id.btn_share_workout)
        private val completeButton: ImageButton = itemView.findViewById(R.id.btn_complete_workout)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.btn_delete_workout)

        init {
            itemView.setOnClickListener {
                listener.onWorkoutClicked(adapterPosition)
            }
        }

        fun bind(workout: Workout) {
            title.text = workout.title
            val subtitleText = "${workout.difficulty} • ${workout.durationWeeks} weeks • ${workout.hoursPerDay}h/day"
            subtitle.text = subtitleText
            progress.progress = workout.progress
            chipGroup.removeAllViews()
            workout.tags.forEach { tag ->
                val chip = Chip(itemView.context)
                chip.text = tag
                chipGroup.addView(chip)
            }

            editButton.setOnClickListener { listener.onWorkoutEdit(adapterPosition) }
            shareButton.setOnClickListener { listener.onWorkoutShare(adapterPosition) }
            completeButton.setOnClickListener { listener.onWorkoutCompleted(adapterPosition) }
            deleteButton.setOnClickListener { listener.onWorkoutDeleted(adapterPosition) }
        }
    }
}