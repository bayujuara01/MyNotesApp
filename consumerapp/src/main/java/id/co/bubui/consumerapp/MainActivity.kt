package id.co.bubui.consumerapp

import android.content.Intent
import android.database.ContentObserver
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import id.co.bubui.consumerapp.adapter.NoteAdapter
import id.co.bubui.consumerapp.db.DatabaseContract.NoteColumns.Companion.CONTENT_URI
import id.co.bubui.consumerapp.entity.Note
import id.co.bubui.consumerapp.helper.MappingHelper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        private const val EXTRA_STATE = "extra_state"
    }

    private lateinit var adapter: NoteAdapter
    //private var noteHelper: NoteHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.title = "Consumer Notes"

        rv_notes.layoutManager = LinearLayoutManager(this)
        rv_notes.setHasFixedSize(true)

        adapter = NoteAdapter(this)
        rv_notes.adapter = adapter

        fab_add.setOnClickListener(this)

        val handlerThread = HandlerThread("DataObserver")
        handlerThread.start()
        val handler = Handler(handlerThread.looper)

        val myObserver = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean) {
                loadNotesAsync()
            }
        }

        contentResolver.registerContentObserver(CONTENT_URI, true, myObserver)

        if (savedInstanceState != null) {
            val list = savedInstanceState.getParcelableArrayList<Note>(EXTRA_STATE)
            Log.d("savedInstanceState", list.toString())
            adapter.listNotes = list as ArrayList<Note>
        } else {
            loadNotesAsync()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList(EXTRA_STATE, adapter.listNotes)
        super.onSaveInstanceState(outState)
    }

    private fun loadNotesAsync() {
        GlobalScope.launch(Dispatchers.Main) {
            progress_bar.visibility = View.VISIBLE
            val deferredNotes = async(Dispatchers.IO) {
                val cursor = contentResolver?.query(CONTENT_URI, null, null, null, null)
                MappingHelper.mapCursorToArrayList(cursor)
            }
            progress_bar.visibility = View.GONE
            val notes = deferredNotes.await()
            if (notes.size > 0) {
                adapter.listNotes = notes
            } else {
                adapter.listNotes = ArrayList()
                showSnackbarMessage("Tidak ada data saat ini")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(
            MainActivity::class.java.simpleName,
            "requestCode : $requestCode, resultCode : $resultCode"
        )
        if (data != null) {
            when (requestCode) {
                NoteAddUpdateActivity.REQUEST_ADD -> {

                    if (resultCode == NoteAddUpdateActivity.RESULT_ADD) {
                        val note = data.getParcelableExtra<Note>(NoteAddUpdateActivity.EXTRA_NOTE)

                        adapter.addItem(note)
                        rv_notes.smoothScrollToPosition(adapter.itemCount - 1)

                        showSnackbarMessage("Satu item berhasil ditambahkan")
                    }
                }

                NoteAddUpdateActivity.REQUEST_UPDATE -> {
                    when (resultCode) {
                        NoteAddUpdateActivity.RESULT_UPDATE -> {
                            val note =
                                data.getParcelableExtra<Note>(NoteAddUpdateActivity.EXTRA_NOTE)
                            val position = data.getIntExtra(NoteAddUpdateActivity.EXTRA_POSITION, 0)

                            adapter.updateItem(position, note)
                            rv_notes.smoothScrollToPosition(position)

                            showSnackbarMessage("Satu item berhasil diubah")
                        }
                        NoteAddUpdateActivity.RESULT_DELETE -> {
                            val position = data.getIntExtra(NoteAddUpdateActivity.EXTRA_POSITION, 0)
                            adapter.removeItem(position)

                            showSnackbarMessage("Satu item berhasil dihapus")
                        }
                    }
                }
            }
        }
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.fab_add) {
            val intent = Intent(this@MainActivity, NoteAddUpdateActivity::class.java)
            startActivityForResult(intent, NoteAddUpdateActivity.REQUEST_ADD)
        }
    }

    private fun showSnackbarMessage(message: String) {
        Snackbar.make(rv_notes, message, Snackbar.LENGTH_SHORT).show()
    }

}
