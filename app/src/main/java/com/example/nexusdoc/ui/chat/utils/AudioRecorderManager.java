package com.example.nexusdoc.ui.chat.utils;

import android.content.Context;
import android.media.MediaRecorder;
import java.io.IOException;

public class AudioRecorderManager {
    private MediaRecorder mediaRecorder;
    private String audioFilePath;
    private boolean isRecording = false;
    private Context context;

    public AudioRecorderManager(Context context) {
        this.context = context;
    }

    public void startRecording() throws IOException {
        audioFilePath = context.getExternalFilesDir(null).getAbsolutePath() +
                "/audio_" + System.currentTimeMillis() + ".3gp";

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(audioFilePath);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        mediaRecorder.prepare();
        mediaRecorder.start();
        isRecording = true;
    }

    public String stopRecording() {
        if (mediaRecorder != null && isRecording) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                isRecording = false;
                return audioFilePath;
            } catch (RuntimeException e) {
                // Gérer l'erreur d'arrêt
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void release() {
        if (mediaRecorder != null) {
            if (isRecording) {
                stopRecording();
            }
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }
}