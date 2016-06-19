package com.example.administrator.diarynet;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class LogIn extends AppCompatActivity {

    //server info
    private static Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private String input;
    private String Account,Password;
    private String str_AccountInfo;
    private final int port = 6666;
    /*private final String LOCALHOST = "IP";//to do

    private PrintWriter out;
    public BufferedReader in;*/
    private final static String address = "192.168.1.116:80";
    Thread thread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        Button create_account = (Button)findViewById(R.id.CREATE_ACCOUNT);
        create_account.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                int ret = register();
                if(ret == 1){
                    login(Account);
                }
                else if(ret == 2){
                    Toast.makeText(getApplicationContext(),"已有此帳號 請重新申請",Toast.LENGTH_SHORT);
                }
                else{
                    Toast.makeText(getApplicationContext(),"錯誤 請再試一次",Toast.LENGTH_SHORT);
                }
            }
        });
        Button log_in = (Button)findViewById(R.id.LOG_IN);

        log_in.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Hello",Toast.LENGTH_LONG);
                EditText ACCOUNT = (EditText)findViewById(R.id.ACCOUNT_NAME);
                EditText PASSWORD = (EditText)findViewById(R.id.PASSWORD);
                Account = new String();
                Account = ACCOUNT.getText().toString();
                Password = new String();
                Password = PASSWORD.getText().toString();

                (new Server()).start();
                thread =new Thread(Login);
                thread.start();
                try {
                    input = inputStream.readUTF();
                    if(input.equals("LogIn_Success")){
                        login(Account);
                    }
                    else if(input.equals("LogIn_WrongPassword")){
                        Toast.makeText(getApplicationContext(),"密碼錯誤",Toast.LENGTH_SHORT);
                    }
                    else if(input.equals("Login_NoAccount")){
                        Toast.makeText(getApplicationContext(),"無此帳號",Toast.LENGTH_SHORT);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private int register(){
        LayoutInflater inflater = LayoutInflater.from(this);
        final View Reg_Dialog = inflater.inflate(R.layout.activity_log_in_register,null);
        new AlertDialog.Builder(this)
                .setTitle("新增事件")
                .setView(Reg_Dialog)
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText account = (EditText) (Reg_Dialog.findViewById(R.id.ACCOUNT_ID));
                        EditText name =  (EditText) (Reg_Dialog.findViewById(R.id.NAME));
                        EditText password =  (EditText) (Reg_Dialog.findViewById(R.id.PassWord));
                        EditText phone_number =  (EditText) (Reg_Dialog.findViewById(R.id.PHONE_NUMBER));
                        String Name,Phone;
                        Account = account.getText().toString();
                        Name = name.getText().toString();
                        Password = password.getText().toString();
                        Phone = phone_number.getText().toString();
                        JSONObject AccountInfo = new JSONObject();
                        try {
                            AccountInfo.put("account",Account);
                            AccountInfo.put("name",Name);
                            AccountInfo.put("password",Password);
                            AccountInfo.put("phone",Phone);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        str_AccountInfo = new String();
                        str_AccountInfo=AccountInfo.toString();
                        Toast.makeText(LogIn.this,"註冊成功",Toast.LENGTH_LONG).show();
                    }
                })
                .show();
        (new Server()).start();
        thread = new Thread(Register);
        thread.start();
        input = new String();
        try {
            input = inputStream.readUTF();
            if(input.equals("Register_Success")){
                return 1;
            }
            else if(input.equals("Register_Fail")){
                return 0;
            }
            else if(input.equals(("Register_SameAccount"))){
                return 2;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        return 0;
    }
    private void login(String UserID){
        Intent intent = new Intent();
        intent.setClass(LogIn.this,ListOfDiary.class);
        Bundle pass_UserID = new Bundle();
        pass_UserID.putString("UserID",UserID);
        intent.putExtras(pass_UserID);
        startActivity(intent);
        LogIn.this.finish();
    }
    private Runnable Register = new Runnable() {
        @Override
        public void run() {
            //connect to Server
            socket = new Socket();
            InetSocketAddress inetSocketAddress = new InetSocketAddress(address, port);
            try{
                socket.connect(inetSocketAddress, 20000);
                System.out.println("Socket success!");
            } catch(IOException e){
                System.out.println("Socket Fault! from client");
                System.out.println("IOException: " + e.toString());
            }
            //initialize input and output stream
            try {
                inputStream = new DataInputStream(socket.getInputStream());
                outputStream = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                System.out.println("client I/O Fault!");
                System.out.println("IOException: " + e.toString());
            }
            try {
                outputStream.writeUTF("Register");
                outputStream.writeUTF(str_AccountInfo);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
    private Runnable Login = new Runnable() {
        @Override
        public void run() {

            //connect to Server
            socket = new Socket();
            InetSocketAddress inetSocketAddress = new InetSocketAddress(address, port);
            try{
                socket.connect(inetSocketAddress, 20000);
                System.out.println("Socket success!");
            } catch(IOException e){
                System.out.println("Socket Fault! from client");
                System.out.println("IOException: " + e.toString());
            }
            //initialize input and output stream
            try {
                inputStream = new DataInputStream(socket.getInputStream());
                outputStream = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                         System.out.println("client I/O Fault!");
                System.out.println("IOException: " + e.toString());
            }
            //request data list
            try {
                outputStream.writeUTF("LogIn");
                outputStream.writeUTF(Account);
                outputStream.writeUTF(Password);
                input=inputStream.readUTF();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };
}