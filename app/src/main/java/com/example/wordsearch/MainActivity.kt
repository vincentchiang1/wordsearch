package com.example.wordsearch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), View.OnTouchListener {

    private var initX = -1f
    private var initY = -1f
    private var diffX = -1f
    private var diffY = -1f
    private var prevDiffX = -1f
    private var prevDiffY = -1f
    private var cellWidth = 0
    enum class SwipeDirection {
        Undefined,
        Vertical,
        Horizontal }

    private var direction = SwipeDirection.Undefined

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initX = event.x
                initY = event.y
                v.setBackgroundResource(R.drawable.selected_background)
            }

            MotionEvent.ACTION_MOVE -> {
                if(initX != -1f && initY != -1f){

                    val tag = v.tag.toString()
                    val tagInt = tag.toInt()
                    diffX = initX - event.x
                    diffY = initY - event.y

                    if(direction == SwipeDirection.Undefined || direction == SwipeDirection.Horizontal){
                        when {
                            diffX > cellWidth -> {
                                // left dir
                                if(prevDiffX == -1f || prevDiffX < diffX){
                                    selectLetter((tagInt - (diffX / cellWidth).toInt()).toString())
                                    direction = SwipeDirection.Horizontal
                                }

                                else if ( prevDiffX != -1f && prevDiffX > diffX){
                                    unselectLetter((tagInt - (prevDiffX / cellWidth).toInt()).toString())
                                }
                            }
                            (-1) * diffX > cellWidth -> {
                                // right dir
                                if(prevDiffX == -1f || prevDiffX > diffX){
                                    selectLetter((tagInt + -1 * (diffX / cellWidth).toInt()).toString())
                                    direction = SwipeDirection.Horizontal
                                }

                                else if ( prevDiffX != -1f && prevDiffX < diffX){
                                    unselectLetter((tagInt - (prevDiffX / cellWidth).toInt()).toString())
                                }
                            }
                        }
                    }

                    if(direction == SwipeDirection.Undefined || direction == SwipeDirection.Vertical){
                        when {
                            diffY > cellWidth -> {
                                if(prevDiffY == -1f || prevDiffY < diffY){
                                    selectLetter((tagInt - 10 * (diffY / cellWidth).toInt()).toString())
                                    direction = SwipeDirection.Vertical
                                }

                                else if (prevDiffY != -1f && prevDiffY > diffY){
                                    unselectLetter((tagInt - 10 * (diffY / cellWidth).toInt()).toString())
                                }
                            }
                            (-1) * diffY > cellWidth -> {
                                if(prevDiffY == -1f || prevDiffY > diffY){
                                    selectLetter((tagInt + -10 * (diffY / cellWidth).toInt()).toString())
                                    direction = SwipeDirection.Vertical
                                }

                                else if (prevDiffY != -1f && prevDiffY < diffY){
                                    unselectLetter((tagInt - 10 * (diffY / cellWidth).toInt()).toString())
                                }
                            }
                        }
                    }

                    prevDiffX = diffX
                    prevDiffY = diffY
                }
            }

            MotionEvent.ACTION_UP -> {
                val tag = v.tag.toString()
                val tagInt = tag.toInt()
                var finalTag = tag

                if(direction == SwipeDirection.Horizontal){
                    finalTag = when {
                        diffX > cellWidth -> {
                            (tagInt - (diffX / cellWidth).toInt()).toString()
                        }
                        -1 * diffX > cellWidth -> {
                            (tagInt + -1 * (diffX / cellWidth).toInt()).toString()
                        }

                        else -> tag
                    }
                }

                else if(direction == SwipeDirection.Vertical){
                    finalTag = when {
                        diffY > cellWidth -> {
                            (tagInt - 10 * (diffY / cellWidth).toInt()).toString()
                        }
                        -1 * diffY > cellWidth -> {
                            (tagInt + -10 * (diffY / cellWidth).toInt()).toString()
                        }

                        else -> tag
                    }
                }
                checkAnswer(v.tag.toString(), finalTag)
            }
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        cellWidth = resources.displayMetrics.widthPixels / 10

        for (i in 0 until game_board.childCount){
            val row = game_board.getChildAt(i) as LinearLayout
            for(j in 0 until row.childCount){
                row.getChildAt(j).setOnTouchListener(this)
            }
        }

        generateBoardHelper()
    }

    private fun generateBoardHelper(){
        // reset all flags to false
        letterFlag = Array(dim) {Array(dim) { false } }
        answerFlag = Array(dim) { Array(dim) { false } }
        var flag: Boolean = Random().nextInt(2) != 0

        for (i in 0 until dim){
            for (j in 0 until dim){
                // set each cell to a random letter
                boardValues[i][j] = letters[(letters.indices).random()].toString()
            }
        }

        val rand = Random()
        for (answer in 0 until answers.size){
            var isFound = false

            while(!isFound){
                var r = 0
                if(answers[answer].wordVal.length < dim){
                    r = rand.nextInt(dim - (answers[answer].wordVal.length))
                } else if (answers[answer].wordVal.length > dim){
                    // invalid word
                    break
                }

                var start = rand.nextInt(dim - 1)

                for (n in 0 until dim){
                    var _n = (n + start) % dim
                    for (i in r until r + answers[answer].wordVal.length ) {
                        if(flag){
                            if(letterFlag[_n][i] && boardValues[_n][i] != answers[answer].wordVal[i-r].toString()) {
                                break
                            } else if (i == r + answers[answer].wordVal.length - 1) {
                                isFound = true
                            }
                        } else {
                            if(letterFlag[i][_n]&& boardValues[i][_n] != answers[answer].wordVal[i-r].toString()) {
                                break
                            } else if (i == r + answers[answer].wordVal.length - 1) {
                                isFound = true
                            }
                        }
                    }
                    if(isFound) {
                        if(flag){
                            answers[answer].setLoc(_n * 10 + r, flag)
                        } else {
                            answers[answer].setLoc(r * 10 + _n, flag)
                        }

                        for (i in r until r + answers[answer].wordVal.length ) {
                            if(flag){
                                boardValues[_n][i] = answers[answer].wordVal[i-r].toString()
                                letterFlag[_n][i] = true
                            } else {
                                boardValues[i][_n] = answers[answer].wordVal[i-r].toString()
                                letterFlag[i][_n] = true
                            }
                        }
                        break
                    }
                }
                flag = !flag
            }
        }

        val childCount = game_board.childCount
        for (i in 0 until childCount){
            val row = game_board.getChildAt(i) as LinearLayout
            for (j in 0 until row.childCount){
                (row.getChildAt(j) as TextView).text = boardValues[i][j]
            }
        }

    }

    private fun selectLetter(tag: String){
        for (i in 0 until game_board.childCount){
            val row = game_board.getChildAt(i) as LinearLayout
            for (j in 0 until row.childCount){
                if(row.getChildAt(j).tag == tag){
                    row.getChildAt(j).setBackgroundResource(R.drawable.selected_background)
                    return
                }
            }
        }
    }

    private fun unselectLetter(tag: String){
        var tagInt = tag.toInt()
        for (i in 0 until game_board.childCount){
            val row = game_board.getChildAt(i) as LinearLayout
            for (t in 0 until row.childCount){
                if(row.getChildAt(t).tag == tag){
                    if(!answerFlag[tagInt / 10][tagInt % 10]){
                        row.getChildAt(t).setBackgroundResource(R.drawable.unselected_background)
                    }
                    return
                }
            }
        }
    }

    private fun unselectLetters(start: Int, end: Int, isHorizontal: Boolean){
        var _start = start
        var _end = end

        if (end < start){
            _start = end
            _end = start
        }
        if (isHorizontal){
            for (i in _start.._end){
                unselectLetter(i.toString())
            }
        }
        else{
            // step 10 to increment 10 cells horizontal since traversing vertically
            for (i in _start.._end step 10){
                unselectLetter(i.toString())
            }
        }
    }

    private fun checkAnswer(start: String, end: String){
        var isFound = false
        var foundWord = ""

        for(answer in answers){
            if(answer.checkLoc(start.toInt(), end.toInt(), direction == SwipeDirection.Horizontal)){
                if(answer.isFound){
                    initX = -1f
                    initY = -1f
                    diffX = -1f
                    diffY = -1f
                    prevDiffX = -1f
                    prevDiffY = -1f
                    direction = SwipeDirection.Undefined
                    return
                }
                flagHelper(start.toInt(), end.toInt(), direction == SwipeDirection.Horizontal)
                answer.isFound = true
                isFound = true
                foundWord = answer.wordVal
                break
            }
        }
        if (isFound){
            findViewById<TextView>(dictionary.getValue(foundWord)).setTextColor(resources.getColor(R.color.green))

        } else {
            unselectLetters(start.toInt(), end.toInt(), direction == SwipeDirection.Horizontal)
        }

        initX = -1f
        initY = -1f
        diffX = -1f
        diffY = -1f
        direction = SwipeDirection.Undefined
    }

    private fun flagHelper(start: Int, end: Int, isHorizontal: Boolean){
        var _start = start
        var _end = end

        if (end < start){
            _start = end
            _end = start
        }
        if (isHorizontal){
            for (i in _start.._end){
                answerFlag[i / 10][i % 10] = true
            }
        }
        else{
            // step 10 to increment 10 cells horizontal since traversing vertically
            for (i in _start.._end step 10){
                answerFlag[i / 10][i % 10] = true
            }
        }
    }

    companion object {
        val answers = arrayOf(Word("SWIFT"), Word("KOTLIN"), Word("OBJECTIVEC"), Word("VARIABLE"), Word("JAVA"), Word("MOBILE"))
        const val letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        const val dim = 10
        val dictionary = mapOf("SWIFT" to R.id.swift, "KOTLIN" to R.id.kotlin, "OBJECTIVEC" to R.id.objectivec, "VARIABLE" to R.id.variable, "JAVA" to R.id.java, "MOBILE" to R.id.mobile)

        // A 10x10 Array of String, all set to V
        var boardValues = Array(dim) {Array(dim) {"V"} }

        // A 10x10 Array of Boolean, all set to false, representing if the letter has been found already
        var letterFlag = Array(dim) {Array(dim) { false } }

        // A 10x10 Array of Boolean, all set to false, representing if the answer has been found already
        var answerFlag = Array(dim) { Array(dim) { false } }
    }
}
