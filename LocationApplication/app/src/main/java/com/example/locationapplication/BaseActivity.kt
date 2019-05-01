package com.example.locationapplication

import android.support.v7.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {
    fun getRepository() = (application as LocationApp).getRepository()
}
