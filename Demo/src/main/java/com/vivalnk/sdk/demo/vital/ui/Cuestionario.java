package com.vivalnk.sdk.demo.vital.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;


import com.vivalnk.sdk.demo.vital.R;

import java.util.ArrayList;
import java.util.List;

public class Cuestionario extends Activity {
    RadioGroup rgNivelResp1, rgNivelResp2, rgNivelResp3, rgNivelResp4, rgNivelResp5, rgNivelResp6,
            rgNivelResp7, rgNivelResp8, rgNivelResp9, rgNivelResp10, rgFaseJornada, rgExigenciaFisica,
            rgCargaMental;

    CheckBox cbCalor, cbFrio, cbPostura, cbRuido, cbMovimiento, cbRepetitivas, cbNinguno;
    EditText etNombre, etEdad, etSexo, etCargo, etResultPCognitiva;
    Button btnGuardar;

    List<String> factoresSeleccionados = new ArrayList<>();

    private String getRadioGroupValue(RadioGroup group){
        int groupId = group.getCheckedRadioButtonId();
        if (groupId != -1) {
            RadioButton rb = findViewById(groupId);
            return rb.getText().toString();
        }
        return "NS";
    }
    private void getCheckBoxValues(){
        if (cbCalor.isChecked()) {
            factoresSeleccionados.add("calor");
        }
        if (cbFrio.isChecked()){
            factoresSeleccionados.add("frio");
        }
//        if (cbPostura.isChecked()) {
//            factoresSeleccionados.add("postura_incomoda");
//        }
        if (cbRuido.isChecked()) {
            factoresSeleccionados.add("ruido");
        }
//        if (cbMovimiento.isChecked()) {
//            factoresSeleccionados.add("movimiento-constante");
//        }
//        if (cbRepetitivas.isChecked()) {
//            factoresSeleccionados.add("tareas-repetitivas");
//        }
        if (cbNinguno.isChecked()) {
            factoresSeleccionados.clear(); // ignorar todo lo anterior si elige 'Ninguno'
            factoresSeleccionados.add("ninguno");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cuestionario_activity); // conecta con el XML
        cbCalor = findViewById(R.id.cbCalor);
        cbFrio = findViewById(R.id.cbFrio);
        //cbPostura = findViewById(R.id.cbPostura);
        cbRuido = findViewById(R.id.cbRuido);
        //cbMovimiento = findViewById(R.id.cbMovimiento);
        //cbRepetitivas = findViewById(R.id.cbRepetitivas);
        cbNinguno = findViewById(R.id.cbNinguno);

        etNombre    =  findViewById(R.id.etNombre);
        etEdad      =  findViewById(R.id.etEdad);
        etSexo      =  findViewById(R.id.etSexo);
        etCargo     =  findViewById(R.id.etCargo);

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

        rgFaseJornada       = findViewById(R.id.rgFaseJornada);
        //rgExigenciaFisica   = findViewById(R.id.rgExigenciaFisica);
        //rgCargaMental       = findViewById(R.id.rgCargaMental);

        // CheckBoxes
        btnGuardar = findViewById(R.id.btnGuardarCuestionario);

        cbNinguno.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cbCalor.setChecked(false);
                cbFrio.setChecked(false);
                cbPostura.setChecked(false);
                cbRuido.setChecked(false);
                cbMovimiento.setChecked(false);
                cbRepetitivas.setChecked(false);
            }
        });
        View.OnClickListener desmarcarNingunoListener = v -> {
            if (cbNinguno.isChecked()) {
                cbNinguno.setChecked(false);
            }
        };

        cbCalor.setOnClickListener(desmarcarNingunoListener);
        cbFrio.setOnClickListener(desmarcarNingunoListener);
        //cbPostura.setOnClickListener(desmarcarNingunoListener);
        cbRuido.setOnClickListener(desmarcarNingunoListener);
        //cbMovimiento.setOnClickListener(desmarcarNingunoListener);
        //cbRepetitivas.setOnClickListener(desmarcarNingunoListener);

        // Cuando se presiona "Guardar"
        //////////////////////////////////////
        btnGuardar.setOnClickListener(v -> {
            String nombre   = etNombre.getText().toString();
            String Edad     = etEdad.getText().toString();
            String Sexo     = etSexo.getText().toString();
            String Cargo    = etCargo.getText().toString();

            String P1 = getRadioGroupValue(rgNivelResp1);
            String P2 = getRadioGroupValue(rgNivelResp2);
            String P3 = getRadioGroupValue(rgNivelResp3);
            String P4 = getRadioGroupValue(rgNivelResp4);
            String P5 = getRadioGroupValue(rgNivelResp5);
            String P6 = getRadioGroupValue(rgNivelResp6);
            String P7 = getRadioGroupValue(rgNivelResp7);
            String P8 = getRadioGroupValue(rgNivelResp8);
            String P9 = getRadioGroupValue(rgNivelResp9);
            String P10 = getRadioGroupValue(rgNivelResp10);

            String PuntajeJuego = etResultPCognitiva.getText().toString();

            String PA = getRadioGroupValue(rgFaseJornada);
            //String PB = getRadioGroupValue(rgCargaMental);
            //String PC = getRadioGroupValue(rgCargaMental);

            getCheckBoxValues();

            // Enviar de vuelta los resultados a DeviceMenuActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("Nombre", nombre);
            resultIntent.putExtra("Edad", Edad);
            resultIntent.putExtra("Sexo", Sexo);
            resultIntent.putExtra("Cargo", Cargo);
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
            resultIntent.putExtra("respuestaA", PA);
            //resultIntent.putExtra("respuestaB", PB);
            //resultIntent.putExtra("respuestaC", PC);

            resultIntent.putStringArrayListExtra("FactoresSeleccionados", new ArrayList<>(factoresSeleccionados));

            resultIntent.putExtra("saveAt", System.currentTimeMillis());

            setResult(Activity.RESULT_OK, resultIntent);
            finish(); // Cierra esta pantalla
        });
    }
}
