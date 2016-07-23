package com.campg.sonix.drawer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class FirstAc extends android.app.Fragment {
    //private static Button btn;
    EditText ed;
    EditText num;
    Button btn;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView=inflater.inflate(R.layout.activity_first,container,false);
        btn=(Button) rootView.findViewById(R.id.butn);
        ed=(EditText) rootView.findViewById(R.id.str);
        num=(EditText) rootView.findViewById(R.id.start);
        onclik();

        return rootView;
    }


/*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        ed=(EditText) findViewById(R.id.str);
        num=(EditText) findViewById(R.id.start);
        onclik();
    }
*/

    public void onclik()
    {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Integer.parseInt(num.getText().toString());
                    //Intent intent=new Intent(.CamTestActivity);
                    /*if(ed.getText().toString()=="Heading")
                        intent.putExtra("string","");
                    else
                        intent.putExtra("string",ed.getText().toString());
                    intent.putExtra("num",num.getText().toString());*/
                    //startActivity(intent);

                    startActivity(new Intent(getActivity(),CamTestActivity.class).putExtra("num",num.getText().toString()));

                    Toast.makeText(getActivity(), "m here", Toast.LENGTH_LONG).show();

                }
                catch (Exception e)
                {
                    num.requestFocus();
                    Toast.makeText(getActivity(),"Enter proper nunber",Toast.LENGTH_LONG).show();
                    Log.e("exc", " is", e);
                }
            }
        });
    }

}
