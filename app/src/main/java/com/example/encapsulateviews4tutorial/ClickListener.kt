package com.example.encapsulateviews4tutorial

import android.view.View

interface ClickListener {

    fun onClick(view: View, position: Int)
}