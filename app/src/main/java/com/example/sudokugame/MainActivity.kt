package com.example.sudokugame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.material3.*

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.example.sudokugame.ui.theme.SudokuGameTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.text2.input.rememberTextFieldState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlin.random.Random
import androidx.compose.ui.text.input.ImeAction

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SudokuGame()
        }
    }
}

@Composable
fun SudokuGame() {
    val grid = remember {mutableStateOf(initGrid())}
    val isGridValid = remember { mutableStateOf(false)}
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    //source: ChatGPT
    fun gridValid(grid: List<List<Int?>>): Boolean{
        for (i in 0..8){
            val row = grid[i].filterNotNull()
            val col = grid.map{it[i]}.filterNotNull()
            if (row.distinct().size != row.size || col.distinct().size != col.size){
                return false
            }
        }
        for (r in 0..2){
            for(c in 0..2){
                val block = mutableListOf<Int?>()
                for(i in 0..2){
                    for (j in 0..2){
                        block.add(grid[r*3+i][c*3+j])
                    }
                }
                if (block.filterNotNull().distinct().size != block.filterNotNull().size){
                    return false
                }
            }
        }
        return true
    }
    fun displaySnackbar(){
        coroutineScope.launch{
            snackbarHostState.showSnackbar("You win!")
        }
    }
    fun resetGrid(){
        grid.value = initGrid()
    }
    var selectRow by remember { mutableStateOf(-1)}
    var selectCol by remember { mutableStateOf(-1)}
    var valueEntered by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { resetGrid() },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(18.dp)
        ) {
            Text("Reset Grid")
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(9),
            modifier = Modifier
                .padding(18.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(8.dp)
        ) {
            itemsIndexed(grid.value.flatten()) { index, value ->
                val row = index / 9
                val col = index % 9
                val cellVal = value ?: " "
                val isSelected = selectRow == row && selectCol == col
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .aspectRatio(1f)
                        .background(
                            if (isSelected) Color.LightGray else Color.White
                        )
                        .clickable {
                                if(row!=0) {
                                    selectRow = row
                                    selectCol = col
                                }

                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cellVal.toString(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

        if (selectRow != -1 && selectCol != -1) {
            //Source: ChatGPT
            val focusManager = LocalFocusManager.current
            TextField(
                value = valueEntered,
                onValueChange = { valueEntered = it },
                label = { Text("Enter number (1-9)") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number,imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    println("value Entered: $valueEntered")
                    if (valueEntered.toIntOrNull() in 1..9) {

                        val updatedGrid = grid.value.map { it.toMutableList() }.toMutableList()
                        updatedGrid[selectRow][selectCol] = valueEntered.toInt()
                        grid.value = updatedGrid
                        valueEntered = ""
                        selectRow = -1
                        selectCol = -1
                        focusManager.clearFocus()
                    }
                }),
                modifier = Modifier.fillMaxWidth().padding(18.dp)
            )

        }




    if(gridValid(grid.value)){
        if(!isGridValid.value){
            isGridValid.value = true
            displaySnackbar()
        }
    }
}
fun initGrid(): List<List<Int?>>{
    val firstRow = (1..9).shuffled()
    val grid = mutableListOf(firstRow.toMutableList() as MutableList<Int?>)

    for (i in 1 until 9) {
        grid.add(mutableListOf(null, null, null, null, null, null, null, null, null)) // Empty rows
    }
    return grid
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SudokuGame()
}

