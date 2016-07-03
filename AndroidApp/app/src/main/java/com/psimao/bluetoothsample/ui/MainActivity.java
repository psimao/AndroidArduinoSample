package com.psimao.bluetoothsample.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.psimao.bluetoothsample.R;
import com.psimao.bluetoothsample.bluetooth.BluetoothController;
import com.psimao.bluetoothsample.bluetooth.thread.ThreadClient;
import com.psimao.bluetoothsample.bluetooth.view.BluetoothView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements BluetoothView, View.OnClickListener, DialogInterface.OnClickListener, DialogInterface.OnCancelListener {

    private BluetoothController bluetoothController;

    private static final int RESULT_CODE_BLUETOOTH = 1;
    private static final int RESULT_CODE_PERMISSION = 2;

    private static final int MSG_TEXTO = 0;
    private static final int MSG_DESCONECTOU = 2;

    private Toolbar toolbar;
    private FloatingActionButton fab;
    private EditText etMsg;
    private TextView tvTerminal;

    ProgressDialog progressDialog;

    /**
     * Findviews, setup da actionbar, checa permissões e inicia o bluetooth e receiver
     *
     * @param savedInstanceState salvo ao rotacionar tela
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        checkPermission();
        bluetoothController = new BluetoothController(this, this);
        bluetoothController.init();
        if (!bluetoothController.isEnabled()) {
            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(i, RESULT_CODE_BLUETOOTH);
        }
    }

    /**
     * Limpa o registro do receiver
     */
    @Override
    protected void onDestroy() {
        bluetoothController.destroy();
        super.onDestroy();
    }

    /**
     * Checa se foi dado permissão para ativar Bluetooth
     *
     * @param requestCode código de quem chamou e está esperando pelo resultado
     * @param resultCode  código do resultado (ok/nok)
     * @param data        dados extras
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_CODE_BLUETOOTH:
                switch (resultCode) {
                    case RESULT_OK:
                        tvTerminal.setText("Bluetooth ativado!");
                        enableViews();
                        break;
                    case RESULT_CANCELED:
                        tvTerminal.setText("Permissão para ativar bluetooth não garantida!");
                        disableViews();
                        break;
                }
                break;
        }
    }

    /**
     * Checa permissão de Coarse Location para achar bluetooths seguros
     *
     * @param requestCode  código de quem chamou e está esperando pelo resultado
     * @param permissions  permissões dadas
     * @param grantResults status permissão
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RESULT_CODE_PERMISSION:
                for (int grantResult : grantResults) {
                    if (grantResult == PackageManager.PERMISSION_DENIED) {
                        tvTerminal.setText("Sem permissão para visualizar dispositivos Bluetooth com senha!");
                    }
                }
                break;
        }
    }

    /**
     * Cria manu escondido na action bat
     *
     * @param menu menu layout
     * @return boolean
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Quando um item do menu escondido é clicado; Busca de dispositivos bluetooth
     *
     * @param item item clicado
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                if (!bluetoothController.isEnabled()) {
                    Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(i, RESULT_CODE_BLUETOOTH);
                } else {
                    bluetoothController.startDeviceDiscovery();
                }
                break;
            case R.id.action_clear:
                tvTerminal.setText("");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Find views e setup da action bar
     */
    private void initViews() {
        this.toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.fab = (FloatingActionButton) findViewById(R.id.fab);
        this.etMsg = (EditText) findViewById(R.id.et_msg);
        this.tvTerminal = (TextView) findViewById(R.id.tv_terminal);
        setSupportActionBar(toolbar);
        this.fab.setOnClickListener(this);
    }

    /**
     * Checa permissão de Coarse Location, caso não tenha
     */
    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    RESULT_CODE_PERMISSION);
        }
    }

    /**
     * Desativa edit e button para não ocorrer envios de mensagens
     */
    private void disableViews() {
        etMsg.setEnabled(false);
        fab.setEnabled(false);
    }

    /**
     * Ativa edit e button
     */
    private void enableViews() {
        etMsg.setEnabled(true);
        fab.setEnabled(true);
    }

    /**
     * Cancela descoberta de dispositivos
     *
     * @param dialogInterface dialog cancelado
     */
    @Override
    public void onCancel(DialogInterface dialogInterface) {
        bluetoothController.cancelDeviceDiscovery();
    }

    /**
     * Envia mensagem quando clicar no FAB
     *
     * @param view FAB
     */
    @Override
    public void onClick(View view) {
        String msg = etMsg.getText().toString();
        bluetoothController.sendMessage(msg);
        String newText = "Enviado: " + msg + "\n\n" + tvTerminal.getText().toString();
        tvTerminal.setText(newText);
        etMsg.setText("");
    }

    /**
     * Dispositivo bluetooth selecionado
     *
     * @param dialogInterface dialog
     * @param which           qual dispositivo
     */
    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        dialogInterface.dismiss();
        BluetoothDevice device = bluetoothController.getDiscoveredDeviceAtPosition(which);
        String newText = "Conectando ao dispositivo " + device.getName() + "...\n\n" + tvTerminal.getText().toString();
        tvTerminal.setText(newText);
        bluetoothController.connectToDevice(bluetoothController.getDiscoveredDeviceAtPosition(which));
    }

    @Override
    public void onDeviceDiscoveryStart() {
        progressDialog = ProgressDialog.show(this, "Aguarde", "Buscando dispositivos Bluetooth", true, true, this);
    }

    @Override
    public void onDeviceDiscoveryFinished(List<BluetoothDevice> devices) {
        progressDialog.dismiss();
        String[] devicesStringArray = new String[devices.size()];
        for (int i = 0; i < devices.size(); i++) {
            devicesStringArray[i] = devices.get(i).getName();
        }
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setSingleChoiceItems(devicesStringArray, -1, this)
                .create();
        dialog.show();
    }

    @Override
    public void onMessageReceived(Message message) {
        switch (message.what) {
            case ThreadClient.HANDLER_RECEIVED_TEXT:
                String newText = message.obj.toString() + "\n" + tvTerminal.getText().toString();
                tvTerminal.setText(newText);
                break;
            case ThreadClient.HANDLER_ERROR:
                String snackText = "Erro: " + message.obj.toString();
                Snackbar.make(fab, snackText, Snackbar.LENGTH_LONG).show();
                break;
        }
    }
}
