package com.example.giaodienlogin

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

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
        val contentValues = ContentValues().apply {
            put("username", username)
            put("password", password)
            put("email", email)
        }
        try {
            val result = db.insertOrThrow("users", null, contentValues)
            return@withContext (result != -1L)
        } catch (e: Exception) {
            Log.e("DBHelper", "Failed to add user: ${e.message}")
            Log.e("DBHelper", Log.getStackTraceString(e))  // Log the stack trace for more detailed error information.
            return@withContext false
        } finally {
            db.close()
        }
    }

    fun validateUser(username: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM users WHERE username=? AND password=?", arrayOf(username, password))
        val userExists = cursor.moveToFirst()
        cursor.close()
        db.close()
        return userExists
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

