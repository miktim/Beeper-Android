package org.miktim;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.beeper.R;

public class MainActivity extends AppCompatActivity
        implements SeekBar.OnSeekBarChangeListener {

//TODO rotate dialog layout on screen orientation change
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initSlider(findViewById(R.id.seek_beeps), 1, 50, 1);
        initSlider(findViewById(R.id.seek_delay), 0, 500, 100);

        initDropDown(findViewById(R.id.spinner_tones), ToneGeneratorTones.getNames(), 0);
        initSlider(findViewById(R.id.seek_volume), 0, 100, 50);
        initSlider(findViewById(R.id.seek_duration), 0, 1000, 100);
        setBeeperDefaults();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        System.exit(0);
    }

    // TODO SliderWidget
    void initSlider(View view, int min, int max, int progress) {
// http://developer.alexanderklimov.ru/android/views/seekbar.php
        SeekBar slider = (SeekBar) view;
        slider.setOnSeekBarChangeListener(this);
//        slider.setMin(min);  // requires API 26
        slider.setMax(max);
        slider.setProgress(progress);
    }

    void showSliderProgress(SeekBar slider, int progress) {
// https://stackoverflow.com/questions/13194823/how-to-get-the-previous-or-next-view
        ViewGroup container = (ViewGroup) slider.getParent();
        int indexOfChild = container.indexOfChild(slider);
        container = (ViewGroup)container.getChildAt(indexOfChild - 1);
        TextView valueText = (TextView) container.getChildAt(1);
        valueText.setText("" + progress);
    }

    void initDropDown(View view, String[] items, int index) {
// http://developer.alexanderklimov.ru/android/views/spinner.php
        Spinner spinner = (Spinner) view;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, items);
        spinner.setAdapter(adapter);
        spinner.setSelection(index);
    }

    void setBeeperDefaults() {
        int toneIdx;
        int[] toneValues = ToneGeneratorTones.getValues();
        for (toneIdx = 0; toneIdx < toneValues.length; toneIdx++) {
            if (toneValues[toneIdx] == Beeper.DEFAULT_TONE) break;
        }
        ((Spinner) findViewById(R.id.spinner_tones)).setSelection(toneIdx);
        ((SeekBar) findViewById(R.id.seek_volume)).setProgress(Beeper.DEFAULT_VOLUME);
        ((SeekBar) findViewById(R.id.seek_duration)).setProgress(Beeper.DEFAULT_DURATION);
    }

    public void onDefaultBtnClick(View view) {
        setBeeperDefaults();
    }

    AsyncTask mBeepsPlayer;

    @SuppressLint("StaticFieldLeak")

    // AsyncTask: This class was deprecated in API level 30.
    // Use the standard java.util.concurrent or Kotlin concurrency utilities instead.

    public void onPlayBtnClick(View view) {
        if (mBeepsPlayer != null) {
            mBeepsPlayer.cancel(true);
        } else {
            mBeepsPlayer = (new AsyncTask<Integer, Void, Boolean>() {
                Button mPlayButton = findViewById(R.id.btn_play);

                @Override
                protected void onPreExecute() {
                    mBeepsPlayer = this;
                    mPlayButton.setText(R.string.action_stop);
                }

                @Override
                protected void onCancelled() {
                    onPostExecute(false);
                }

                @Override
                protected Boolean doInBackground(Integer[] params) {
                    try {
                        for (int i = 0; i < params[0]; i++) {
                            Beeper.beep(params[2], params[3], params[4]);
                            Thread.sleep(params[1]);
                        }
                    } catch (InterruptedException ignored) {
                        return false;
                    }
                    return true;
                }

                @Override
                protected void onPostExecute(Boolean res) {
                    mBeepsPlayer = null;
                    mPlayButton.setText(R.string.action_play);
                }

            });

            int beeps = ((SeekBar) findViewById(R.id.seek_beeps)).getProgress();
            int delay = ((SeekBar) findViewById(R.id.seek_delay)).getProgress();
            int tone = ToneGeneratorTones.getValues()
                    [((Spinner) findViewById(R.id.spinner_tones)).getSelectedItemPosition()];
            int volume = ((SeekBar) findViewById(R.id.seek_volume)).getProgress();
            int duration = ((SeekBar) findViewById(R.id.seek_duration)).getProgress();
            mBeepsPlayer.execute(new Integer[]{beeps, delay, tone, volume, duration});
        }
    }

    public void onAboutClick(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://github.com/miktim/Beeper"));
        startActivity(browserIntent);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if(seekBar.equals(((SeekBar) findViewById(R.id.seek_beeps))) && i == 0)
            ((SeekBar) findViewById(R.id.seek_beeps)).setProgress(1);
        else showSliderProgress(seekBar, i);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}
