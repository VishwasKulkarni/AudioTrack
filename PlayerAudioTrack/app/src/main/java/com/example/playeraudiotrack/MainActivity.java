package com.example.playeraudiotrack;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {
    AudioTrack mAudioTrack = null;
    static boolean playing_16bit = false;
    static boolean playing_float = false;
    static boolean clicked_16bit = true;
    static boolean clicked_float = true;
    public AudioTrack mAudioTrack_16bit = null;
    public AudioTrack mAudioTrack_float = null;
    int bytes = 0, buffSize = 1024;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View topHalf = findViewById(R.id.tophalf);
        View bottomHalf = findViewById(R.id.bottomHalf);

        /** PCM 16Bit tone Playback **/
        topHalf.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    if(isTouchOnLayout(view, motionEvent)) {
                        if(clicked_16bit) {
                            Thread workthread = new Thread() {
                                @Override
                                public void run() {
                                    clicked_16bit = false;
                                    clicked_float = true;
                                    playing_16bit = true;
                                    playing_float = false;
                                    mAudioTrack_16bit = buildTrack(AudioFormat.ENCODING_PCM_16BIT);
                                    byte[] readBuf = new byte[buffSize];
                                    try {
                                        InputStream inStream = getResources().openRawResource(R.raw.stereo16bit441);
                                        while ((bytes = inStream.read(readBuf)) != -1 && playing_16bit) {
                                            mAudioTrack_16bit.write(readBuf, 0, bytes);
                                            mAudioTrack_16bit.play();
                                        }
                                        mAudioTrack_16bit.stop();
                                        mAudioTrack_16bit.flush();
                                        mAudioTrack_16bit.release();
                                        playing_16bit = false;
                                        clicked_16bit = true;
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            workthread.start();
                        }
                    }
                }
                return false;
            }
        });

        /** PCM Float tone Playback **/
        bottomHalf.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    if(isTouchOnLayout(view, motionEvent)){
                        if(clicked_float){
                            Thread workthread = new Thread() {
                                @Override
                                public void run() {
                                    clicked_float= false;
                                    clicked_16bit = true;
                                    playing_16bit = false;
                                    playing_float = true;
                                    mAudioTrack_float = buildTrack(AudioFormat.ENCODING_PCM_FLOAT);
                                    byte[] readBuf = new byte[buffSize * 16];
                                    try {
                                        InputStream inStream = getResources().openRawResource(R.raw.float441stereo);
                                        /** ByteBuffer is used to write Float format as byte[]
                                         * doesn't support.
                                         */
                                        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(buffSize);
                                        while ((bytes = inStream.read(readBuf)) != -1 && playing_float) {
                                            byteBuffer = ByteBuffer.wrap(readBuf);
                                            mAudioTrack_float.write(byteBuffer, bytes, AudioTrack.WRITE_BLOCKING);
                                            mAudioTrack_float.play();
                                        }
                                        mAudioTrack_float.stop();
                                        mAudioTrack_float.flush();
                                        mAudioTrack_float.release();
                                        playing_float = false;
                                        clicked_float = true;
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            workthread.start();
                        }
                    }
                }
                return false;
            }
        });
    }

    public AudioTrack buildTrack(int mFormat) {
        int minimumBufSize = AudioTrack.getMinBufferSize(44100,AudioFormat.CHANNEL_OUT_STEREO,mFormat);
        mAudioTrack = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(mFormat)
                        .setSampleRate(44100)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build())
                .setBufferSizeInBytes(minimumBufSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build();
        return mAudioTrack;
    }

    private boolean isTouchOnLayout(View view, MotionEvent motionEvent) {
        if(motionEvent.getX() < 0 || motionEvent.getY() < 0 ||
                motionEvent.getX() > view.getMeasuredWidth() ||
                motionEvent.getY() > view.getMeasuredHeight())
            return false;
        return true;
    }
}