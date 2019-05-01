package com.example.locationapplication

import android.app.Application

class LocationApp: Application() {
    private lateinit var repository: ReminderRepository

    override fun onCreate() {
        super.onCreate()
        repository = ReminderRepository(this)
    }

    fun getRepository() = repository
}