package com.hielf.ssii_psi5;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;

import javax.net.SocketFactory;

public class MainActivity extends AppCompatActivity {

    private List<CheckBox> checkedCheckbox;

    private String serverIp;
    private Integer serverPort;

    private ProgressDialog progressDialog;
    private Handler handler;

    public List<CheckBox> getCheckedCheckbox() {
        return checkedCheckbox;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //We receive the intent and set the server ip to the value
        Intent intent = getIntent();

        serverIp = intent.getStringExtra(ServerSelectionActivity.SERVER_IP);
        serverPort = 7070;

        //We will add and remove the checkbox from this list
        checkedCheckbox = new ArrayList<CheckBox>();

        TableLayout checkboxLayout = (TableLayout) findViewById(R.id.checkboxLayout);

        Button button = (Button) findViewById(R.id.btn_send);

        //We set the same width as the table with the checkbox
        button.setWidth(checkboxLayout.getWidth());

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    private void showDialog() {
        if (!checkedCheckbox()) {
            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.select_one), Toast.LENGTH_SHORT).show();
        } else {

            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setMessage(R.string.confirm_send)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String message;
                            String signedMessage;
                            String messageToSend;
                            KeyPair kp;

                            //Sign the elements
                            message = generateMessage();
                            try {
                                kp = generateKeyPair();
                                signedMessage = signMessage(message, kp.getPrivate());
                                messageToSend = generateMessageToSend(message, kp.getPublic());//TODO: Change to signedMessage

                                //Display a loading screen
                                progressDialog = new ProgressDialog(MainActivity.this);
                                progressDialog.setTitle(getString(R.string.loading_title));
                                progressDialog.setMessage(getString(R.string.loading_message));
                                progressDialog.setCancelable(false);

                                progressDialog.show();
                                final Thread loadingThread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Thread.currentThread().interrupt();//This is inefficient but if don't do it this way the progress dialog will show too late
                                    }
                                });
                                loadingThread.start();

                                //Connect to to the server and get data in a new thread
                                Thread thread = new Thread(new ClientThread(messageToSend, serverIp, serverPort));
                                thread.start();
//                                System.out.println(responseMessage);

                            } catch (Exception oops) {
                                //TODO: Change with other exceptions and display errors
                                System.out.println(oops.getMessage());
                            }
                        }
                    }).setNegativeButton(R.string.cancel, null)
                    .setTitle(R.string.dialog_title);
            dialog.show();
        }
    }

    //<--------------------Thread to connect to the server------------------------------->
    private class ClientThread implements Runnable {

        private String messageToSend;
        private String serverIp;
        private Integer serverPort;

        public ClientThread(String messageToSend, String serverIp, Integer serverPort) {
            super();
            this.messageToSend = messageToSend;
            this.serverIp = serverIp;
            this.serverPort = serverPort;
        }

        @Override
        public void run() {
            String result;

            try {

                //Send the data
                SocketFactory socketFactory = SocketFactory.getDefault();
                Socket socket = socketFactory.createSocket(serverIp, serverPort);
                BufferedWriter outputStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                BufferedReader inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                //The "\n" is necessary because it tells the server when a line is read and receives the message
                outputStream.write(messageToSend + "\n");
                outputStream.flush();//Send the data
                result = inputStream.readLine();

                //Closing
                inputStream.close();
                outputStream.close();
                socket.close();

                progressDialog.dismiss();

                //All accord to the plan
                Toast.makeText(getApplicationContext(), R.string.confirmed, Toast.LENGTH_SHORT).show();
            } catch (Exception oops) {
                //TODO: Change with other exceptions and display errors
                System.out.println(oops.getMessage());
                result = null;

                progressDialog.dismiss();
            }

        }
    }

    // <---------------------- Methods ------------------------->

    private String generateMessageToSend(String signedMessage, PublicKey publicKey) throws JSONException {
        JSONObject result;

        result = new JSONObject();

        result.put("message", signedMessage);
        result.put("publicKey", publicKey);

        return result.toString();
    }

    private String generateMessage() {
        //We generate a JSON String. That String will be our message with the selected items
        JSONArray result;

        result = new JSONArray();
        for (int i = 0; i < checkedCheckbox.size(); i++) {
            CheckBox cb = checkedCheckbox.get(i);
            result.put(cb.getText());
        }

        return result.toString();
    }

    public void onCheckboxClicked(View v) {
        CheckBox cb = (CheckBox) v;
        if (cb.isChecked() && !checkedCheckbox.contains(cb)) {
            checkedCheckbox.add(cb);
        } else {
            checkedCheckbox.remove(v);
        }
    }

    //Method to generate a keyPair
    private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg;
        KeyPair kp;

        kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        kp = kpg.generateKeyPair();

        return kp;
    }

    //Method to sign a message
    private String signMessage(String message, PrivateKey pk) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature sg;
        byte[] signedBytes;
        String signedMessage;

        //We get the signature
        sg = Signature.getInstance("SHA256WithRSA");

        //We sign the message
        sg.initSign(pk);
        sg.update(message.getBytes());
        signedBytes = sg.sign();

        //We obtain the message converted to String in Base64
        signedMessage = Base64.encodeToString(signedBytes, Base64.DEFAULT);

        return signedMessage;
    }

    //We check if some checkbox is selected
    private boolean checkedCheckbox() {
        CheckBox guantes = (CheckBox) findViewById(R.id.cb_guantes);
        CheckBox traje = (CheckBox) findViewById(R.id.cb_traje_enfermero);
        CheckBox bisturi = (CheckBox) findViewById(R.id.cb_bisturi);
        CheckBox mascarilla = (CheckBox) findViewById(R.id.cb_mascarilla);
        CheckBox pizas = (CheckBox) findViewById(R.id.cb_pinzas);
        CheckBox agujas = (CheckBox) findViewById(R.id.cb_agujas);
        CheckBox vendas = (CheckBox) findViewById(R.id.cb_vendas);
        CheckBox camilla = (CheckBox) findViewById(R.id.cb_camilla);

        return guantes.isChecked() || traje.isChecked() || bisturi.isChecked() || mascarilla.isChecked() || pizas.isChecked() || agujas.isChecked() || vendas.isChecked() || camilla.isChecked();
    }

}
