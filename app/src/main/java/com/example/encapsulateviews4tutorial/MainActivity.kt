package com.example.encapsulateviews4tutorial

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.encapsulateviews4tutorial.tooltip.Tooltip
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rv_top.setHasFixedSize(false)
        val lLayout = GridLayoutManager(applicationContext, 4)
        rv_top.layoutManager = lLayout
        var rv_button_operation = AdapterRvTop(
            applicationContext,
            Arrays.asList(
                R.color.color_blue,
                R.color.color_red,
                R.color.color_green,
                R.color.color_orange
            ),
            Arrays.asList("Option 1", "Option 2", "Option 3", "Option 4")
        )
        rv_top.adapter = rv_button_operation

        rv_center.setHasFixedSize(false)
        val lLayoutC = GridLayoutManager(applicationContext, 1)
        rv_center.layoutManager = lLayoutC
        var adapter_rv_center = AdapterRvCenter(
            applicationContext,
            Arrays.asList(
                "By far, the most widely used modern version control system in the world today is Git. Git is a mature, actively maintained open source project originally developed in 2005 by Linus Torvalds, the famous creator of the Linux operating system kernel. A staggering number of software projects rely on Git for version control, including commercial projects as well as open source. Developers who have worked with Git are well represented in the pool of available software development talent and it works well on a wide range of operating systems and IDEs (Integrated Development Environments).",
                "Having a distributed architecture, Git is an example of a DVCS (hence Distributed Version Control System). Rather than have only one single place for the full version history of the software as is common in once-popular version control systems like CVS or Subversion (also known as SVN), in Git, every developer's working copy of the code is also a repository that can contain the full history of all changes.",
                "The raw performance characteristics of Git are very strong when compared to many alternatives. Committing new changes, branching, merging and comparing past versions are all optimized for performance. The algorithms implemented inside Git take advantage of deep knowledge about common attributes of real source code file trees, how they are usually modified over time and what the access patterns are."
            )
        )
        rv_center.adapter = adapter_rv_center


        rv_top.addOnItemTouchListener(
            RecyclerTouchListener(applicationContext,
                rv_top,
                object : ClickListener {
                    override fun onClick(view: View, position: Int) {
                        selectItemRv(0, position)
                    }
                })
        )

        rv_center.addOnItemTouchListener(
            RecyclerTouchListener(applicationContext,
                rv_top,
                object : ClickListener {
                    override fun onClick(view: View, position: Int) {
                        selectItemRv(1, position)
                    }
                })
        )

        tv_custom.setOnClickListener {
            var tv =
                ((root_activity.getChildAt(2) as CoordinatorLayout).findViewById<View>(R.id.tv_custom))
            setBitmapaPosition(tv, Gravity.TOP, "Custom")
        }

        tv_favorites.setOnClickListener {
            var tv =
                ((root_activity.getChildAt(2) as CoordinatorLayout).findViewById<View>(R.id.tv_favorites))
            setBitmapaPosition(tv, Gravity.TOP, "Favorite")
        }
        fab_settings.setOnClickListener {
            var tv =
                ((root_activity.getChildAt(2) as CoordinatorLayout).findViewById<View>(R.id.fab_settings))
            setBitmapaPosition(tv, Gravity.TOP, "Settings")
        }
    }

    fun selectItemRv(positionParent: Int, positionChild: Int) {
        var viewStandOut =
            (root_activity.getChildAt(positionParent) as RecyclerView).getChildAt(positionChild)
        setBitmapaPosition(viewStandOut, Gravity.BOTTOM, "Option " + (positionChild + 1))
    }

    fun setBitmapaPosition(viewStandOut: View, gravity: Int, text: String) {
        if (viewStandOut != null) {
            Tooltip.Builder(viewStandOut, R.style.Tool_tip)
                .setText(text)
                .setCornerRadius(10f)
                .setTextSize(16f)
                .setTextColor(ContextCompat.getColor(applicationContext, R.color.color_white))
                .setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.color_grey))
                .setCancelable(true)
                .setDismissOnClick(false)
                .setGravity(gravity)
                .setPadding(20)
                .show()

            val locationPoput = IntArray(2).apply { viewStandOut!!.getLocationOnScreen(this) }
            view_lock_screen.visibility = View.VISIBLE

            var resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            var restHeightStatusBar =
                (24 * applicationContext.resources.displayMetrics.density).toInt()

            val b = Bitmap.createBitmap(
                Resources.getSystem().displayMetrics.widthPixels,
                Resources.getSystem().displayMetrics.heightPixels,
                Bitmap.Config.ARGB_8888
            )
            val c = Canvas(b)
            c.translate(
                locationPoput[0].toFloat(),
                locationPoput[1].toFloat() - restHeightStatusBar
            )
            viewStandOut!!.draw(c)

            iv_lock_screen.setImageBitmap(b)
            iv_lock_screen.setOnClickListener { view_lock_screen.visibility = View.GONE }
            tv_lock_screen.setOnClickListener { view_lock_screen.visibility = View.GONE }
        }
    }
}
