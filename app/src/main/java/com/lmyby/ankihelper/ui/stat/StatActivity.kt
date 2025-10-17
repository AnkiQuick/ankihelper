package com.lmyby.ankihelper.ui.stat

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.material.chip.ChipGroup
import com.lmyby.ankihelper.R
import com.lmyby.ankihelper.data.Settings
import com.lmyby.ankihelper.data.history.HistoryStat

/**
 * Activity for displaying statistics with charts
 * Converted to Kotlin as part of Phase 5 activity migration
 */
class StatActivity : AppCompatActivity() {

    private lateinit var mHistoryStat: HistoryStat
    private lateinit var mHourChart: BarChart
    private lateinit var mLastDaysChart: LineChart
    private lateinit var lastDaySpinner: Spinner
    private lateinit var mChipGroup: ChipGroup
    private var mLastDays = 7
    private val dayMap = intArrayOf(1, 7, 30, 365, 3650)

    companion object {
        private val DARK_GREEN = Color.parseColor("#2d6d4b")
        private val DARK_PINK = Color.parseColor("#b05154")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val settings = Settings.getInstance(this)
        if (settings.getPinkThemeQ()) {
            setTheme(R.style.AppThemePink)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stat)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mHistoryStat = HistoryStat(this, mLastDays)
        mHourChart = findViewById(R.id.hourt_chart)
        mLastDaysChart = findViewById(R.id.last_days_chart)
        mChipGroup = findViewById(R.id.last_days_stat_chipgroup)
        lastDaySpinner = findViewById(R.id.spinner_last_days)

        plotData()

        lastDaySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                mLastDays = dayMap[i]
                mHistoryStat = HistoryStat(this@StatActivity, mLastDays)
                mLastDaysChart.clear()
                mHourChart.clear()
                plotData()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                // Do nothing
            }
        }

        mChipGroup.setOnCheckedStateChangeListener { chipGroup, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            when (checkedId) {
                R.id.chip_1 -> mLastDays = dayMap[0]
                R.id.chip_7 -> mLastDays = dayMap[1]
                R.id.chip_30 -> mLastDays = dayMap[2]
                R.id.chip_365 -> mLastDays = dayMap[3]
            }
            mHistoryStat = HistoryStat(this@StatActivity, mLastDays)
            mLastDaysChart.clear()
            mHourChart.clear()
            plotData()
        }
    }

    private fun plotData() {
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                val data = mutableListOf<Array<IntArray>>()
                data.add(mHistoryStat.getHourStatistics())
                data.add(mHistoryStat.getLastDaysStatistics())
                data
            }
            // UI updates automatically run on main thread
            drawHourChart(result[0])
            drawLastDaysChart(result[1])
        }
    }

    private fun drawLastDaysChart(data: Array<IntArray>) {
        val lookupEntries = mutableListOf<Entry>()
        val cardaddEntries = mutableListOf<Entry>()

        for (i in data[0].indices) {
            lookupEntries.add(Entry(i.toFloat(), data[1][i].toFloat()))
            cardaddEntries.add(Entry(i.toFloat(), data[2][i].toFloat()))
        }

        if (lookupEntries.size <= 1) {
            mLastDaysChart.visibility = View.INVISIBLE
        } else {
            mLastDaysChart.visibility = View.VISIBLE
        }

        val lineWidth = 2f
        val lineDataSet2 = LineDataSet(lookupEntries, "Lookups").apply {
            color = DARK_PINK
            this.lineWidth = lineWidth
            mode = LineDataSet.Mode.HORIZONTAL_BEZIER
            setDrawCircles(false)
            setDrawValues(false)
        }

        val lineDataSet3 = LineDataSet(cardaddEntries, "Cards").apply {
            color = DARK_GREEN
            this.lineWidth = lineWidth
            mode = LineDataSet.Mode.HORIZONTAL_BEZIER
            setDrawCircles(false)
            setDrawValues(false)
        }

        val dataSets = mutableListOf<ILineDataSet>(lineDataSet2, lineDataSet3)
        mLastDaysChart.data = LineData(dataSets)
        mLastDaysChart.description.text = ""
        mLastDaysChart.xAxis.setDrawGridLines(false)
        mLastDaysChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        mLastDaysChart.axisRight.isEnabled = false
        mLastDaysChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${(-mLastDays + value.toInt() + 1)}d"
            }
        }

        mLastDaysChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        mLastDaysChart.legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        mLastDaysChart.legend.orientation = Legend.LegendOrientation.HORIZONTAL

        mLastDaysChart.invalidate()
    }

    private fun drawHourChart(data: Array<IntArray>) {
        val lookupEntries = mutableListOf<BarEntry>()

        for (i in 0 until 24) {
            lookupEntries.add(BarEntry(i.toFloat(), floatArrayOf(data[1][i].toFloat(), data[2][i].toFloat())))
        }

        val barDataSet = BarDataSet(lookupEntries, "Bar").apply {
            stackLabels = arrayOf("Lookups", "Cards")
            setDrawValues(false)
            colors = listOf(DARK_PINK, DARK_GREEN)
        }

        val barData = BarData(barDataSet)
        mHourChart.data = barData
        mHourChart.description.text = "hour"
        mHourChart.xAxis.setDrawGridLines(false)
        mHourChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        mHourChart.axisRight.isEnabled = false
        mHourChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString()
            }
        }
        mHourChart.xAxis.labelCount = 24
        mHourChart.xAxis.axisMinimum = -0.5f
        mHourChart.xAxis.axisMaximum = 23.5f
        mHourChart.legend.isEnabled = false
        mHourChart.invalidate()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
