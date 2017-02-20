package com.csis.lab04; //package we're in


//android imports
import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

//PURE DATA IMPORTS

import org.puredata.android.io.AudioParameters;
import org.puredata.android.io.PdAudio;
import org.puredata.android.utils.PdUiDispatcher;
import org.puredata.core.PdBase;
import org.puredata.core.PdReceiver;
import org.puredata.core.utils.IoUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private PdUiDispatcher dispatcher; //must declare this to use later, used to receive data from sendEvents
    TextView myCounter;
    TextView myFrequency;

    private SeekBar slider1;
    float slider1Value = 0.0f;

    private SeekBar slider2;
    float slider2Value = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);//Mandatory
        setContentView(R.layout.activity_main);//Mandatory



        myCounter = (TextView) findViewById(R.id.counter);
        myFrequency = (TextView) findViewById(R.id.frequency);

        Button playButton = (Button) findViewById(R.id.playButton); //findViewById uses the ids you specified in the xml!
        Button stopButton = (Button) findViewById(R.id.stopButton);

        Switch onOffSwitch = (Switch) findViewById(R.id.onOffSwitch);//declared the switch here pointing to id onOffSwitch
        Switch reverseSwitch = (Switch) findViewById(R.id.reverseSwitch);
        Switch songOnOff = (Switch) findViewById(R.id.songOnOff);

        slider1 = (SeekBar) findViewById(R.id.slider1);
        slider2 = (SeekBar) findViewById(R.id.slider2);


        //Check to see if onOffSwitch value changes
        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                float val = (isChecked) ?  1.0f : 0.0f; // value = (get value of isChecked, if true val = 1.0f, if false val = 0.0f)
                sendFloatPD("onOff", val); //send value to patch, receiveEvent names onOff

            }
        });

        //Check to see if songOnOff value changes
        songOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                float val = (isChecked) ?  1.0f : 0.0f; // value = (get value of isChecked, if true val = 1.0f, if false val = 0.0f)
                sendFloatPD("songOnOff", val); //send value to patch, receiveEvent names onOff

            }
        });

        //Check to see if reverseSwitch value changes
        reverseSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                float val = (isChecked) ?  1.0f : 0.0f; // value = (get value of isChecked, if true val = 1.0f, if false val = 0.0f)
                sendFloatPD("reverse", val); //send value to patch, receiveEvent names onOff

            }
        });

        //<------playButton CLICK LISTENER--------------->
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendBangPD("play");

            }
        });

        //<------stopButton CLICK LISTENER--------------->
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendBangPD("stop");

            }
        });

        //<--------SLIDER 1 LISTENER------------>
        slider1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                slider1Value = progress / 100.0f;
                sendFloatPD("slider1", slider1Value);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //<--------SLIDER 2 LISTENER------------>
        slider2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                slider2Value = progress / 100.0f;
                sendFloatPD("slider1", slider2Value);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        try { // try the code below, catch errors if things go wrong
            initPD(); //method is below to start PD
            loadPDPatch("counter.pd"); // This is the name of the patch in the zip
        } catch (IOException e) {
            e.printStackTrace(); // print error if init or load patch fails.
            finish(); // end program
        }

    }

    @Override //If screen is resumed
    protected void onResume(){
        super.onResume();
        PdAudio.startAudio(this);
    }

    @Override//If we switch to other screen
    protected void onPause()
    {
        super.onPause();
        PdAudio.stopAudio();
    }

    //METHOD TO SEND FLOAT TO PUREDATA PATCH
    public void sendFloatPD(String receiver, Float value)//REQUIRES (RECEIVEEVENT NAME, FLOAT VALUE TO SEND)
    {
        PdBase.sendFloat(receiver, value); //send float to receiveEvent
    }

    //METHOD TO SEND BANG TO PUREDATA PATCH
    public void sendBangPD(String receiver)
    {

        PdBase.sendBang(receiver); //send bang to receiveEvent
    }


    //<---THIS METHOD LOADS SPECIFIED PATCH NAME----->
    private void loadPDPatch(String patchName) throws IOException
    {
        File dir = getFilesDir(); //Get current list of files in directory
        try {
            IoUtils.extractZipResource(getResources().openRawResource(R.raw.counter), dir, true); //extract the zip file in raw called synth
            File pdPatch = new File(dir, patchName); //Create file pointer to patch
            PdBase.openPatch(pdPatch.getAbsolutePath()); //open patch
        }catch (IOException e)
        {

        }
    }



    //Listener receiver1
    private PdReceiver receiver1 = new PdReceiver() {
        private void pdPost(final String msg) {
            Log.e("RECEIVED:", msg);

            new Handler().post(new Runnable() {
                @Override
                public void run() {

                }
            });
        }

        @Override
        public void print(String s) {
            Log.i("PRINT", s);
            Toast.makeText(getBaseContext(),s,Toast.LENGTH_LONG);
        }
        @Override
        public void receiveBang(String source) {
            pdPost("bang");
        }

        @Override
        public void receiveFloat(String source, float x) {
            pdPost("float:" + x);
            if(source.equals("sendCounter")) {
                myCounter.setText(String.valueOf(x));
            }

            if(source.equals("sendFrequency")) {
                myFrequency.setText(String.valueOf(x));
            }
        }

        @Override
        public void receiveSymbol(String source, String symbol) {
            pdPost("symbol: " + symbol);
        }

        @Override
        public void receiveList(String source, Object... args) {
            pdPost("list: " + Arrays.toString(args));
        }

        @Override
        public void receiveMessage(String source, String symbol, Object... args) {
            pdPost("message: " + Arrays.toString(args));
        }
    };



    //<---THIS METHOD INITIALISES AUDIO SERVER----->
    private void initPD() throws IOException
    {
        int sampleRate = AudioParameters.suggestSampleRate(); //get sample rate from system
        PdAudio.initAudio(sampleRate,0,2,8,true); //initialise audio engine

        dispatcher = new PdUiDispatcher(); //create UI dispatcher
        PdBase.setReceiver(dispatcher); //set dispatcher to receive items from puredata patches

        dispatcher.addListener("sendCounter", receiver1);
        PdBase.subscribe("sendCounter");

        dispatcher.addListener("sendFrequency", receiver1);
        PdBase.subscribe("sendFrequency");

    }

}
