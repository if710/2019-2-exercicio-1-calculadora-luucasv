package br.ufpe.cin.android.calculadora

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

// Key for the text_info state, it is used to save the state when there is a config change
const val TEXT_INFO_STATE_KEY = "textInfo"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        restoreState(savedInstanceState)
        setButtonListeners()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(TEXT_INFO_STATE_KEY, text_info.text.toString())
        super.onSaveInstanceState(outState)
    }

    /**
     * Restores saved state, used when there is a config change
     */
    private fun restoreState(savedInstanceState: Bundle?) {
        // Restore text info value from saved state when a config change occurs
        // we don't need to do the same for text calc because it already saves its states between config changes
        if (savedInstanceState?.containsKey(TEXT_INFO_STATE_KEY) == true) {
            text_info.text = savedInstanceState.getString(TEXT_INFO_STATE_KEY)
        }
    }

    /**
     * Sets listeners for all buttons in the layout.
     */
    private fun setButtonListeners() {
        // Put views to variables making it accessible in the listener lambda functions.
        val textCalcView = text_calc
        val textInfoView = text_info

        // Set equal button listener
        btn_Equal.setOnClickListener {
            val expression = textCalcView.text.toString()
            val resultString = try {
                val result = eval(expression)
                "$expression = $result"
            } catch (e: Exception) {
                Toast.makeText(this, "Error while evaluating the expression: ${e.message}", Toast.LENGTH_LONG).show()
                "$expression = Error!"
            }
            textInfoView.text = resultString
            textCalcView.text.clear()
        }
        // Set clear button listener
        btn_Clear.setOnClickListener {
            textCalcView.text.clear()
        }
        // Set character buttons, those are the buttons that simply append their character to textCalcView
        btn_0.setOnClickListener {
            textCalcView.append("0")
        }
        btn_1.setOnClickListener {
            textCalcView.append("1")
        }
        btn_2.setOnClickListener {
            textCalcView.append("2")
        }
        btn_3.setOnClickListener {
            textCalcView.append("3")
        }
        btn_4.setOnClickListener {
            textCalcView.append("4")
        }
        btn_5.setOnClickListener {
            textCalcView.append("5")
        }
        btn_6.setOnClickListener {
            textCalcView.append("6")
        }
        btn_7.setOnClickListener {
            textCalcView.append("7")
        }
        btn_8.setOnClickListener {
            textCalcView.append("8")
        }
        btn_9.setOnClickListener {
            textCalcView.append("9")
        }
        btn_Divide.setOnClickListener {
            textCalcView.append("/")
        }
        btn_Multiply.setOnClickListener {
            textCalcView.append("*")
        }
        btn_Subtract.setOnClickListener {
            textCalcView.append("-")
        }
        btn_Add.setOnClickListener {
            textCalcView.append("+")
        }
        btn_Power.setOnClickListener {
            textCalcView.append("^")
        }
        btn_Dot.setOnClickListener {
            textCalcView.append(".")
        }
        btn_LParen.setOnClickListener {
            textCalcView.append("(")
        }
        btn_RParen.setOnClickListener {
            textCalcView.append(")")
        }
    }
    //Como usar a função:
    // eval("2+2") == 4.0
    // eval("2+3*4") = 14.0
    // eval("(2+3)*4") = 20.0
    //Fonte: https://stackoverflow.com/a/26227947
    private fun eval(str: String): Double {
        return object : Any() {
            var pos = -1
            var ch: Char = ' '
            fun nextChar() {
                val size = str.length
                ch = if ((++pos < size)) str.get(pos) else (-1).toChar()
            }

            fun eat(charToEat: Char): Boolean {
                while (ch == ' ') nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Caractere inesperado: " + ch)
                return x
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            // | number | functionName factor | factor `^` factor
            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'))
                        x += parseTerm() // adição
                    else if (eat('-'))
                        x -= parseTerm() // subtração
                    else
                        return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'))
                        x *= parseFactor() // multiplicação
                    else if (eat('/'))
                        x /= parseFactor() // divisão
                    else
                        return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+')) return parseFactor() // + unário
                if (eat('-')) return -parseFactor() // - unário
                var x: Double
                val startPos = this.pos
                if (eat('(')) { // parênteses
                    x = parseExpression()
                    eat(')')
                } else if ((ch in '0'..'9') || ch == '.') { // números
                    while ((ch in '0'..'9') || ch == '.') nextChar()
                    x = java.lang.Double.parseDouble(str.substring(startPos, this.pos))
                } else if (ch in 'a'..'z') { // funções
                    while (ch in 'a'..'z') nextChar()
                    val func = str.substring(startPos, this.pos)
                    x = parseFactor()
                    if (func == "sqrt")
                        x = Math.sqrt(x)
                    else if (func == "sin")
                        x = Math.sin(Math.toRadians(x))
                    else if (func == "cos")
                        x = Math.cos(Math.toRadians(x))
                    else if (func == "tan")
                        x = Math.tan(Math.toRadians(x))
                    else
                        throw RuntimeException("Função desconhecida: " + func)
                } else {
                    if (ch == (-1).toChar())
                        throw RuntimeException("Final inesperado da expressão")
                    else
                        throw RuntimeException("Caractere inesperado: " + ch)
                }
                if (eat('^')) x = Math.pow(x, parseFactor()) // potência
                return x
            }
        }.parse()
    }
}
