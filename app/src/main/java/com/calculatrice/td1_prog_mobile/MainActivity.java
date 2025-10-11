package com.calculatrice.td1_prog_mobile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private TextView txtDisplay;
    private String input = "";
    private final String FILE_NAME = "historique.txt";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtDisplay = findViewById(R.id.txtDisplay);

        if (savedInstanceState != null) {
            input = savedInstanceState.getString("input", "");
            txtDisplay.setText(input.isEmpty() ? "0" : input);
        } else {
            txtDisplay.setText("0");
        }

        int[] numberButtons = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3,
                R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        };
        for (int id : numberButtons) {
            findViewById(id).setOnClickListener(v -> {
                Button b = (Button) v;
                input += b.getText().toString();
                txtDisplay.setText(input);
            });
        }

        if (findViewById(R.id.btnDot) != null) {
            findViewById(R.id.btnDot).setOnClickListener(v -> appendDecimal());
        }
        if (findViewById(R.id.btnVirgDec) != null) {
            findViewById(R.id.btnVirgDec).setOnClickListener(v -> appendDecimal());
        }

        findViewById(R.id.btnAdd).setOnClickListener(v -> addOperator("+"));
        findViewById(R.id.btnSub).setOnClickListener(v -> addOperator("-"));
        findViewById(R.id.btnMul).setOnClickListener(v -> addOperator("*"));
        findViewById(R.id.btnDiv).setOnClickListener(v -> addOperator("/"));
        if (findViewById(R.id.btnPuissance) != null)
            findViewById(R.id.btnPuissance).setOnClickListener(v -> addOperator("^"));
        if (findViewById(R.id.btnParOuv) != null)
            findViewById(R.id.btnParOuv).setOnClickListener(v -> addOperator("("));
        if (findViewById(R.id.btnParFer) != null)
            findViewById(R.id.btnParFer).setOnClickListener(v -> addOperator(")"));

        if (findViewById(R.id.btnRacine) != null) {
            findViewById(R.id.btnRacine).setOnClickListener(v -> {
                input += "sqrt(";
                txtDisplay.setText(input);
            });
        }

        if (findViewById(R.id.btnUnaire) != null) {
            findViewById(R.id.btnUnaire).setOnClickListener(v -> toggleSign());
        }

        if (findViewById(R.id.btnBack) != null) {
            findViewById(R.id.btnBack).setOnClickListener(v -> {
                if (!input.isEmpty()) {
                    input = input.substring(0, input.length() - 1);
                    txtDisplay.setText(input.isEmpty() ? "0" : input);
                }
            });
        }

        findViewById(R.id.btnClear).setOnClickListener(v -> {
            input = "";
            txtDisplay.setText("0");
            saveToFile("");
        });

        findViewById(R.id.btnEqual).setOnClickListener(v -> {
            if (input.isEmpty()) return;
            try {
                String exprToEval = input.replace(",", ".");
                double result = evaluateExpression(exprToEval);
                DecimalFormat df = new DecimalFormat("0.##########");
                String resultStr = df.format(result);

                appendToFile(input + " = " + resultStr + "\n");

                txtDisplay.setText(resultStr);
                input = resultStr;
            } catch (ArithmeticException ae) {
                txtDisplay.setText("Erreur");
                input = "";
            } catch (Exception e) {
                txtDisplay.setText("Erreur");
                input = "";
            }
        });
    }

    private void appendDecimal() {
        if (input.isEmpty() || endsWithOperator(input) || input.endsWith("(")) {
            input += "0.";
            txtDisplay.setText(input);
            return;
        }
        int i = input.length() - 1;
        while (i >= 0 && (Character.isDigit(input.charAt(i)) || input.charAt(i) == '.')) i--;
        String lastNumber = input.substring(i + 1);
        if (!lastNumber.contains(".")) {
            input += ".";
            txtDisplay.setText(input);
        }
    }

    private void addOperator(String op) {
        if (input.isEmpty()) {
            if (op.equals("-")) {
                input += "-";
                txtDisplay.setText(input);
            }
            return;
        }
        if (endsWithOperator(input) && !op.equals("(") && !op.equals(")")) {
            input = input.substring(0, input.length() - 1) + op;
            txtDisplay.setText(input);
            return;
        }
        // append operator normally
        input += op;
        txtDisplay.setText(input);
    }

    private boolean endsWithOperator(String s) {
        if (s.isEmpty()) return false;
        char c = s.charAt(s.length() - 1);
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '^';
    }

    private void toggleSign() {
        if (input.isEmpty()) return;
        List<String> tokens = tokenize(input);
        if (tokens.isEmpty()) return;
        String last = tokens.get(tokens.size() - 1);
        if (isNumber(last)) {
            if (last.startsWith("-")) last = last.substring(1);
            else last = "-" + last;
            tokens.set(tokens.size() - 1, last);
            input = joinTokens(tokens);
            txtDisplay.setText(input);
        }
    }

    private double evaluateExpression(String expr) throws Exception {
        List<String> tokens = tokenize(expr);
        List<String> postfix = toPostfix(tokens);
        return evalPostfix(postfix);
    }

    private List<String> tokenize(String s) {
        List<String> tokens = new ArrayList<>();
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (Character.isWhitespace(c)) { i++; continue; }

            if ((c == '-' && (i == 0 || isOperatorChar(s.charAt(i - 1)) || s.charAt(i - 1) == '('))
                    && i + 1 < s.length() && (Character.isDigit(s.charAt(i + 1)) || s.charAt(i + 1) == '.')) {
                int j = i + 1;
                while (j < s.length() && (Character.isDigit(s.charAt(j)) || s.charAt(j) == '.')) j++;
                String num = s.substring(i, j);
                if (!tokens.isEmpty()) {
                    String prev = tokens.get(tokens.size() - 1);
                    if (prev.equals(")") || isNumber(prev)) tokens.add("*");
                }
                tokens.add(num);
                i = j;
                continue;
            }

            if (Character.isDigit(c) || c == '.') {
                int j = i;
                while (j < s.length() && (Character.isDigit(s.charAt(j)) || s.charAt(j) == '.')) j++;
                String num = s.substring(i, j);
                if (!tokens.isEmpty()) {
                    String prev = tokens.get(tokens.size() - 1);
                    if (prev.equals(")") ) tokens.add("*");
                }
                tokens.add(num);
                i = j;
                continue;
            }

            if (Character.isLetter(c)) {
                int j = i;
                while (j < s.length() && Character.isLetter(s.charAt(j))) j++;
                String word = s.substring(i, j);
                if (!tokens.isEmpty()) {
                    String prev = tokens.get(tokens.size() - 1);
                    if (prev.equals(")") || isNumber(prev)) tokens.add("*");
                }
                tokens.add(word);
                i = j;
                continue;
            }

            if (c == '(') {
                if (!tokens.isEmpty()) {
                    String prev = tokens.get(tokens.size() - 1);
                    if (prev.equals(")") || isNumber(prev)) tokens.add("*");
                }
                tokens.add("(");
                i++;
                continue;
            }
            if (c == ')') {
                tokens.add(")");
                i++;
                continue;
            }

            if (c == '+' || c == '-' || c == '*' || c == '/' || c == '^') {
                tokens.add(String.valueOf(c));
                i++;
                continue;
            }

            i++;
        }
        return tokens;
    }

    private boolean isOperatorChar(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '^';
    }

    private boolean isNumber(String token) {
        if (token == null || token.isEmpty()) return false;
        int start = (token.charAt(0) == '-') ? 1 : 0;
        boolean hasDigit = false;
        for (int i = start; i < token.length(); i++) {
            char ch = token.charAt(i);
            if (Character.isDigit(ch)) hasDigit = true;
            else if (ch == '.') continue;
            else return false;
        }
        return hasDigit;
    }

    private boolean isFunction(String token) {
        if (token == null) return false;
        return token.equals("sqrt");
    }

    private boolean isOperator(String token) {
        return token != null && (token.equals("+") || token.equals("-") || token.equals("*")
                || token.equals("/") || token.equals("^"));
    }

    private int precedence(String op) {
        if (op == null) return 0;
        switch (op) {
            case "+": case "-": return 1;
            case "*": case "/": return 2;
            case "^": return 4;
        }
        if (isFunction(op)) return 5;
        return 0;
    }

    private boolean isLeftAssociative(String op) {
        return !("^".equals(op));
    }

    private List<String> toPostfix(List<String> tokens) {
        List<String> output = new ArrayList<>();
        Stack<String> stack = new Stack<>();

        for (String token : tokens) {
            if (token.isEmpty()) continue;
            if (isNumber(token)) {
                output.add(token);
            } else if (isFunction(token)) {
                stack.push(token);
            } else if (token.equals("(")) {
                stack.push(token);
            } else if (token.equals(")")) {
                while (!stack.isEmpty() && !stack.peek().equals("(")) {
                    output.add(stack.pop());
                }
                if (!stack.isEmpty() && stack.peek().equals("(")) stack.pop();
                if (!stack.isEmpty() && isFunction(stack.peek())) output.add(stack.pop());
            } else if (isOperator(token)) {
                while (!stack.isEmpty() && (isFunction(stack.peek())
                        || (isOperator(stack.peek()) &&
                        ( (isLeftAssociative(token) && precedence(token) <= precedence(stack.peek()))
                                || (!isLeftAssociative(token) && precedence(token) < precedence(stack.peek())) )))) {
                    output.add(stack.pop());
                }
                stack.push(token);
            } else {
            }
        }

        while (!stack.isEmpty()) output.add(stack.pop());
        return output;
    }

    private double evalPostfix(List<String> postfix) throws Exception {
        Stack<Double> st = new Stack<>();
        for (String token : postfix) {
            if (isNumber(token)) {
                st.push(Double.parseDouble(token));
            } else if (isFunction(token)) {
                if (st.isEmpty()) throw new Exception("Invalid expression");
                double a = st.pop();
                if (token.equals("sqrt")) {
                    if (a < 0) throw new ArithmeticException("sqrt negative");
                    st.push(Math.sqrt(a));
                }
            } else if (isOperator(token)) {
                if (st.isEmpty()) throw new Exception("Invalid expression");
                double b = st.pop();
                double a = st.isEmpty() ? 0.0 : st.pop();
                switch (token) {
                    case "+": st.push(a + b); break;
                    case "-": st.push(a - b); break;
                    case "*": st.push(a * b); break;
                    case "/":
                        if (b == 0) throw new ArithmeticException("Division by zero");
                        st.push(a / b); break;
                    case "^": st.push(Math.pow(a, b)); break;
                    default: throw new Exception("Unknown op");
                }
            } else {
                throw new Exception("Unknown token in postfix: " + token);
            }
        }
        if (st.isEmpty()) throw new Exception("Invalid evaluation");
        return st.pop();
    }

    private void appendToFile(String data) {
        try (FileOutputStream fos = openFileOutput(FILE_NAME, Context.MODE_APPEND)) {
            fos.write(data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveToFile(String data) {
        try (FileOutputStream fos = openFileOutput(FILE_NAME, Context.MODE_PRIVATE)) {
            fos.write(data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String readFromFile() {
        StringBuilder sb = new StringBuilder();
        try (FileInputStream fis = openFileInput(FILE_NAME);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (Exception e) { }
        return sb.toString();
    }

    // rotate: keep input
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("input", input);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        input = savedInstanceState.getString("input", "");
        txtDisplay.setText(input.isEmpty() ? "0" : input);
    }

    private String joinTokens(List<String> tokens) {
        StringBuilder sb = new StringBuilder();
        for (String t : tokens) sb.append(t);
        return sb.toString();
    }
}
