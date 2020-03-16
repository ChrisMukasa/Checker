package com.chris.mukasa.updatechecker;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.View;

import com.chris.mukasa.toastlibrary.Toaster;

import org.jsoup.Jsoup;

import java.util.Objects;

/**
 **** This Library is the property for Chris Simba Mukasa
 **** Email: mukasa.chris7@gmail.com
 **** Phone: +243-994-802-444
 **** R.D.Congo
 **** Edition March 2020
 **** Mobile Developer Enthusiast
 */

public class UpdateChecker
{
    private static final String TAG                    = "UpdateChecker";
    private static final String UPDATE_USER_AGENT      = "Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6";
    private static final String UPDATE_GOOGLE_ADDRESS  = "http://www.google.com";
    private static final String UPDATE_SELECT          = ".hAyfc .htlgb";
    private static final String UPDATE_GOOGLE_PLAY     = "https://play.google.com/store/apps/details?id=";
    private static final String UPDATE_GOOGLE_PLAY_EXT = "&hl=it";

    private Toaster toaster = new Toaster();
    private Activity activity;

    public UpdateChecker(Activity activity)
    {
        this.activity = activity;
    }

    private String getCurrentVersion()
    {
        PackageManager manager = activity.getPackageManager();
        PackageInfo info    = null;

        try
        {
            info = manager.getPackageInfo(activity.getPackageName(), 0);
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }

        return info.versionName;
    }

    private class GetLatestVersion extends AsyncTask<String, String, String>
    {
        private String latestVersion;
        private ProgressDialog progress;
        private boolean manual;

        GetLatestVersion(boolean manual)
        {
            this.manual = manual;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            if(manual)
            {
                progress = new ProgressDialog(activity);
                progress.setMessage(activity.getString(R.string.checking_for_update));
                progress.setCancelable(false);
                progress.show();
            }
        }

        @Override
        protected String doInBackground(String... strings)
        {
            try
            {
                latestVersion = Jsoup
                        .connect(UPDATE_GOOGLE_PLAY + activity.getPackageName() + UPDATE_GOOGLE_PLAY_EXT)
                        .timeout(30000)
                        .userAgent(UPDATE_USER_AGENT)
                        .referrer(UPDATE_GOOGLE_ADDRESS)
                        .get()
                        .select(UPDATE_SELECT)
                        .get(7)
                        .ownText();

                return latestVersion;
            }
            catch (Exception e)
            {
                return latestVersion;
            }
        }

        @Override
        protected void onPostExecute(String s)
        {
            super.onPostExecute(s);

            if(manual)
                if(progress != null)
                    if(progress.isShowing())
                        progress.dismiss();

            String currentVersion = getCurrentVersion();

            if(!currentVersion.equals(latestVersion) && latestVersion != null)
            {
                final Dialog dialog = new Dialog(activity);
                dialog.setCancelable(true);
                dialog.setContentView(R.layout.dialog_animation_download);
                dialog.findViewById(R.id.dialog_update_cancel).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        dialog.dismiss();
                    }
                });
                dialog.findViewById(R.id.dialog_update_update).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" +activity.getPackageName())));
                        dialog.dismiss();
                    }
                });

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                    Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

                Log.e(TAG, activity.getResources().getString(R.string.update_available));
            }
            else
            {
                if(manual)
                {
                    toaster.updating(activity, activity.getResources().getString(R.string.no_update_available));
                    Log.e(TAG, activity.getResources().getString(R.string.no_update_available));
                }
            }
        }
    }

    public void check(boolean manual)
    {
        new GetLatestVersion(manual).execute();
    }
}
