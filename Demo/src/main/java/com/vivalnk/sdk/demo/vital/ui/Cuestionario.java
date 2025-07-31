package com.vivalnk.sdk.demo.vital.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.vivalnk.sdk.demo.vital.R;

public class Cuestionario extends Activity {
    RadioGroup rgCansado, rgNivelCansancio;
    SeekBar seekCansancio;
    TextView tvSeekValor;
    EditText etEstadoAnimo;
    EditText etNombre;
    Button btnGuardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cuestionario_activity); // conecta con el XML

        rgCansado = findViewById(R.id.rgCansado);
        rgNivelCansancio = findViewById(R.id.rgNivelCansancio);
        seekCansancio = findViewById(R.id.seekCansancio);
        tvSeekValor = findViewById(R.id.tvSeekCansancioValor);
        etEstadoAnimo = findViewById(R.id.etEstadoAnimo);
        etNombre =      findViewById(R.id.etNombre);
        btnGuardar = findViewById(R.id.btnGuardarCuestionario);

        // Mostrar el valor en texto cuando se mueve el slider
        seekCansancio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvSeekValor.setText("Valor: " + (progress + 1));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Cuando se presiona "Guardar"
        String nombre = etNombre.getText().toString();
        btnGuardar.setOnClickListener(v -> {
            // Pregunta 1: ¿Está cansado?
            String cansado = "";
            int idCansado = rgCansado.getCheckedRadioButtonId();
            if (idCansado != -1) {
                cansado = ((RadioButton)findViewById(idCansado)).getText().toString();
            }

            // Pregunta 2: Slider
            int nivelCansancioSlider = seekCansancio.getProgress() + 1;

            // Pregunta 3: Radio 1-5
            String nivelCansancio = "";
            int idNivel = rgNivelCansancio.getCheckedRadioButtonId();
            if (idNivel != -1) {
                nivelCansancio = ((RadioButton)findViewById(idNivel)).getText().toString();
            }

            // Pregunta 4: Texto
            String estadoAnimo = etEstadoAnimo.getText().toString();
            // Enviar de vuelta los resultados a DeviceMenuActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("Nombre", nombre);
            resultIntent.putExtra("respuestaCansado", cansado);
            resultIntent.putExtra("respuestaSlider", nivelCansancioSlider);
            resultIntent.putExtra("respuestaNivel", nivelCansancio);
            resultIntent.putExtra("respuestaTexto", estadoAnimo);
            setResult(Activity.RESULT_OK, resultIntent);
            finish(); // Cierra esta pantalla
        });
    }
}
