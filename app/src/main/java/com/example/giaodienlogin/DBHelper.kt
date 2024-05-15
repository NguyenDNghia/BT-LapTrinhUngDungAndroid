package com.example.giaodienlogin

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class DBHelper(context: Context) : SQLiteOpenHelper(context, "user.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE users(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE,
                password TEXT,
                email TEXT UNIQUE
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    suspend fun addUser(username: String, password: String, email: String): Boolean = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val hashedPassword = hashPassword(password)
        val contentValues = ContentValues().apply {
            put("username", username)
            put("password", hashedPassword)
            put("email", email)
        }
        try {
            val result = db.insertOrThrow("users", null, contentValues)
            return@withContext (result != -1L)
        } catch (e: Exception) {
            Log.e("DBHelper", "Failed to add user: ${e.message}")
            Log.e("DBHelper", Log.getStackTraceString(e))
            return@withContext false
        } finally {
            db.close()
        }
    }

    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray(StandardCharsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun validateUser(username: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT password FROM users WHERE username=?", arrayOf(username))
        if (cursor.moveToFirst()) {
            val storedPassword = cursor.getString(cursor.getColumnIndexOrThrow("password"))
            cursor.close()
            db.close()
            return storedPassword == hashPassword(password)
        }
        cursor.close()
        db.close()
        return false
    }

    fun checkUserExists(username: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM users WHERE username=?", arrayOf(username))
        val userExists = cursor.count > 0
        Log.d("DBHelper", "checkUserExists: $username, exists: $userExists")
        cursor.close()
        db.close()
        return userExists
    }

    fun checkEmailExists(email: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM users WHERE email=?", arrayOf(email))
        val emailExists = cursor.count > 0
        Log.d("DBHelper", "checkEmailExists: $email, exists: $emailExists")
        cursor.close()
        db.close()
        return emailExists
    }
}

