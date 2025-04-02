package com.example.expensetracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.ui.viewmodel.OverviewViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme // Use M3 theme provider
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.of
import com.patrykandpatrick.vico.compose.common.rememberLegendItem
import com.patrykandpatrick.vico.compose.common.rememberVerticalLegend
import com.patrykandpatrick.vico.compose.pie.PieChart
import com.patrykandpatrick.vico.compose.pie.rememberPieChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.shape.Shape
import com.patrykandpatrick.vico.core.pie.data.PieChartModel
import com.patrykandpatrick.vico.core.pie.data.pieEntry
import java.text.NumberFormat
import java.util.Locale

@Composable
@Composable
fun OverviewScreen(
    viewModel: OverviewViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val numberFormat = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()), // Make content scrollable
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else if (uiState.errorMessage != null) {
            Text("Error: ${uiState.errorMessage}", color = MaterialTheme.colorScheme.error)
        } else {
            // Display Total Expenses
            Text(
                text = "Total This Month: ${numberFormat.format(uiState.totalExpensesThisMonth)}",
                style = MaterialTheme.typography.headlineSmall
            )

            // Category Spending Pie Chart
            if (uiState.categorySpending.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                         Text("Spending by Category (This Month)", style = MaterialTheme.typography.titleMedium)
                         Spacer(modifier = Modifier.height(16.dp))
                         CategoryPieChart(uiState.categorySpending)
                    }
                }
            } else {
                 Text("No spending data for category chart this month.")
            }


            // Monthly Spending Bar Chart
             if (uiState.monthlySpending.isNotEmpty()) {
                 Card(modifier = Modifier.fillMaxWidth()) {
                     Column(modifier = Modifier.padding(16.dp)) {
                         Text("Monthly Spending (This Year)", style = MaterialTheme.typography.titleMedium)
                         Spacer(modifier = Modifier.height(16.dp))
                         MonthlyBarChart(uiState.monthlySpending)
                     }
                 }
             } else {
                  Text("No spending data for monthly chart this year.")
             }
        }
    }
}

@Composable
fun CategoryPieChart(categorySpending: Map<String, Float>) {
     ProvideVicoTheme { // Use Vico's M3 theme wrapper
         val pieChartModel = remember(categorySpending) {
             PieChartModel(entries = categorySpending.map { pieEntry(key = it.key, value = it.value) })
         }
         val legendItems = remember(categorySpending) {
             categorySpending.map {
                 rememberLegendItem(
                     // TODO: Assign colors dynamically or use Vico defaults
                     icon = ShapeComponent(Shape.Pill, MaterialTheme.colorScheme.primary),
                     label = rememberTextComponent(text = it.key),
                     labelText = rememberTextComponent(text = "%.1f%%".format(it.value / categorySpending.values.sum() * 100))
                 )
             }
         }
         val legend = rememberVerticalLegend(
             items = legendItems,
             iconSize = 8.dp,
             iconPadding = Dimensions.of(end = 8.dp),
             spacing = 4.dp
         )

         PieChart(
             model = pieChartModel,
             modifier = Modifier.height(250.dp),
             legend = legend,
             // chart = rememberPieChart(slices = ...) // Customize slice appearance if needed
         )
     }
}

@Composable
fun MonthlyBarChart(monthlySpending: Map<Int, Float>) {
    ProvideVicoTheme {
        val monthValueFormatter = CartesianValueFormatter { value, _, _ ->
            // Convert float index (0f, 1f, ...) back to month number (1-12)
            val monthIndex = value.toInt() + 1 // Assuming keys are 0-based indices for the chart model
            // Simple month name mapping
            when (monthIndex) {
                1 -> "Jan"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Apr"; 5 -> "May"; 6 -> "Jun";
                7 -> "Jul"; 8 -> "Aug"; 9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; 12 -> "Dec"
                else -> ""
            }
        }
         val amountValueFormatter = CartesianValueFormatter { value, _, _ ->
            NumberFormat.getCurrencyInstance(Locale.getDefault()).format(value)
        }

        // Prepare data for Vico - needs x values (0f, 1f, ...) and y values
        val chartModel = remember(monthlySpending) {
            val sortedMonths = monthlySpending.keys.sorted()
            val entries = sortedMonths.mapIndexed { index, month ->
                // Use index as x-value for the chart
                ColumnCartesianLayerModel.Entry.of(index.toFloat(), monthlySpending[month] ?: 0f)
            }
            CartesianChartModel(ColumnCartesianLayerModel.build { series(entries) })
        }


        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(),
                startAxis = rememberStartAxis(valueFormatter = amountValueFormatter),
                bottomAxis = rememberBottomAxis(
                    valueFormatter = monthValueFormatter,
                    guideline = null // Hide bottom guideline for cleaner look
                ),
            ),
            model = chartModel,
            modifier = Modifier.height(250.dp)
        )
    }
}