package id.co.bubui.consumerapp.helper

import android.database.Cursor
import id.co.bubui.consumerapp.db.DatabaseContract
import id.co.bubui.consumerapp.entity.Note

object MappingHelper {
    fun mapCursorToArrayList(notesCursor: Cursor?): ArrayList<Note> {
        val noteList = arrayListOf<Note>()

        notesCursor?.apply {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(DatabaseContract.NoteColumns._ID))
                val title = getString(getColumnIndexOrThrow(DatabaseContract.NoteColumns.TITLE))
                val description =
                    getString(getColumnIndexOrThrow(DatabaseContract.NoteColumns.DESCRIPTION))
                val date = getString(getColumnIndexOrThrow(DatabaseContract.NoteColumns.DATE))
                noteList.add(Note(id, title, description, date))
            }
        }

        return noteList
    }

    fun mapCursorToObject(notesCursor: Cursor?): Note {
        var note = Note()
        notesCursor?.apply {
            moveToFirst()
            val id = getInt(getColumnIndexOrThrow(DatabaseContract.NoteColumns._ID))
            val title = getString(getColumnIndexOrThrow(DatabaseContract.NoteColumns.TITLE))
            val description =
                getString(getColumnIndexOrThrow(DatabaseContract.NoteColumns.DESCRIPTION))
            val date = getString(getColumnIndexOrThrow(DatabaseContract.NoteColumns.DESCRIPTION))
            note = Note(id, title, description, date)
        }
        return note
    }
}