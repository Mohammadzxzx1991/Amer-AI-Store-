package com.example.ui

import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun D3PriceChartComponent(data: String, modifier: Modifier = Modifier) {
    val html = """
        <!DOCTYPE html>
        <html>
        <head>
            <script src="https://d3js.org/d3.v7.min.js"></script>
        </head>
        <body>
            <div id="chart"></div>
            <script>
                const data = $data;
                const svg = d3.select("#chart").append("svg").attr("width", 300).attr("height", 200);
                const x = d3.scaleTime().domain(d3.extent(data, d => new Date(d.date))).range([0, 300]);
                const y = d3.scaleLinear().domain([0, d3.max(data, d => d.price)]).range([200, 0]);
                const line = d3.line().x(d => x(new Date(d.date))).y(d => y(d.price));
                svg.append("path").datum(data).attr("fill", "none").attr("stroke", "steelblue").attr("stroke-width", 1.5).attr("d", line);
            </script>
        </body>
        </html>
    """.trimIndent()

    AndroidView(
        modifier = modifier.fillMaxWidth().height(200.dp),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                loadData(html, "text/html", "UTF-8")
            }
        }
    )
}
