package otus.homework.customview

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.databinding.ActivityMainBinding
import java.time.Instant
import java.time.ZoneId

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var payloadList: List<Payload> = listOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        payloadList = parsePayloadFromFile(this, R.raw.payload)

        binding.pieChartView.setPayloads(payloadList)
        binding.pieChartView.setOnCategoryClickListener {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

            binding.detailChart.setData(mapPayloadToCategoryChartData(payloadList))
    }

    private fun parsePayloadFromFile(context: Context, rawResId: Int): List<Payload> {
        val inputStream = context.resources.openRawResource(rawResId)
        val json = inputStream.bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<Payload>>() {}.type
        return Gson().fromJson(json, type)
    }


    private fun mapPayloadToCategoryChartData(payloads: List<Payload>): List<DetailChartView.Data> {
        return payloads.groupBy {
            val day = Instant.ofEpochMilli(it.time)
                .atZone(ZoneId.systemDefault())
                .dayOfMonth
            day to it.category
        }.map { (key, values) ->
            DetailChartView.Data(
                day = key.first,
                sumAmount = values.sumOf { it.amount },
                category = key.second
            )
        }
    }

}