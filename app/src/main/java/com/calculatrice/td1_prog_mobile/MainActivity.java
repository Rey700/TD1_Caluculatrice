package com.calculatrice.td1_prog_mobile;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private TextView txtDisplay;
    private String input = "";
    private double num1 = 0, num2 = 0;
    private char operator;
    private boolean isOperatorClicked = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Gérer les marges du système (barre de statut, navigation)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Récupération du TextView
        txtDisplay = findViewById(R.id.txtDisplay);

        // Récupération des boutons
        int[] numberButtons = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3,
                R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        };

        // Listener pour les chiffres
        View.OnClickListener numberClickListener = v -> {
            Button b = (Button) v;
            input += b.getText().toString();
            txtDisplay.setText(input);
        };

        // Appliquer le listener à tous les boutons de chiffres
        for (int id : numberButtons) {
            findViewById(id).setOnClickListener(numberClickListener);
        }

        // Bouton virgule
        findViewById(R.id.btnDot).setOnClickListener(v -> {
            if (!input.contains(",")) {
                if (input.isEmpty()) input = "0";
                input += ",";
                txtDisplay.setText(input);
            }
        });

        // Opérateurs
        findViewById(R.id.btnAdd).setOnClickListener(v -> setOperator('+'));
        findViewById(R.id.btnSub).setOnClickListener(v -> setOperator('-'));
        findViewById(R.id.btnMul).setOnClickListener(v -> setOperator('*'));
        findViewById(R.id.btnDiv).setOnClickListener(v -> setOperator('/'));

        // Égal
        findViewById(R.id.btnEqual).setOnClickListener(v -> calculateResult());

        // Clear
        findViewById(R.id.btnClear).setOnClickListener(v -> {
            input = "";
            txtDisplay.setText("0");
            num1 = num2 = 0;
            isOperatorClicked = false;
        });
    }

    private void setOperator(char op) {
        if (!input.isEmpty()) {
            num1 = Double.parseDouble(input.replace(",", "."));
            operator = op;
            isOperatorClicked = true;
            input = "";
        }
    }

    private void calculateResult() {
        if (!input.isEmpty() && isOperatorClicked) {
            num2 = Double.parseDouble(input.replace(",", "."));
            double result = 0;

            switch (operator) {
                case '+': result = num1 + num2; break;
                case '-': result = num1 - num2; break;
                case '*': result = num1 * num2; break;
                case '/': result = (num2 != 0) ? num1 / num2 : 0; break;
            }

            String resStr = String.valueOf(result);
            if (resStr.endsWith(".0")) resStr = resStr.replace(".0", "");

            txtDisplay.setText(resStr.replace(".", ","));
            input = resStr.replace(".", ",");
            isOperatorClicked = false;
        }
    }
}
