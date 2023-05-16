package com.example.fileexample

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager

class ShowValueDialog(private val msg: String) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext()).apply {
            setMessage(msg)
            setPositiveButton("Ok") { _, _ -> }
        }.create()
    }
}

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MyViewModel> { MyViewModelFactory(this) }
    private val pref by lazy { getSharedPreferences("MY-SETTINGS", 0) }
    private lateinit var activityForResult: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editText = findViewById<EditText>(R.id.editText)
        val buttonWriteIn = findViewById<Button>(R.id.buttonWriteIn)
        val buttonReadIn = findViewById<Button>(R.id.buttonReadIn)
        val buttonWriteExt = findViewById<Button>(R.id.buttonWriteExt)
        val buttonReadExt = findViewById<Button>(R.id.buttonReadExt)
        val buttonWritePref = findViewById<Button>(R.id.buttonWritePref)
        val buttonReadPref = findViewById<Button>(R.id.buttonReadPref)

        buttonWriteIn.setOnClickListener {
            viewModel.valueInternal = editText.text.toString()
        }

        buttonReadIn.setOnClickListener {
            ShowValueDialog(viewModel.valueInternal).show(supportFragmentManager, "ShowValueDialog")
        }

        buttonWriteExt.setOnClickListener {
            viewModel.valueExternal = editText.text.toString()

        }
        buttonReadExt.setOnClickListener {
            ShowValueDialog(viewModel.valueExternal).show(supportFragmentManager, "ShowValueDialog")
        }

        buttonWritePref.setOnClickListener {
            pref.edit {
                putString("key", editText.text.toString())

                apply()
            }
        }

        buttonReadPref.setOnClickListener {
            ShowValueDialog(pref.getString("key", "") ?: "").show(supportFragmentManager, "ShowValueDialog")
        }
    }

    override fun onStart() {
        super.onStart()
        displaySettings()
    }

    private fun displaySettings() {
        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        val signature = settings.getString("signature", "")
        val reply = settings.getString("reply", "")
        val sync = settings.getBoolean("sync", false)
        val attachment = settings.getBoolean("attachment", false)
        val str = """signature: $signature
reply: $reply
sync: $sync
attachment: $attachment
"""
        val textViewSettings = findViewById<TextView>(R.id.textViewSettings)
        textViewSettings.text = str
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}