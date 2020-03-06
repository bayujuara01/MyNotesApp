package id.co.bubui.consumerapp.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.co.bubui.consumerapp.CustonOnItemClickListener
import id.co.bubui.consumerapp.NoteAddUpdateActivity
import id.co.bubui.consumerapp.R
import id.co.bubui.consumerapp.entity.Note
import kotlinx.android.synthetic.main.item_note.view.*

class NoteAdapter(private val activity: Activity) : RecyclerView.Adapter<NoteAdapter.ViewHolder>() {

    var listNotes = arrayListOf<Note>()
        set(listNotes) {
            if (listNotes.size > 0) {
                this.listNotes.clear()
            }
            this.listNotes.addAll(listNotes)
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = listNotes.size

    override fun onBindViewHolder(holder: NoteAdapter.ViewHolder, position: Int) {
        holder.bind(listNotes[position])
    }

    fun addItem(note: Note) {
        this.listNotes.add(note)
        notifyItemInserted(this.listNotes.size - 1)
    }

    fun updateItem(position: Int, note: Note) {
        this.listNotes[position] = note
        notifyItemChanged(position, note)
    }

    fun removeItem(position: Int) {
        this.listNotes.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, this.listNotes.size)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(note: Note) {
            with(itemView) {
                tv_item_title.text = note.title
                tv_item_date.text = note.date
                tv_item_description.text = note.description

                cv_item_note.setOnClickListener(
                    CustonOnItemClickListener(
                        adapterPosition,
                        object : CustonOnItemClickListener.OnItemClickCallback {
                            override fun onItemClicked(view: View, position: Int) {
                                val intent = Intent(activity, NoteAddUpdateActivity::class.java)
                                intent.putExtra(NoteAddUpdateActivity.EXTRA_POSITION, position)
                                intent.putExtra(NoteAddUpdateActivity.EXTRA_NOTE, note)
                                activity.startActivityForResult(
                                    intent,
                                    NoteAddUpdateActivity.REQUEST_UPDATE
                                )
                            }

                        })
                )
            }
        }
    }
}