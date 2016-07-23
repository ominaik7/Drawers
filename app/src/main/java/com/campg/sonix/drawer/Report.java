package com.campg.sonix.drawer;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

public class Report extends  android.app.Fragment {
Button send;
    EditText nam,email,report;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.report, container, false);
        send=(Button) rootView.findViewById(R.id.send);
        nam=(EditText) rootView.findViewById(R.id.name);
        email=(EditText) rootView.findViewById(R.id.email);
        report=(EditText) rootView.findViewById(R.id.con);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msgsend="\n--------------------------------------------\n"+"Name :"+nam.getText().toString()+"\n"+"Email :"+ email.getText().toString()+"\n"+"Report :"+"\n"+report.getText().toString()+"\n--------------------------------------------";
                final String username = "shbhmambavale7@gmail.com";
                final String password = "";

                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");

                Session session = Session.getInstance(props,
                        new javax.mail.Authenticator() {
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(username, password);
                            }
                        });
                try {
                    MimeMessage message = new MimeMessage(session);
                    DataHandler handler = new DataHandler(new ByteArrayDataSource(
                            msgsend.getBytes(), "text/plain"));
                    message.setSubject("Report");
                    message.setDataHandler(handler);
                    message.setFrom(new InternetAddress("shbhmambavale7@gmail.com"));
                    message.setRecipients(Message.RecipientType.TO,
                            InternetAddress.parse("omkarnaik8888@gmail.com"));
                    new send().execute(message);

                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
                catch (Exception e) {
                    Log.e("","exp: ",e);
                }
            }
        });

        return rootView;

    }
    class send extends AsyncTask<Message, Void, Void >
    {
        int h=0;
        @Override
        protected void onPreExecute() {
            Toast.makeText(getActivity(),"Sending",Toast.LENGTH_SHORT).show();

        }
        private boolean isNetworkAvailable(Context ctx) {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null;
        }

        public boolean hasActiveInternetConnection(Context context) {
            if (isNetworkAvailable(context)) {
                try {
                    HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                    urlc.setRequestProperty("User-Agent", "Test");
                    urlc.setRequestProperty("Connection", "close");
                    urlc.setConnectTimeout(1500);
                    urlc.connect();
                    return (urlc.getResponseCode() == 200);
                } catch (IOException e) {
                    Log.e("", "Error checking internet connection", e);
                }
            } else {
                Log.d("", "No network available!");
            }
            return false;
        }

        @Override
        protected Void doInBackground(Message... params) {
            try {
                if (hasActiveInternetConnection(getActivity()))
                    Transport.send(params[0]);
                else
                    h=1;
            }
            catch (MessagingException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(h==0)
            Toast.makeText(getActivity(),"Sent",Toast.LENGTH_SHORT).show();
            else if(h==1)
                Toast.makeText(getActivity(),"No Internet Connection",Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onCancelled() {
            Toast.makeText(getActivity(),"Cancelled . Try again",Toast.LENGTH_SHORT).show();
        }
    }

}