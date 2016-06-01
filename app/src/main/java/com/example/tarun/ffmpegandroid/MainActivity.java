package com.example.tarun.ffmpegandroid;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.tarun.ffmpegandroid.util.FileMover;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements FfmpegFinishListener{
    private ProgressDialog progressDialog;

    private String[] libraryAssets = {"ffmpeg"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        moveFFMPEG(getPackageName());
    }

    public void call(View view) {
        Log.i("FFMPEG", "Inside call");
        callTrimAsynctask("/sdcard/a.mp4");
    }

    /********
     * ************************************************************************************************
     * ************************************************************************************************
     * *******************implementing FFmpeg library for video ****************************************
     * **************************************************************************************************
     * **************************************************************************************************
     *
     */

    private boolean isFFMPEGCopied(String packageName)
    {
        File ffmpegFile=new File("/data/data/"+packageName+"/ffmpeg" );
        if(ffmpegFile.exists()){
            Log.i("FFMPEG", "FFmpeg lib exists");
            return true;
        }
        else{
            return false;
        }
    }
    private void moveFFMPEG(String packageName){
        progressDialog=new ProgressDialog(MainActivity.this);
        if(!isFFMPEGCopied(packageName)){
            progressDialog.show();
            for (int i = 0; i < libraryAssets.length; i++) {
                try {
                    InputStream ffmpegInputStream = this.getAssets().open(libraryAssets[i]);
                    FileMover fm = new FileMover(ffmpegInputStream,"/data/data/"+packageName+"/" + libraryAssets[i]);
                    fm.moveIt();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        if(progressDialog.isShowing()){
            progressDialog.dismiss();
        }
        setFFMPEFGPermission(packageName);
    }


   private void setFFMPEFGPermission(String packageName){
        String[] args = {"/system/bin/chmod", "755", "/data/data/"+packageName+"/ffmpeg"};
        Process process=null;

        try {
            process = new ProcessBuilder(args).start();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        process.destroy();

    }


    public void callTrimAsynctask(String vPath){

        //Ready to merge two videos
        String packageName=getApplicationContext().getPackageName();
        FfmpegProcess processVideo=new FfmpegProcess(packageName,MainActivity.this,vPath,MainActivity.this);
        processVideo.execute(new String[]{"1"});
    }

    /***function to trim the video**/
    public static  String[] trimVideo(String inputFile, String outputFile, String packageName) {
        return new String[] {
                "/data/data/"+packageName+"/ffmpeg",
                "-y", "-i", inputFile, "-strict", "experimental", "-s", "160x120", "-r", "25", "-vcodec", "mpeg4", "-b", "150k", "-ab", "48000",
                "-ac", "2", "-ar", "22050", outputFile

        };
    }


    @Override
    public void ffmpegResult(boolean success, String msg) {
        if (success) {
            // TODO Auto-generated method stub
            final File path = new File(FfmpegProcess.ROOT + FfmpegProcess.DIRECTORY);
            File outputFile = new File(FfmpegProcess.ROOT + FfmpegProcess.DIRECTORY + FfmpegProcess.OUTPUT_FILE);
            String absolutePath = outputFile.getAbsolutePath();
            //Preferences.saveVideoPath(getApplicationContext(), absolutePath);
            /*split(absolutePath);
            setFrameList();*/
        } else {
            Toast.makeText(this, "Failed", Toast.LENGTH_LONG).show();
        }
    }

}
