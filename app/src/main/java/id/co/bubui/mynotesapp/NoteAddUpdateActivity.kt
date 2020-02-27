package id.co.bubui.mynotesapp

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import id.co.bubui.mynotesapp.db.DatabaseContract
import id.co.bubui.mynotesapp.db.NoteHelper
import id.co.bubui.mynotesapp.entity.Note
import kotlinx.android.synthetic.main.activity_note_add_update.*
import java.text.SimpleDateFormat
import java.util.*

class NoteAddUpdateActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        const val EXTRA_POSITION = "extra_position"
        const val EXTRA_NOTE = "extra_note"

        const val REQUEST_ADD = 100
        const val RESULT_ADD = 101
        const val REQUEST_UPDATE = 200

        const val RESULT_UPDATE = 201
        const val RESULT_DELETE = 301

        const val ALERT_DIALOG_CLOSE = 10
        const val ALERT_DIALOG_DELETE = 20
    }

    private var isEdit = false
    private var note: Note? = null
    private var position: Int = 0

    private var noteHelper: NoteHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_add_update)

        noteHelper = NoteHelper.getInstance(applicationContext)
        noteHelper?.open()

        note = intent.getParcelableExtra(EXTRA_NOTE)
        if (note != null) {
            position = intent.getIntExtra(EXTRA_POSITION, 0)
            isEdit = true
        } else {
            note = Note()
        }

        val actionBarTitle: String
        val btnTitle: String

        if (isEdit) {
            actionBarTitle = "Ubah"
            btnTitle = "Update"

            note?.let { edt_title.setText(it.title) }
            note?.let { edt_description.setText(it.description) }
        } else {
            actionBarTitle = "Tambah"
            btnTitle = "Simpan"
        }

        supportActionBar?.title = actionBarTitle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        btn_submit.text = btnTitle

        btn_submit.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.btn_submit) {
            Log.d("onClick", "Clicked!")
            val title = edt_title.text.toString().trim()
            val description = edt_description.text.toString().trim()

            if (title.isEmpty()) {
                edt_title.error = "Field can't be blank"
                return
            }

            note?.title = title
            note?.description = description
            Log.d("EditText", "$title : $description")

            val intent = Intent()
            intent.putExtra(EXTRA_NOTE, note)
            intent.putExtra(EXTRA_POSITION, position)

            val values = ContentValues()
            values.put(DatabaseContract.NoteColumns.TITLE, title)
            values.put(DatabaseContract.NoteColumns.DESCRIPTION, description)

            if (isEdit) {
                val result = noteHelper?.update(note?.id.toString(), values)?.toLong()
                if (result != null) {
                    if (result > 0) {
                        setResult(RESULT_UPDATE, intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@NoteAddUpdateActivity,
                            "Gagal update Data",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                val currentDate = getCurrentDate()
                note?.date = currentDate
                values.put(DatabaseContract.NoteColumns.DATE, currentDate)
                val result = noteHelper?.insert(values)
                Log.d("isAdd", "After Result : $result")
                if (result != null) {
                    Log.d("RESULT", "$result")
                    if (result > 0) {
                        note?.id = result.toInt()
                        setResult(RESULT_ADD, intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@NoteAddUpdateActivity,
                            "Gagal menambah Data",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (isEdit) {
            menuInflater.inflate(R.menu.menu_form, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> showAlertDialog(ALERT_DIALOG_DELETE)
            android.R.id.home -> showAlertDialog(ALERT_DIALOG_CLOSE)
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showAlertDialog(type: Int) {
        val isDialogClose = type == ALERT_DIALOG_CLOSE
        val dialogTitle: String
        val dialogMessage: String

        if (isDialogClose) {
            dialogTitle = "Batal"
            dialogMessage = "Apakah anda ingin membatalkan perubahan form?"
        } else {
            dialogTitle = "Hapus Note"
            dialogMessage = "Apakah anda yakin menghapus item ini?"
        }

        val alertDialogBuilder = AlertDialog.Builder(this@NoteAddUpdateActivity)
        alertDialogBuilder.setTitle(dialogTitle)
        alertDialogBuilder
            .setMessage(dialogMessage)
            .setCancelable(false)
            .setPositiveButton("Ya") { dialog, id ->
                if (isDialogClose) {
                    finish()
                } else {
                    val result = noteHelper?.deleteById(note?.id.toString())?.toLong()
                    if (result != null) {
                        if (result > 0) {
                            val intent = Intent()
                            intent.putExtra(EXTRA_POSITION, position)
                            setResult(RESULT_DELETE, intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this@NoteAddUpdateActivity,
                                "Gagal menghapus data",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            .setNegativeButton("Tidak") { dialog, id ->
                dialog.cancel()
            }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun getCurrentDate(): String =
        SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale("id")).format(Date())

}
