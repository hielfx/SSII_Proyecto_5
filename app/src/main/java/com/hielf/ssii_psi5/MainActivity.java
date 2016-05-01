package com.hielf.ssii_psi5;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.Toast;

import org.json.JSONArray;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<CheckBox> checkedCheckbox;

    public List<CheckBox> getCheckedCheckbox() {
        return checkedCheckbox;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                            KeyPair kp;

                            //Sign the elements
                            message = generateMessage();
                            try {
                                kp = generateKeyPair();
                                signedMessage = signMessage(message, kp.getPrivate());
                            } catch (Exception oops) {
                                //TODO: Change with other exceptions and display errors
                                oops.printStackTrace();
                            }
                            //Send the data
                            //TODO: send the message


                            Toast.makeText(getApplicationContext(), R.string.confirmed, Toast.LENGTH_SHORT).show();
                        }
                    }).setNegativeButton(R.string.cancel, null)
                    .setTitle(R.string.dialog_title);
            dialog.show();
        }
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

    private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg;
        KeyPair kp;

        kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        kp = kpg.generateKeyPair();

        return kp;
    }

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
