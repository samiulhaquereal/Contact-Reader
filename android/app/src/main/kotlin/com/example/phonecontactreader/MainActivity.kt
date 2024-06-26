package com.example.phonecontactreader

import android.content.ContentResolver
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity: FlutterActivity() {

    private val CHANNEL = "contactsChannel"

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            if (call.method == "getContacts") {
                getContacts { contacts ->
                    result.success(contacts)
                }
            } else {
                result.notImplemented()
            }
        }
    }

    private fun getContacts(callback: (List<Map<String, String>>) -> Unit) {
        val contentResolver: ContentResolver = applicationContext.contentResolver

        // Launch a coroutine in IO dispatcher
        CoroutineScope(Dispatchers.IO).launch {
            val contactsList = mutableListOf<Map<String, String>>()

            val cursor: Cursor? = contentResolver.query(
                    ContactsContract.Contacts.CONTENT_URI,
                    null,
                    null,
                    null,
                    null
            )

            cursor?.use { contactsCursor ->
                while (contactsCursor.moveToNext()) {
                    val id: String = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts._ID))
                    val name: String? = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))

                    if (!name.isNullOrBlank()) { // Check if name is not null or blank
                        if (contactsCursor.getInt(contactsCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                            val phoneCursor: Cursor? = contentResolver.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                    arrayOf(id),
                                    null
                            )

                            phoneCursor?.use { phonesCursor ->
                                while (phonesCursor.moveToNext()) {
                                    val phoneNumber: String = phonesCursor.getString(phonesCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                    val contactMap = mapOf("name" to name, "phone" to phoneNumber)
                                    contactsList.add(contactMap)
                                }
                            }
                            phoneCursor?.close()
                        }
                    }
                }
            }
            cursor?.close()

            // Return contactsList on the main thread using withContext
            withContext(Dispatchers.Main) {
                callback(contactsList)
            }
        }
    }
}
