package com.example.weatherforecast

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherforecast.R.id.print_city
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CityList: AppCompatActivity() {

    private var cityname: MutableList<City> = ArrayList()
    private var flag = false
    private var adapter = CityAdapter(cityname)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city)

        val recyclerView = findViewById<View>(R.id.list) as RecyclerView

        recyclerView.adapter = adapter

        recyclerView.addOnItemClickListener(object : OnItemClickListener {
            override fun onItemClicked(position: Int, view: View) {
                saveData()
                start(cityname[position].name)
                finish()
            }
        })

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            builder.setTitle(R.string.add_menu)
            val dialogLayout = inflater.inflate(R.layout.context_add_city, null)
            val editText  = dialogLayout.findViewById(print_city) as EditText
            editText.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
            builder.setView(dialogLayout)
            builder.setCancelable(false)
            builder.setPositiveButton("OK") { _, _ ->
                val city = editText.text.toString()
                if (city.isNotEmpty() && !cityname.contains(City(city))) {
                    val count = adapter.itemCount
                    if (count >= 9) {
                        Toast.makeText(this, "Максимальное количество городов, очистите список", Toast.LENGTH_LONG).show()
                    } else {
                        adapter.addItem(city)
                        saveData()
                        editText.setText("")
                        adapter.notifyDataSetChanged()
                    }
                    imm.hideSoftInputFromWindow(editText.windowToken, 0)
                    saveData()
                    start(city)
                    finish()

                }
            }
                    .setNegativeButton("Отмена"){_, _ ->
                        imm.hideSoftInputFromWindow(editText.windowToken, 0)

                    }
            builder.show()
        }
        initCity()

        val swipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                adapter.removeAt(viewHolder.adapterPosition)
                saveData()

            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerView)

    }

    private fun saveData() {

        val sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(cityname)
        flag = true
        editor.putBoolean("flag", flag)
        editor.putString("task list", json)
        editor.apply()
    }

    private fun loadData() {

        val sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("task list", "")
        val type = object: TypeToken<MutableList<City>>() {}.type

        cityname = if(json == null)
            ArrayList()
        else
            gson.fromJson(json, type)
        adapter = CityAdapter(cityname)
        val recyclerView = findViewById<View>(R.id.list) as RecyclerView
        recyclerView.adapter = adapter
    }

    private fun start(city: String){
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("city", city)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun initCity(){

        val sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE)
        flag = sharedPreferences.getBoolean("flag", flag)

        if (!flag) {
            cityname.add(City("Ваше местоположение"))
            cityname.add(City("Москва"))
            cityname.add(City("Берлин"))
            cityname.add(City("Париж"))
            cityname.add(City("Лондон"))
            flag = true
        } else {
            loadData()
        }

        }

}









