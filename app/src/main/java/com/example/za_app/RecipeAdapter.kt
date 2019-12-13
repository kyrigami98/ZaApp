package com.example.za_app

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso

class lieux{
    var nom = ""
    var specialite =""
    var longitude = ""
    var latitude =""
}

class CustomAdapter(private val context: Activity, private val lieux: Array<String>)
    : BaseAdapter() {
    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.list_item_recipe, null)
        val imageView = rowView.findViewById<ImageView>(R.id.recipe_list_thumbnail)
        val title = rowView.findViewById<TextView>(R.id.recipe_list_title)
        val description= rowView.findViewById<TextView>(R.id.description)

        imageView.setImageResource(R.drawable.logo)
        title.text = lieux[p0]
        description.text = lieux[p0]


        return rowView
    }

    override fun getItem(p0: Int): Any {
        return lieux.get(p0)
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return lieux.size
    }

}
