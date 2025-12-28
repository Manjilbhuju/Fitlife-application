package com.example.finalyearproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
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
        private val menu: ImageView = itemView.findViewById(R.id.iv_workout_actions)

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

            menu.setOnClickListener { showPopupMenu(it) }
        }

        private fun showPopupMenu(view: View) {
            val popup = PopupMenu(view.context, view)
            popup.inflate(R.menu.workout_card_menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_edit_workout -> {
                        listener.onWorkoutEdit(adapterPosition)
                        true
                    }
                    R.id.action_share_sms -> {
                        listener.onWorkoutShare(adapterPosition)
                        true
                    }
                    R.id.action_complete_workout -> {
                        listener.onWorkoutCompleted(adapterPosition)
                        true
                    }
                    R.id.action_delete_workout -> {
                        listener.onWorkoutDeleted(adapterPosition)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }
}