package app

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import io.ktor.client.engine.okhttp.OkHttpConfig
import io.ktor.client.engine.okhttp.OkHttpEngine
import io.ktor.util.InternalAPI
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import sample.R
import util.WeatherListViewAdapter
import io.WeatherApi
import util.getDeviceModel
import util.getFullDeviceInfo
import util.getTheWeather
import kotlin.coroutines.CoroutineContext

@InternalAPI // to allow usage of `OkHttpEngine` in `showTheWeather` fun
class MainActivity : AppCompatActivity(), CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job


    private fun initializeWeatherList() {
        weatherListView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = WeatherListViewAdapter()
        }
    }

    inline fun <reified T> Any.safeCast() = this as? T


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeWeatherList()

        /* Set the text with the device info */
        header.text = getDeviceModel()
        deviceInfo.text = getFullDeviceInfo()

        /* Download and show the weather info */
        showTheWeather()

        job.complete()
    }

    private fun showTheWeather() {
        // set the "loading" placeholder
        weatherDebugInfo.text = getString(R.string.loading_weather_placeholder)

        val weatherApi = WeatherApi(OkHttpEngine(OkHttpConfig()))
        launch(Dispatchers.Main) {
            val resultWeatherList = mutableListOf<WeatherApi.Weather>()
            try {
                getTheWeather(resultWeatherList, weatherApi)
                displayTheWeather(resultWeatherList)
            } catch (e: Exception) {
                weatherDebugInfo.text = e.message.toString()
            }
        }
    }

    private fun displayTheWeather(resultWeatherList: MutableList<WeatherApi.Weather>) {
        // clean the "loading" placeholder
        weatherDebugInfo.text = ""
        weatherListView.adapter?.safeCast<WeatherListViewAdapter>()
            ?.updWeatherList(resultWeatherList)
    }
}