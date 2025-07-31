package com.vivalnk.sdk.demo.vital.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;


import com.vivalnk.sdk.demo.vital.R;

public class Cuestionario extends Activity {
    RadioGroup rgNivelResp1, rgNivelResp2, rgNivelResp3, rgNivelResp4, rgNivelResp5, rgNivelResp6, rgNivelResp7, rgNivelResp8, rgNivelResp9, rgNivelResp10;
    EditText etNombre, etResultPCognitiva;
    Button btnGuardar;

    private String getRadioCroupValue(RadioGroup group){
        int groupId = group.getCheckedRadioButtonId();
        if (groupId != -1) {
            RadioButton rb = findViewById(groupId);
            return rb.getText().toString();
        }
        return "";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cuestionario_activity); // conecta con el XML

        etNombre    =  findViewById(R.id.etNombre);
        rgNivelResp1 = findViewById(R.id.rgNivelResp1);
        rgNivelResp2 = findViewById(R.id.rgNivelResp2);
        rgNivelResp3 = findViewById(R.id.rgNivelResp3);
        rgNivelResp4 = findViewById(R.id.rgNivelResp4);
        rgNivelResp5 = findViewById(R.id.rgNivelResp5);
        rgNivelResp6 = findViewById(R.id.rgNivelResp6);
        rgNivelResp7 = findViewById(R.id.rgNivelResp7);
        rgNivelResp8 = findViewById(R.id.rgNivelResp8);
        rgNivelResp9 = findViewById(R.id.rgNivelResp9);
        rgNivelResp10 = findViewById(R.id.rgNivelResp10);
        etResultPCognitiva = findViewById(R.id.etResultPCognitiva);

        btnGuardar = findViewById(R.id.btnGuardarCuestionario);

        // Cuando se presiona "Guardar"

        btnGuardar.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString();

            String P1 = getRadioCroupValue(rgNivelResp1);
            String P2 = getRadioCroupValue(rgNivelResp2);
            String P3 = getRadioCroupValue(rgNivelResp3);
            String P4 = getRadioCroupValue(rgNivelResp4);
            String P5 = getRadioCroupValue(rgNivelResp5);
            String P6 = getRadioCroupValue(rgNivelResp6);
            String P7 = getRadioCroupValue(rgNivelResp7);
            String P8 = getRadioCroupValue(rgNivelResp8);
            String P9 = getRadioCroupValue(rgNivelResp9);
            String P10 = getRadioCroupValue(rgNivelResp10);

            String PuntajeJuego = etResultPCognitiva.getText().toString();



            // Enviar de vuelta los resultados a DeviceMenuActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("Nombre", nombre);
            resultIntent.putExtra("respuesta1", P1);
            resultIntent.putExtra("respuesta2", P2);
            resultIntent.putExtra("respuesta3", P3);
            resultIntent.putExtra("respuesta4", P4);
            resultIntent.putExtra("respuesta5", P5);
            resultIntent.putExtra("respuesta6", P6);
            resultIntent.putExtra("respuesta7", P7);
            resultIntent.putExtra("respuesta8", P8);
            resultIntent.putExtra("respuesta9", P9);
            resultIntent.putExtra("respuesta10", P10);
            resultIntent.putExtra("puntajeJuego", PuntajeJuego);
            setResult(Activity.RESULT_OK, resultIntent);
            finish(); // Cierra esta pantalla
        });
    }
}
