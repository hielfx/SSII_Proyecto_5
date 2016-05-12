package com.hielf.ssii_psi5;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ServerSelectionActivity extends AppCompatActivity {

    public final static String SERVER_IP = "com.hielf.ssii_psi5.SERVER_IP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_selection);

        Button send_ip_btn = (Button) findViewById(R.id.btn_ip_send);

        send_ip_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendIp(v);
            }
        });
    }

    private void sendIp(View v) {
        //We get the server ip and send it to the MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        EditText server_ip = (EditText) findViewById(R.id.server_ip);
        String server_ip_str = server_ip.getText().toString();
        intent.putExtra(SERVER_IP, server_ip_str);

        //We start the new activity
        startActivity(intent);
    }
}
