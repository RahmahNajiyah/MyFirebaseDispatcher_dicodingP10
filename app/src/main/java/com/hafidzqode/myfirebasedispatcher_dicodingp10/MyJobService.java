package com.hafidzqode.myfirebasedispatcher_dicodingp10;


import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.firebase.jobdispatcher.JobService;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONObject;

import java.text.DecimalFormat;

import cz.msebera.android.httpclient.Header;

//todo 5
// buat kelas "MyJobService" yang di dalamnya berisi kode dari firebase dispatcher.
public class MyJobService extends JobService{
    //todo 7
    public static final String TAG = MyJobService.class.getSimpleName();
    final String APP_ID = "4d73c23a515c0a652fae489dfa47ce79";
    public static String EXTRAS_CITY = "extras_city";

    //todo 8
    private void getCurrentWeather(final com.firebase.jobdispatcher.JobParameters job){
        String city = job.getExtras().getString(EXTRAS_CITY);

        AsyncHttpClient client = new AsyncHttpClient();
        String url = "http://api.openweathermap.org/data/2.5/weather?q="+city+"&appid="+APP_ID;
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                Log.d(TAG, result);
                try {
                    JSONObject responseObject = new JSONObject(result);
                    String currentWeather = responseObject.getJSONArray("weather").getJSONObject(0).getString("main");
                    String description = responseObject.getJSONArray("weather").getJSONObject(0).getString("description");
                    double tempInKelvin = responseObject.getJSONObject("main").getDouble("temp");

                    double tempInCelcius = tempInKelvin - 273;
                    String temprature = new DecimalFormat("##.##").format(tempInCelcius);

                    String title = "Current Weather";
                    String message = currentWeather+", "+description+" with "+temprature+" celcius";
                    int notifId = 100;
                    
                    showNotification(getApplicationContext(), title, message, notifId);

                    //ketika proses selesai, maka perlu dipanggil jobFinished dengan parameter false;
                    jobFinished((com.firebase.jobdispatcher.JobParameters) job,false);
                }catch (Exception e){
                    //ketika terjadi error, maka jobfinished diset dengan parameter true, yang artinya job perlu di reschedule
                    jobFinished((com.firebase.jobdispatcher.JobParameters) job,true);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                //ketika proses gagal, maka jobFinished diset dengan parameter true. Yang artinya job perlu di reschedule
                jobFinished((com.firebase.jobdispatcher.JobParameters) job,true);
            }
        });
    }

    private void showNotification(Context applicationContext, String title, String message, int notifId) {
        NotificationManager notificationManagerCompat = (NotificationManager) applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(applicationContext)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_replay_black_24dp)
                .setContentText(message)
                .setColor(ContextCompat.getColor(applicationContext, android.R.color.black))
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setSound(alarmSound);
        notificationManagerCompat.notify(notifId, builder.build());
    }


    @Override
    public boolean onStartJob(com.firebase.jobdispatcher.JobParameters job) {
        getCurrentWeather(job);
        // return true ketika ingin dijalankan proses dengan thread yang berbeda, misal asynctask
        return true;
    }

    @Override
    public boolean onStopJob(com.firebase.jobdispatcher.JobParameters job) {
        return false;
    }
}