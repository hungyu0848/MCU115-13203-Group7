package com.example.myapplication2

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import java.math.BigDecimal
import java.math.MathContext

class MainActivity : ComponentActivity() {
    private lateinit var expressionView: TextView
    private lateinit var inputView: TextView
    private lateinit var resultView: TextView
    private var input = "0"
    private var left: BigDecimal? = null
    private var operator: String? = null
    private var expression = ""
    private var result = ""
    private var fresh = false
    private var evaluated = false
    private var error = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        expressionView = findViewById(R.id.expressionView)
        inputView = findViewById(R.id.inputView)
        resultView = findViewById(R.id.resultView)
        intArrayOf(R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9)
            .forEachIndexed { n, id ->
                findViewById<Button>(id).setOnClickListener { digit(n.toString()) }
            }
        findViewById<Button>(R.id.btnDecimal).setOnClickListener { decimal() }
        mapOf(R.id.btnAdd to "+", R.id.btnSubtract to "-", R.id.btnMultiply to "×",
            R.id.btnDivide to "÷").forEach { (id, symbol) ->
            findViewById<Button>(id).setOnClickListener { operate(symbol) }
        }
        findViewById<Button>(R.id.btnEquals).setOnClickListener { equals() }
        findViewById<Button>(R.id.btnAC).setOnClickListener { clearAll() }
        findViewById<Button>(R.id.btnC).setOnClickListener { clearInput() }
        findViewById<Button>(R.id.btnBackspace).setOnClickListener { backspace() }
        render()
    }

    private fun digit(value: String) {
        if (error || evaluated) clearAll()
        input = if (fresh || input == "0") value else input + value
        fresh = false
        render()
    }

    private fun decimal() {
        if (error || evaluated) clearAll()
        if (fresh) {
            input = "0."
            fresh = false
        } else if (!input.contains('.')) input += "."
        render()
    }

    private fun operate(symbol: String) {
        if (error) return
        if (operator != null && !fresh) {
            val value = calculate(left ?: return, input.toBigDecimal(), operator!!) ?: return
            left = value
            input = format(value)
        } else if (operator == null) left = input.toBigDecimal()
        operator = symbol
        expression = "${format(left!!)} $symbol"
        result = ""
        fresh = true
        evaluated = false
        render()
    }

    private fun equals() {
        if (error || operator == null || fresh) return
        val a = left ?: return
        val b = input.toBigDecimal()
        val symbol = operator!!
        val value = calculate(a, b, symbol) ?: return
        expression = "${format(a)} $symbol ${format(b)} ="
        input = format(value)
        result = input
        left = null
        operator = null
        evaluated = true
        render()
    }

    private fun calculate(a: BigDecimal, b: BigDecimal, symbol: String): BigDecimal? {
        if (symbol == "÷" && b.compareTo(BigDecimal.ZERO) == 0) {
            expression = "${format(a)} ÷ ${format(b)} ="
            input = "無法除以零"
            result = ""
            error = true
            render()
            return null
        }
        return when (symbol) {
            "+" -> a.add(b)
            "-" -> a.subtract(b)
            "×" -> a.multiply(b)
            else -> a.divide(b, MathContext.DECIMAL64)
        }
    }

    private fun clearAll() {
        input = "0"
        left = null
        operator = null
        expression = ""
        result = ""
        fresh = false
        evaluated = false
        error = false
        render()
    }

    private fun clearInput() {
        if (error || evaluated) { clearAll(); return }
        input = "0"
        fresh = operator != null
        result = ""
        render()
    }

    private fun backspace() {
        if (error || evaluated) { clearAll(); return }
        if (!fresh) input = input.dropLast(1).ifEmpty { "0" }
        render()
    }

    private fun format(value: BigDecimal) = value.stripTrailingZeros().toPlainString()

    private fun render() {
        expressionView.text = expression
        inputView.text = input
        resultView.text = result
    }
}
