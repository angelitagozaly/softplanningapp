package com.example.softplanningapp.ui.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.softplanningapp.R
import com.example.softplanningapp.data.entities.Note
import com.example.softplanningapp.data.entities.NoteType
import com.example.softplanningapp.data.entities.Priority
import java.text.SimpleDateFormat
import java.util.*

class NotesAdapter(
    private val onNoteClick: (Note) -> Unit,
    private val onNoteComplete: (Note) -> Unit,
    private val onNoteDelete: (Note) -> Unit
) : ListAdapter<Note, NotesAdapter.NoteViewHolder>(NoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note_full, parent, false)
        return NoteViewHolder(view, onNoteClick, onNoteComplete, onNoteDelete)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class NoteViewHolder(
        itemView: View,
        private val onNoteClick: (Note) -> Unit,
        private val onNoteComplete: (Note) -> Unit,
        private val onNoteDelete: (Note) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val titleText: TextView = itemView.findViewById(R.id.text_note_title)
        private val contentText: TextView = itemView.findViewById(R.id.text_note_content)
        private val typeText: TextView = itemView.findViewById(R.id.text_note_type)
        private val priorityText: TextView = itemView.findViewById(R.id.text_note_priority)
        private val dateText: TextView = itemView.findViewById(R.id.text_note_date)
        private val statusIndicator: View = itemView.findViewById(R.id.indicator_status)
        private val completeButton: ImageButton = itemView.findViewById(R.id.button_complete)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.button_delete)

        private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        fun bind(note: Note) {
            titleText.text = note.title
            contentText.text = if (note.content.length > 150) {
                note.content.take(150) + "..."
            } else {
                note.content
            }

            // Note type display
            typeText.text = when (note.noteType) {
                NoteType.ACTIVITY -> "🎯 Activity"
                NoteType.IDEA_DUMP -> "💡 Idea"
            }

            // Priority display - show previous priority if completed
            val displayPriority = if (note.isCompleted && note.previousPriority != null) {
                note.previousPriority
            } else {
                note.priority
            }

            priorityText.text = when (displayPriority) {
                Priority.LOW -> "🟢 Low"
                Priority.MEDIUM -> "🟡 Medium"
                Priority.HIGH -> "🔴 High"
            }

            // Show completion status
            if (note.isCompleted) {
                priorityText.text = "✅ Completed"
                priorityText.setTextColor(itemView.context.getColor(R.color.colorSuccess))
            } else {
                priorityText.setTextColor(itemView.context.getColor(R.color.textPrimary))
            }

            // Format date
            dateText.text = dateFormatter.format(note.createdAt)

            // Completion state styling
            updateCompletionState(note.isCompleted)

            // Status indicator color
            val statusColor = if (note.isCompleted) {
                itemView.context.getColor(R.color.colorSuccess)
            } else {
                when (note.priority) {
                    Priority.HIGH -> itemView.context.getColor(R.color.colorError)
                    Priority.MEDIUM -> itemView.context.getColor(R.color.colorWarning)
                    Priority.LOW -> itemView.context.getColor(R.color.colorPrimary)
                }
            }
            statusIndicator.setBackgroundColor(statusColor)

            // Button states and tooltips
            if (note.isCompleted) {
                completeButton.setImageResource(R.drawable.ic_undo_24)
                completeButton.contentDescription = "Mark as incomplete"
            } else {
                completeButton.setImageResource(R.drawable.ic_check_24)
                completeButton.contentDescription = "Mark as completed"
            }

            // Click listeners
            itemView.setOnClickListener { onNoteClick(note) }
            completeButton.setOnClickListener { onNoteComplete(note) }
            deleteButton.setOnClickListener { onNoteDelete(note) }
        }

        private fun updateCompletionState(isCompleted: Boolean) {
            if (isCompleted) {
                titleText.paintFlags = titleText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                contentText.paintFlags = contentText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                titleText.alpha = 0.6f
                contentText.alpha = 0.6f
                typeText.alpha = 0.6f
                priorityText.alpha = 0.6f
            } else {
                titleText.paintFlags = titleText.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                contentText.paintFlags = contentText.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                titleText.alpha = 1.0f
                contentText.alpha = 1.0f
                typeText.alpha = 1.0f
                priorityText.alpha = 1.0f
            }
        }
    }

    class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }
}