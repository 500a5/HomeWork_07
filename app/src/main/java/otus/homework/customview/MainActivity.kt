package otus.homework.customview

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.pieChartView.setPayloads(parsePayloadFromFile(this, R.raw.payload))
        binding.pieChartView.setOnCategoryClickListener {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun parsePayloadFromFile(context: Context, rawResId: Int): List<Payload> {
        val inputStream = context.resources.openRawResource(rawResId)
        val json = inputStream.bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<Payload>>() {}.type
        return Gson().fromJson(json, type)
    }

}