package com.example.administrator.diarynet;

import android.util.Log;

import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhanyuzhen on 2016/6/16.
 */
public class Server extends Thread {
    private boolean OutServer = false;
    private ServerSocket server;
    private final int ServerPort = 8765;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;
    private String input;
    private JSONObject AccountInfo;
    ArrayList<JSONObject> list = new ArrayList<JSONObject>();
    ArrayList<JSONObject> user_info = new ArrayList<JSONObject>();
    private Socket socket = new Socket();

    public Server(){
        try{
            server = new ServerSocket(ServerPort);
            server.setSoTimeout(20000);
        } catch(IOException e){
            System.out.println("Server fault!");
            System.out.println("IOException: " + e.toString());
        }
        String ip = getLocalIpAddress();
        System.out.println("ip = " + ip);

    }
    public void run(){
        System.out.println("Server start!");
        while(!OutServer){
            socket = null;
            try{
                synchronized (server){
                    socket = server.accept();
                }
                // socket = server.accept();
                System.out.println("Connected, InetAddress = " + socket.getInetAddress());
                socket.setSoTimeout(20000);
                inputStream = new DataInputStream(socket.getInputStream());
                outputStream = new DataOutputStream(socket.getOutputStream());

                while(!((input = inputStream.readUTF()).equals("Bye"))){
                    //client request for data
                    if(input.equals("Data")){
                        for(int i = 0; i < list.size(); i ++){
                            outputStream.writeUTF(list.get(i).toString());
                        }
                        outputStream.writeUTF("Data End");
                    }
                    //client log in
                    else if(input.equals("Login")){
                        String str_account,str_password;
                        str_account= inputStream.readUTF();
                        str_password = inputStream.readUTF();
                        boolean NOACCOUNT =true;
                        for(int i=0; i<user_info.size();i++){
                            try {
                                if(user_info.get(i).getString("account").equals(str_account)){
                                    NOACCOUNT=false;
                                    if(user_info.get(i).getString("account").equals(str_password)){
                                        outputStream.writeUTF("LogIn_Success");
                                    }
                                    else{
                                        outputStream.writeUTF("LogIn_WrongPassWord");
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        if(NOACCOUNT) {
                            outputStream.writeUTF("LogIn_NoAccount");
                        }
                    }
                    //client register
                    else if(input.equals("Register")){
                        String str_AccountInfo = inputStream.readUTF();
                        boolean repeat =false;
                        try {
                            AccountInfo = new JSONObject(str_AccountInfo);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            outputStream.writeUTF("Register_Fail");
                        }
                        for(int i=0;i<user_info.size();i++){
                            try {
                                if(user_info.get(i).getString("account").equals(AccountInfo.getString("account"))){
                                    repeat = true;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        if(repeat){
                            outputStream.writeUTF("Register_SameAccount");
                        }
                        else{
                            user_info.add(AccountInfo);
                            outputStream.writeUTF("Register_Success");
                        }
                    }
                }
                outputStream.close();
                outputStream = null;
                inputStream.close();
                inputStream = null;
                socket.close();
            } catch(IOException e){
                System.out.println("Socket fault!");
                System.out.println("IOException: " + e.toString());
            }
        }
    }
    //public void main(String args[]){
    //    (new Server()).start();
    //}
    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            System.out.println("cannot get ip");
        }
        return null;
    }
}