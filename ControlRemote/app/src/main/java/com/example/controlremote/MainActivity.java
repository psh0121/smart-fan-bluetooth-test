package com.example.controlremote;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    EditText edtTextoOut;
    ImageButton btnEnviar, btnAdlante, btnIzquierda, btnStop, btnDerecha, btnReversa;
    TextView tvtMensaje;
    Button btnDesconectar;

    Handler bluetoothIn;
    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder DataStringIN = new StringBuilder();
    private ConnectedThread MyConexionBT;

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static String address = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {

                    char MyCaracter = (char) msg.obj;

                    if (MyCaracter == 'a') {
                        tvtMensaje.setText("ACELERANDO");
                    }

                    if (MyCaracter == 'i') {
                        tvtMensaje.setText("GIRO IZQUIERDA");
                    }

                    if (MyCaracter == 'd') {
                        tvtMensaje.setText("GIRO DERECHA");
                    }

                    if (MyCaracter == 'r') {
                        tvtMensaje.setText("RETROCEDIENDO");
                    }

                    if (MyCaracter == 's') {
                        tvtMensaje.setText("DETENIDO");
                    }
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        VerificarEstadoBT();

        edtTextoOut = findViewById(R.id.edtTextoOut);
        btnEnviar = findViewById(R.id.btnEnviar);
        btnAdlante = findViewById(R.id.btnAdelante);
        btnIzquierda = findViewById(R.id.btnlzquierda);
        btnStop = findViewById(R.id.btnStop);
        btnDerecha = findViewById(R.id.btnDerecha);
        btnReversa = findViewById(R.id.btnReversa);
        tvtMensaje = findViewById(R.id.tvtMensaje);
        btnDesconectar = findViewById(R.id.btnDesconectar);

        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String GetDat = edtTextoOut.getText().toString();
                //tvtMensaje.setText(GetDat);
                MyConexionBT.write(GetDat);
            }
        });

        btnAdlante.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyConexionBT.write("A");
            }
        });

        btnIzquierda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyConexionBT.write("I");
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyConexionBT.write("S");
            }
        });

        btnDerecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyConexionBT.write("D");
            }
        });

        btnReversa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyConexionBT.write("R");
            }
        });

        btnDesconectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btSocket!=null) {
                    try {
                        btSocket.close();
                    } catch (IOException e) {
                        Toast.makeText(getBaseContext(), "Error", Toast.LENGTH_SHORT).show();
                        ;
                    }
                }
                    finish();
            }
        });

    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws  IOException {

        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = getIntent();
        address = intent.getStringExtra(DispositivosVinculados.EXTRA_DEVICE_ADDRESS);

        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        }
        catch (IOException e) {
            Toast.makeText(getBaseContext(), "La creacclon del Socket fallo", Toast.LENGTH_LONG).show();
        }

        try {
            btSocket.connect();
        }
        catch (IOException e) {
            try {
                btSocket.close();
            }
            catch (IOException e2) {}
        }
        MyConexionBT = new ConnectedThread(btSocket);
        MyConexionBT.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            btSocket.close();
        }
        catch (IOException e2) {}
    }

    private void VerificarEstadoBT() {

        if (btAdapter==null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        }
        else {
            if (btAdapter.isEnabled()) {
            }
            else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }
            catch (IOException e) {}
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] byte_in = new byte[1];

            while (true) {
                try {
                    mmInStream.read(byte_in);
                    char ch = (char) byte_in[0];
                    bluetoothIn.obtainMessage(handlerState, ch).sendToTarget();
                }
                catch (IOException e) {
                    break;
                }
            }
        }

        public void write(String input) {
            try {
                mmOutStream.write(input.getBytes());
            }
            catch (IOException e) {
                Toast.makeText(getBaseContext(), "La Conexion fallo", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

}