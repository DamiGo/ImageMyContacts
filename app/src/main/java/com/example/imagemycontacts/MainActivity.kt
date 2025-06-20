package com.example.imagemycontacts

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide

class MainActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var contactName: TextView
    private lateinit var contactImage: ImageView
    private lateinit var deleteButton: ImageButton

    private val contacts = mutableListOf<Contact>()
    private var currentIndex = 0

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.READ_CONTACTS] == true &&
                permissions[Manifest.permission.WRITE_CONTACTS] == true
            ) {
                loadContacts()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar = findViewById(R.id.progressBar)
        contactName = findViewById(R.id.contactName)
        contactImage = findViewById(R.id.contactImage)
        deleteButton = findViewById(R.id.deleteButton)

        deleteButton.setOnClickListener { confirmDelete() }

        contactImage.setOnTouchListener(SwipeListener(this,
            onSwipeRight = { acceptImage() },
            onSwipeLeft = { rejectImage() }
        ))

        checkPermissions()
    }

    private fun checkPermissions() {
        val readGranted = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
        val writeGranted = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
        if (readGranted && writeGranted) {
            loadContacts()
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS
                )
            )
        }
    }

    private fun loadContacts() {
        contacts.clear()
        val resolver = contentResolver
        val cursor = resolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_URI
            ),
            null,
            null,
            null
        )
        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getLong(0)
                val name = it.getString(1) ?: ""
                val photo = it.getString(2)
                if (photo.isNullOrEmpty()) {
                    contacts.add(Contact(id, name))
                }
            }
        }
        progressBar.max = contacts.size
        showNextContact()
    }

    private fun showNextContact() {
        if (currentIndex >= contacts.size) {
            contactName.text = getString(R.string.app_name)
            contactImage.setImageDrawable(null)
            return
        }
        val contact = contacts[currentIndex]
        contactName.text = contact.name
        progressBar.progress = currentIndex
        loadNextImage(contact)
    }

    private fun loadNextImage(contact: Contact) {
        val query = Uri.encode(contact.name)
        val url = "https://www.google.com/search?tbm=isch&q=$query"
        // TODO: Real image fetching; here we simply load the first result page as placeholder
        Glide.with(this)
            .load(url)
            .placeholder(android.R.color.darker_gray)
            .into(contactImage)
    }

    private fun acceptImage() {
        val contact = contacts.getOrNull(currentIndex) ?: return
        // Placeholder: use loaded image url as photo
        val query = Uri.encode(contact.name)
        val url = "https://www.google.com/search?tbm=isch&q=$query"
        val values = ContentValues().apply {
            put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
            put(ContactsContract.Data.RAW_CONTACT_ID, contact.id)
            put(ContactsContract.CommonDataKinds.Photo.PHOTO_URI, url)
        }
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, values)
        currentIndex++
        showNextContact()
    }

    private fun rejectImage() {
        loadNextImage(contacts[currentIndex])
    }

    private fun confirmDelete() {
        val contact = contacts.getOrNull(currentIndex) ?: return
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.confirm_delete))
            .setPositiveButton(R.string.yes) { _, _ -> deleteContact(contact) }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun deleteContact(contact: Contact) {
        val uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contact.id)
        contentResolver.delete(uri, null, null)
        contacts.removeAt(currentIndex)
        showNextContact()
    }
}

data class Contact(val id: Long, val name: String)
