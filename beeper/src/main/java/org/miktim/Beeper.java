/*
 * Android Beeper. MIT (c) 2022 miktim@mail.ru
 *
 * Release notes:
 *  - a new beep interrupts the previous one;
 *  - in android 4.0 the volume of the beep depends on the system volume,
 *    in android 7.0 it is the absolute volume.
 * Overview:
 *   Class Beeper;
 *     static void beep(); // with default beeper tone and volume
 *     static void beep(int volume);
 *     static void beep(int toneType, int volume, int durationMs);
 *     static void cancel(); // interrupt current beep
 * See:
 *   https://developer.android.com/reference/android/media/ToneGenerator
 *   https://stackoverflow.com/questions/29509010/how-to-play-a-short-beep-to-android-phones-loudspeaker-programmatically
 *   https://stackoverflow.com/questions/11964623/audioflinger-could-not-create-track-status-12
 */
package org.miktim;

import android.media.AudioManager;
import android.media.ToneGenerator;

import static android.media.AudioManager.STREAM_MUSIC;

public class Beeper {
    public static final int DEFAULT_TONE = ToneGenerator.TONE_CDMA_ABBR_ALERT;
    public static final int DEFAULT_VOLUME = (AudioManager.STREAM_SYSTEM * ToneGenerator.MAX_VOLUME) / 15;
    public static final int DEFAULT_DURATION = 100;

    private static Thread sBeepThread = new Thread();

    public static void beep() {
        beep(DEFAULT_VOLUME);
    }

    public static void beep(int volume) {
        beep(DEFAULT_TONE, volume, DEFAULT_DURATION);
    }

    synchronized public static void beep(int toneType, int volume, int durationMs) {
        (sBeepThread = new BeepGenerator(toneType, volume, durationMs)).start();
    }
    
    public void cancel() {
        sBeepThread.interrupt();
    }

    private static class BeepGenerator extends Thread {
        private int mTone;
        private int mVolume;
        private int mDuration;
        private Thread mPrevBeepThread = sBeepThread;

        BeepGenerator(int toneType, int volume, int durationMs) {
            mTone = toneType;
            mVolume = volume;
            mDuration = durationMs;
            this.setDaemon(true);
        }

        public void run() {
            mPrevBeepThread.interrupt();
            try {
                ToneGenerator toneGen = new ToneGenerator(STREAM_MUSIC, mVolume);
                try {
                    mPrevBeepThread.join();
                    toneGen.startTone(mTone, mDuration);
                    Thread.sleep(mDuration);
                } catch (InterruptedException ignored) {
                }
                toneGen.stopTone();
                toneGen.release();
            } catch (RuntimeException ignored) {
            }
        }
    }

}
