package com.example.TradewithMe

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import java.text.SimpleDateFormat


/**
 * A simple [Fragment] subclass.
 * Use the [Exchange_table.newInstance] factory method to
 * create an instance of this fragment.
 */
class Exchange_table : Fragment() {

    var usaconversionRate :String = ""
    var ukconversionRate :String = ""
    var euconversionRate :String = ""
    var japanconversionRate :String = ""
    var koreaconversionRate :String = ""
    var hongkongconversionRate :String = ""
    lateinit var usarate:TextView
    lateinit var ukrate:TextView
    lateinit var eurate:TextView
    lateinit var japanrate:TextView
    lateinit var korearate:TextView
    lateinit var hongkongrate:TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view: View
        view= inflater.inflate(R.layout.fragment_exchange_table, container, false)
        usarate = view.findViewById(R.id.usarate)
        ukrate = view.findViewById(R.id.ukrate)
        eurate = view.findViewById(R.id.eurate)
        japanrate = view.findViewById(R.id.japanrate)
        korearate = view.findViewById(R.id.korearate)
        hongkongrate = view.findViewById(R.id.hongkongrate)

        //USA pic
        val usatext :TextView = view.findViewById(R.id.usa)
        val usaimg : Drawable = usatext.context.resources.getDrawable(R.drawable.america)
        val bitmap = (usaimg as BitmapDrawable).bitmap
        val d:Drawable = BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap,150,100,true))
        usatext.setCompoundDrawablesWithIntrinsicBounds(d,null,null,null)

        //UK pic
        val uktext :TextView = view.findViewById(R.id.uk)
        val ukimg : Drawable = uktext.context.resources.getDrawable(R.drawable.uk)
        val ukbitmap = (ukimg as BitmapDrawable).bitmap
        val ukd:Drawable = BitmapDrawable(resources, Bitmap.createScaledBitmap(ukbitmap,150,100,true))
        uktext.setCompoundDrawablesWithIntrinsicBounds(ukd,null,null,null)

        //EU pic
        val eutext :TextView = view.findViewById(R.id.eu)
        val euimg : Drawable = eutext.context.resources.getDrawable(R.drawable.eu)
        val eubitmap = (euimg as BitmapDrawable).bitmap
        val eud:Drawable = BitmapDrawable(resources, Bitmap.createScaledBitmap(eubitmap,150,100,true))
        eutext.setCompoundDrawablesWithIntrinsicBounds(eud,null,null,null)

        //JAPAN pic
        val japantext :TextView = view.findViewById(R.id.japan)
        val japanimg : Drawable = japantext.context.resources.getDrawable(R.drawable.japan)
        val japanbitmap = (japanimg as BitmapDrawable).bitmap
        val japand:Drawable = BitmapDrawable(resources, Bitmap.createScaledBitmap(japanbitmap,150,100,true))
        japantext.setCompoundDrawablesWithIntrinsicBounds(japand,null,null,null)

        //Korea pic
        val koreatext :TextView = view.findViewById(R.id.korea)
        val koreaimg : Drawable = koreatext.context.resources.getDrawable(R.drawable.korea)
        val koreabitmap = (koreaimg as BitmapDrawable).bitmap
        val koread:Drawable = BitmapDrawable(resources, Bitmap.createScaledBitmap(koreabitmap,150,100,true))
        koreatext.setCompoundDrawablesWithIntrinsicBounds(koread,null,null,null)

        //Hongkong pic
        val hongkongtext :TextView = view.findViewById(R.id.hongkong)
        val hongkongimg : Drawable = hongkongtext.context.resources.getDrawable(R.drawable.hongkong)
        val hongkongbitmap = (hongkongimg as BitmapDrawable).bitmap
        val hongkongd:Drawable = BitmapDrawable(resources, Bitmap.createScaledBitmap(hongkongbitmap,150,100,true))
        hongkongtext.setCompoundDrawablesWithIntrinsicBounds(hongkongd,null,null,null)

        //Date-Time
        val thread: Thread = object : Thread() {
            override fun run() {
                try {
                    while (!this.isInterrupted) {
                        sleep(1000)
                        activity?.runOnUiThread {
                            var tdate: TextView = view.findViewById(R.id.Date)
                            var date : Long = System.currentTimeMillis()
                            var formatter = SimpleDateFormat("MMM dd yyyy\nHH:mm")
                            var datastring = formatter.format(date)
                            tdate.setText(datastring)

                        }
                    }
                } catch (e: InterruptedException) {
                }
            }
        }
        thread.start()

        getApiResult()

        return view

    }



    private fun getApiResult(){

        //USA
        var usaAPI = "https://api.ratesapi.io/api/latest?base=USD&symbols=THB"
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val usaapiResult = URL(usaAPI).readText()
                val jsonObject = JSONObject(usaapiResult)
                usaconversionRate = jsonObject.getJSONObject("rates").getString("THB")
                val usa_num = jsonObject.getJSONObject("rates").getString("THB").toFloat()
                val usa_convert = String.format("%.02f", usa_num)

                Log.d("Main", "$usaconversionRate")
                Log.d("Main", usaapiResult)

                withContext(Dispatchers.Main) {
                    usarate?.setText(usa_convert)
                }

            } catch (e: Exception) {
                Log.e("Main", "$e")
            }
        }

        //UK
        var ukAPI = "https://api.ratesapi.io/api/latest?base=GBP&symbols=THB"
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val ukapiResult = URL(ukAPI).readText()
                val jsonObject = JSONObject(ukapiResult)
                ukconversionRate = jsonObject.getJSONObject("rates").getString("THB")
                val uk_num = jsonObject.getJSONObject("rates").getString("THB").toFloat()
                val uk_convert = String.format("%.02f", uk_num)

                Log.d("Main", "$ukconversionRate")
                Log.d("Main", ukapiResult)

                withContext(Dispatchers.Main) {
                    ukrate?.setText(uk_convert)
                }

            } catch (e: Exception) {
                Log.e("Main", "$e")
            }
        }

        //UK
        var euAPI = "https://api.ratesapi.io/api/latest?base=EUR&symbols=THB"
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val euapiResult = URL(euAPI).readText()
                val jsonObject = JSONObject(euapiResult)
                euconversionRate = jsonObject.getJSONObject("rates").getString("THB")
                val eu_num = jsonObject.getJSONObject("rates").getString("THB").toFloat()
                val eu_convert = String.format("%.02f", eu_num)

                Log.d("Main", "$euconversionRate")
                Log.d("Main", euapiResult)

                withContext(Dispatchers.Main) {
                    eurate?.setText(eu_convert)
                }

            } catch (e: Exception) {
                Log.e("Main", "$e")
            }
        }

        //Japan
        var japanAPI = "https://api.ratesapi.io/api/latest?base=JPY&symbols=THB"
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val japanapiResult = URL(japanAPI).readText()
                val jsonObject = JSONObject(japanapiResult)
                japanconversionRate = jsonObject.getJSONObject("rates").getString("THB")
                val japan_num = jsonObject.getJSONObject("rates").getString("THB").toFloat()
                val japan_convert = String.format("%.02f", japan_num)

                Log.d("Main", "$japanconversionRate")
                Log.d("Main", japanapiResult)

                withContext(Dispatchers.Main) {
                    japanrate?.setText(japan_convert)
                }

            } catch (e: Exception) {
                Log.e("Main", "$e")
            }
        }

        //Korea
        var koreaAPI = "https://api.ratesapi.io/api/latest?base=KRW&symbols=THB"
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val koreaapiResult = URL(koreaAPI).readText()
                val jsonObject = JSONObject(koreaapiResult)
                koreaconversionRate = jsonObject.getJSONObject("rates").getString("THB")
                val korea_num = jsonObject.getJSONObject("rates").getString("THB").toFloat()
                val korea_convert = String.format("%.02f", korea_num)

                Log.d("Main", "$koreaconversionRate")
                Log.d("Main", koreaapiResult)

                withContext(Dispatchers.Main) {
                    korearate?.setText(korea_convert)
                }

            } catch (e: Exception) {
                Log.e("Main", "$e")
            }
        }

        //Hongkong
        var hongkongAPI = "https://api.ratesapi.io/api/latest?base=HKD&symbols=THB"
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val hongkongapiResult = URL(hongkongAPI).readText()
                val jsonObject = JSONObject(hongkongapiResult)
                hongkongconversionRate = jsonObject.getJSONObject("rates").getString("THB")
                val hongkong_num = jsonObject.getJSONObject("rates").getString("THB").toFloat()
                val hongkong_convert = String.format("%.02f", hongkong_num)

                Log.d("Main", "$hongkongconversionRate")
                Log.d("Main", hongkongapiResult)

                withContext(Dispatchers.Main) {
                    hongkongrate?.setText(hongkong_convert)
                }

            } catch (e: Exception) {
                Log.e("Main", "$e")
            }
        }



    }
}