package com.midterm.danhba.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.midterm.danhba.data.ContactRepository

abstract class BaseActivity : AppCompatActivity() {
    protected val repository by lazy { ContactRepository.get(this) }
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); enableEdgeToEdge() }
    protected fun insets(root: View) {
        val left = root.paddingLeft; val top = root.paddingTop; val right = root.paddingRight; val bottom = root.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, i ->
            val bars = i.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.setPadding(left + bars.left, top + bars.top, right + bars.right, bottom + bars.bottom); i
        }
        ViewCompat.requestApplyInsets(root)
    }
    protected fun alive() = !isFinishing && !isDestroyed
    protected fun message(id: Int) = Toast.makeText(this, id, Toast.LENGTH_LONG).show()
}
