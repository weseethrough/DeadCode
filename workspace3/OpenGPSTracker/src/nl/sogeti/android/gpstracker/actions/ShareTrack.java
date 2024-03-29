///*------------------------------------------------------------------------------
// **     Ident: Sogeti Smart Mobile Solutions
// **    Author: rene
// ** Copyright: (c) Apr 24, 2011 Sogeti Nederland B.V. All Rights Reserved.
// **------------------------------------------------------------------------------
// ** Sogeti Nederland B.V.            |  No part of this file may be reproduced
// ** Distributed Software Engineering |  or transmitted in any form or by any
// ** Lange Dreef 17                   |  means, electronic or mechanical, for the
// ** 4131 NJ Vianen                   |  purpose, without the express written
// ** The Netherlands                  |  permission of the copyright holder.
// *------------------------------------------------------------------------------
// *
// *   This file is part of OpenGPSTracker.
// *
// *   OpenGPSTracker is free software: you can redistribute it and/or modify
// *   it under the terms of the GNU General Public License as published by
// *   the Free Software Foundation, either version 3 of the License, or
// *   (at your option) any later version.
// *
// *   OpenGPSTracker is distributed in the hope that it will be useful,
// *   but WITHOUT ANY WARRANTY; without even the implied warranty of
// *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// *   GNU General Public License for more details.
// *
// *   You should have received a copy of the GNU General Public License
// *   along with OpenGPSTracker.  If not, see <http://www.gnu.org/licenses/>.
// *
// */
//package nl.sogeti.android.gpstracker.actions;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.List;
//
//import nl.sogeti.android.gpstracker.R;
//import nl.sogeti.android.gpstracker.actions.tasks.GpxCreator;
//import nl.sogeti.android.gpstracker.actions.tasks.GpxSharing;
//import nl.sogeti.android.gpstracker.actions.tasks.JogmapSharing;
//import nl.sogeti.android.gpstracker.actions.tasks.KmzCreator;
//import nl.sogeti.android.gpstracker.actions.tasks.KmzSharing;
//import nl.sogeti.android.gpstracker.actions.tasks.OsmSharing;
//import nl.sogeti.android.gpstracker.actions.utils.ProgressListener;
//import nl.sogeti.android.gpstracker.actions.utils.StatisticsCalulator;
//import nl.sogeti.android.gpstracker.actions.utils.StatisticsDelegate;
//import nl.sogeti.android.gpstracker.db.GPStracking.Tracks;
//import nl.sogeti.android.gpstracker.util.Constants;
//import nl.sogeti.android.gpstracker.util.UnitsI18n;
//import nl.sogeti.android.gpstracker.viewer.map.LoggerMap;
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.app.AlertDialog.Builder;
//import android.app.Dialog;
//import android.app.Notification;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.content.ComponentName;
//import android.content.ContentResolver;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.DialogInterface.OnClickListener;
//import android.content.Intent;
//import android.content.ServiceConnection;
//import android.content.SharedPreferences;
//import android.content.SharedPreferences.Editor;
//import android.content.pm.PackageManager;
//import android.content.pm.ResolveInfo;
//import android.database.Cursor;
//import android.graphics.Bitmap;
//import android.graphics.Bitmap.CompressFormat;
//import android.graphics.BitmapFactory;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.IBinder;
//import android.preference.PreferenceManager;
//import android.util.Log;
//import android.view.Menu;
//import android.view.View;
//import android.view.Window;
//import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemSelectedListener;
//import android.widget.ArrayAdapter;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageButton;
//import android.widget.ImageView;
//import android.widget.RemoteViews;
//import android.widget.Spinner;
//import android.widget.Toast;
//
//public class ShareTrack extends Activity implements StatisticsDelegate
//{
//   private static final String TAG = "OGT.ShareTrack";
//
//   private static final int EXPORT_TYPE_KMZ = 0;
//   private static final int EXPORT_TYPE_GPX = 1;
//   private static final int EXPORT_TYPE_TEXTLINE = 2;
//   private static final int EXPORT_TARGET_SAVE = 0;
//   private static final int EXPORT_TARGET_SEND = 1;
//   private static final int EXPORT_TARGET_JOGRUN = 2;
//   private static final int EXPORT_TARGET_OSM = 3;
//   private static final int EXPORT_TARGET_BREADCRUMBS = 4;
//   private static final int EXPORT_TARGET_TWITTER = 0;
//   private static final int EXPORT_TARGET_SMS = 1;
//   private static final int EXPORT_TARGET_TEXT = 2;
//
//   private static final int PROGRESS_STEPS = 10;
//   private static final int DIALOG_ERROR = Menu.FIRST + 28;
//   private static final int DIALOG_CONNECTBREADCRUMBS = Menu.FIRST + 29;
//   private static final int DESCRIBE = 312;
//
//   private static File sTempBitmap;
//
//   private RemoteViews mContentView;
//   private int barProgress = 0;
//   private Notification mNotification;
//   private NotificationManager mNotificationManager;
//
//   private EditText mFileNameView;
//   private EditText mTweetView;
//   private Spinner mShareTypeSpinner;
//   private Spinner mShareTargetSpinner;
//   private Uri mTrackUri;
//   boolean mBound = false;
//   private String mErrorDialogMessage;
//   private Throwable mErrorDialogException;
//
//   private ImageView mImageView;
//   private ImageButton mCloseImageView;
//
//   private Uri mImageUri;
//
//   @Override
//   public void onCreate(Bundle savedInstanceState)
//   {
//      super.onCreate(savedInstanceState);
//      setContentView(R.layout.sharedialog);
//      //startService(service);
//
//      mTrackUri = getIntent().getData();
//
//      mFileNameView = (EditText) findViewById(R.id.fileNameField);
//      mTweetView = (EditText) findViewById(R.id.tweetField);
//      mImageView = (ImageView) findViewById(R.id.imageView);
//      mCloseImageView = (ImageButton) findViewById(R.id.closeImageView);
//
//      mShareTypeSpinner = (Spinner) findViewById(R.id.shareTypeSpinner);
//      ArrayAdapter<CharSequence> shareTypeAdapter = ArrayAdapter.createFromResource(this, R.array.sharetype_choices, android.R.layout.simple_spinner_item);
//      shareTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//      mShareTypeSpinner.setAdapter(shareTypeAdapter);
//      mShareTargetSpinner = (Spinner) findViewById(R.id.shareTargetSpinner);
//      mShareTargetSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
//         {
//            @Override
//            public void onItemSelected(AdapterView< ? > arg0, View arg1, int position, long arg3)
//            {
//               if (mShareTypeSpinner.getSelectedItemPosition() == EXPORT_TYPE_GPX && position == EXPORT_TARGET_BREADCRUMBS)
//               {
//                  boolean authorized = mService.isAuthorized();
//                  if (mShareTypeSpinner.getSelectedItemPosition() == EXPORT_TYPE_TEXTLINE && position != EXPORT_TARGET_SMS)
//               {
//                  readScreenBitmap();
//               }
//               else
//               {
//                  removeScreenBitmap();
//               }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView< ? > arg0)
//            { /* NOOP */
//            }
//         });
//
//      mShareTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
//         {
//            @Override
//            public void onItemSelected(AdapterView< ? > arg0, View arg1, int position, long arg3)
//            {
//               adjustTargetToType(position);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView< ? > arg0)
//            { /* NOOP */
//            }
//         });
//
//      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//      int lastType = prefs.getInt(Constants.EXPORT_TYPE, EXPORT_TYPE_KMZ);
//      mShareTypeSpinner.setSelection(lastType);
//      adjustTargetToType(lastType);
//
//      mFileNameView.setText(queryForTrackName(getContentResolver(), mTrackUri));
//
//      Button okay = (Button) findViewById(R.id.okayshare_button);
//      okay.setOnClickListener(new View.OnClickListener()
//         {
//            @Override
//            public void onClick(View v)
//            {
//               v.setEnabled(false);
//               share();
//            }
//         });
//
//      Button cancel = (Button) findViewById(R.id.cancelshare_button);
//      cancel.setOnClickListener(new View.OnClickListener()
//         {
//            @Override
//            public void onClick(View v)
//            {
//               v.setEnabled(false);
//               ShareTrack.this.finish();
//            }
//         });
//   }
//
//   @Override
//   protected void onStart()
//   {
//      super.onStart();
//      Intent intent = new Intent(this, BreadcrumbsService.class);
//      bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
//   }
//
//   @Override
//   protected void onResume()
//   {
//      super.onResume();
//
//      // Upgrade from stored OSM username/password to OAuth authorization
//      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//      if (prefs.contains(Constants.OSM_USERNAME) || prefs.contains(Constants.OSM_PASSWORD))
//      {
//         Editor editor = prefs.edit();
//         editor.remove(Constants.OSM_USERNAME);
//         editor.remove(Constants.OSM_PASSWORD);
//         editor.commit();
//      }
//      findViewById(R.id.okayshare_button).setEnabled(true);
//      findViewById(R.id.cancelshare_button).setEnabled(true);
//   }
//
//   @Override
//   protected void onStop()
//   {
//      if (mBound)
//      {
//         unbindService(mConnection);
//         mBound = false;
//         mService = null;
//      }
//      super.onStop();
//   }
//
//   @Override
//   protected void onDestroy()
//   {
//      if (isFinishing())
//      {
//         Intent service = new Intent(this, BreadcrumbsService.class);
//         stopService(service);
//      }
//      super.onDestroy();
//   }
//
//   /**
//    * @see android.app.Activity#onCreateDialog(int)
//    */
//   @Override
//   protected Dialog onCreateDialog(int id)
//   {
//      Dialog dialog = null;
//      Builder builder = null;
//      switch (id)
//      {
//         case DIALOG_ERROR:
//            builder = new AlertDialog.Builder(this);
//            String exceptionMessage = mErrorDialogException == null ? "" : " (" + mErrorDialogException.getMessage() + ") ";
//            builder.setIcon(android.R.drawable.ic_dialog_alert).setTitle(android.R.string.dialog_alert_title).setMessage(mErrorDialogMessage + exceptionMessage)
//                  .setNeutralButton(android.R.string.cancel, null);
//            dialog = builder.create();
//            return dialog;
//         case DIALOG_CONNECTBREADCRUMBS:
//            builder = new AlertDialog.Builder(this);
//            builder.setTitle(R.string.dialog_breadcrumbsconnect).setMessage(R.string.dialog_breadcrumbsconnect_message).setIcon(android.R.drawable.ic_dialog_alert)
//                  .setPositiveButton(R.string.btn_okay, mBreadcrumbsDialogListener).setNegativeButton(R.string.btn_cancel, null);
//            dialog = builder.create();
//            return dialog;
//         default:
//            return super.onCreateDialog(id);
//      }
//   }
//
//   /**
//    * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
//    */
//   @Override
//   protected void onPrepareDialog(int id, Dialog dialog)
//   {
//      super.onPrepareDialog(id, dialog);
//      AlertDialog alert;
//      switch (id)
//      {
//         case DIALOG_ERROR:
//            alert = (AlertDialog) dialog;
//            String exceptionMessage = mErrorDialogException == null ? "" : " (" + mErrorDialogException.getMessage() + ") ";
//            alert.setMessage(mErrorDialogMessage + exceptionMessage);
//            break;
//      }
//   }
//
//   private void setGpxExportTargets()
//   {
//      ArrayAdapter<CharSequence> shareTargetAdapter = ArrayAdapter.createFromResource(this, R.array.sharegpxtarget_choices, android.R.layout.simple_spinner_item);
//      shareTargetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//      mShareTargetSpinner.setAdapter(shareTargetAdapter);
//      int lastTarget = PreferenceManager.getDefaultSharedPreferences(this).getInt(Constants.EXPORT_GPXTARGET, EXPORT_TARGET_SEND);
//      mShareTargetSpinner.setSelection(lastTarget);
//
//      removeScreenBitmap();
//   }
//
//   private void setKmzExportTargets()
//   {
//      ArrayAdapter<CharSequence> shareTargetAdapter = ArrayAdapter.createFromResource(this, R.array.sharekmztarget_choices, android.R.layout.simple_spinner_item);
//      shareTargetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//      mShareTargetSpinner.setAdapter(shareTargetAdapter);
//      int lastTarget = PreferenceManager.getDefaultSharedPreferences(this).getInt(Constants.EXPORT_KMZTARGET, EXPORT_TARGET_SEND);
//      mShareTargetSpinner.setSelection(lastTarget);
//
//      removeScreenBitmap();
//   }
//
//   private void setTextLineExportTargets()
//   {
//      ArrayAdapter<CharSequence> shareTargetAdapter = ArrayAdapter.createFromResource(this, R.array.sharetexttarget_choices, android.R.layout.simple_spinner_item);
//      shareTargetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//      mShareTargetSpinner.setAdapter(shareTargetAdapter);
//      int lastTarget = PreferenceManager.getDefaultSharedPreferences(this).getInt(Constants.EXPORT_TXTTARGET, EXPORT_TARGET_TWITTER);
//      mShareTargetSpinner.setSelection(lastTarget);
//   }
//
//   private void share()
//   {
//      String chosenFileName = mFileNameView.getText().toString();
//      String textLine = mTweetView.getText().toString();
//      int type = (int) mShareTypeSpinner.getSelectedItemId();
//      int target = (int) mShareTargetSpinner.getSelectedItemId();
//
//      Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
//      editor.putInt(Constants.EXPORT_TYPE, type);
//
//      switch (type)
//      {
//         case EXPORT_TYPE_KMZ:
//            editor.putInt(Constants.EXPORT_KMZTARGET, target);
//            editor.commit();
//            exportKmz(chosenFileName, target);
//            break;
//         case EXPORT_TYPE_GPX:
//            editor.putInt(Constants.EXPORT_GPXTARGET, target);
//            editor.commit();
//            exportGpx(chosenFileName, target);
//            break;
//         case EXPORT_TYPE_TEXTLINE:
//            editor.putInt(Constants.EXPORT_TXTTARGET, target);
//            editor.commit();
//            exportTextLine(textLine, target);
//            break;
//         default:
//            Log.e(TAG, "Failed to determine sharing type" + type);
//            break;
//      }
//   }
//
//   protected void exportKmz(String chosenFileName, int target)
//   {
//      switch (target)
//      {
//         case EXPORT_TARGET_SEND:
//            new KmzSharing(this, mTrackUri, chosenFileName, new ShareProgressListener(chosenFileName)).execute();
//            break;
//         case EXPORT_TARGET_SAVE:
//            new KmzCreator(this, mTrackUri, chosenFileName, new ShareProgressListener(chosenFileName)).execute();
//            break;
//         default:
//            Log.e(TAG, "Unable to determine target for sharing KMZ " + target);
//            break;
//      }
//      ShareTrack.this.finish();
//   }
//
//   protected void exportGpx(String chosenFileName, int target)
//   {
//      switch (target)
//      {
//         case EXPORT_TARGET_SAVE:
//            new GpxCreator(this, mTrackUri, chosenFileName, true, new ShareProgressListener(chosenFileName)).execute();
//            ShareTrack.this.finish();
//            break;
//         case EXPORT_TARGET_SEND:
//            new GpxSharing(this, mTrackUri, chosenFileName, true, new ShareProgressListener(chosenFileName)).execute();
//            ShareTrack.this.finish();
//            break;
//         case EXPORT_TARGET_JOGRUN:
//            new JogmapSharing(this, mTrackUri, chosenFileName, false, new ShareProgressListener(chosenFileName)).execute();
//            ShareTrack.this.finish();
//            break;
//         case EXPORT_TARGET_OSM:
//            new OsmSharing(this, mTrackUri, false, new ShareProgressListener(OsmSharing.OSM_FILENAME)).execute();
//            ShareTrack.this.finish();
//            break;
//         case EXPORT_TARGET_BREADCRUMBS:
//            sendToBreadcrumbs(mTrackUri, chosenFileName);
//            break;
//         default:
//            Log.e(TAG, "Unable to determine target for sharing GPX " + target);
//            break;
//      }
//   }
//
//   protected void exportTextLine(String message, int target)
//   {
//      String subject = "Open GPS Tracker";
//      switch (target)
//      {
//         case EXPORT_TARGET_TWITTER:
//            sendTweet(message);
//            break;
//         case EXPORT_TARGET_SMS:
//            sendSMS(message);
//            ShareTrack.this.finish();
//            break;
//         case EXPORT_TARGET_TEXT:
//            sentGenericText(subject, message);
//            ShareTrack.this.finish();
//            break;
//      }
//
//   }
//
//   @Override
//   protected void onActivityResult(int requestCode, int resultCode, Intent data)
//   {
//      if (resultCode != RESULT_CANCELED)
//      {
//         String name;
//         switch (requestCode)
//         {
//            case DESCRIBE:
//               Uri trackUri = data.getData();
//               if (data.getExtras() != null && data.getExtras().containsKey(Constants.NAME))
//               {
//                  name = data.getExtras().getString(Constants.NAME);
//               }
//               else
//               {
//                  name = "shareToGobreadcrumbs";
//               }
//               mService.startUploadTask(this, new ShareProgressListener(name), trackUri, name);
//               finish();
//               break;
//            default:
//               super.onActivityResult(requestCode, resultCode, data);
//               break;
//         }
//      }
//   }
//
//   private void sendTweet(String tweet)
//   {
//      final Intent intent = findTwitterClient();
//      intent.putExtra(Intent.EXTRA_TEXT, tweet);
//      if (mImageUri != null)
//      {
//         intent.putExtra(Intent.EXTRA_STREAM, mImageUri);
//      }
//      startActivity(intent);
//      ShareTrack.this.finish();
//   }
//
//   private void sendSMS(String msg)
//   {
//      final Intent intent = new Intent(Intent.ACTION_VIEW);
//      intent.setType("vnd.android-dir/mms-sms");
//      intent.putExtra("sms_body", msg);
//      startActivity(intent);
//   }
//
//   private void sentGenericText(String subject, String msg)
//   {
//      final Intent intent = new Intent(Intent.ACTION_SEND);
//      intent.setType("text/plain");
//      intent.putExtra(Intent.EXTRA_SUBJECT, subject);
//      intent.putExtra(Intent.EXTRA_TEXT, msg);
//      if (mImageUri != null)
//      {
//         intent.putExtra(Intent.EXTRA_STREAM, mImageUri);
//      }
//      startActivity(intent);
//   }
//
//   private void sendToBreadcrumbs(Uri mTrackUri, String chosenFileName)
//   {
//      // Start a description of the track
//      Intent namingIntent = new Intent(this, DescribeTrack.class);
//      namingIntent.setData(mTrackUri);
//      namingIntent.putExtra(Constants.NAME, chosenFileName);
//      startActivityForResult(namingIntent, DESCRIBE);
//   }
//
//   private Intent findTwitterClient()
//   {
//      final String[] twitterApps = {
//            // package // name
//            "com.twitter.android", // official
//            "com.twidroid", // twidroyd
//            "com.handmark.tweetcaster", // Tweecaster
//            "com.thedeck.android" // TweetDeck
//      };
//      Intent tweetIntent = new Intent(Intent.ACTION_SEND);
//      tweetIntent.setType("text/plain");
//      final PackageManager packageManager = getPackageManager();
//      List<ResolveInfo> list = packageManager.queryIntentActivities(tweetIntent, PackageManager.MATCH_DEFAULT_ONLY);
//      for (int i = 0; i < twitterApps.length; i++)
//      {
//         for (ResolveInfo resolveInfo : list)
//         {
//            String p = resolveInfo.activityInfo.packageName;
//            if (p != null && p.startsWith(twitterApps[i]))
//            {
//               tweetIntent.setPackage(p);
//            }
//         }
//      }
//      return tweetIntent;
//   }
//
//   private void createTweetText()
//   {
//      StatisticsCalulator calculator = new StatisticsCalulator(this, new UnitsI18n(this), this);
//      findViewById(R.id.tweet_progress).setVisibility(View.VISIBLE);
//      calculator.execute(mTrackUri);
//   }
//
//   @Override
//   public void finishedCalculations(StatisticsCalulator calculated)
//   {
//      String name = queryForTrackName(getContentResolver(), mTrackUri);
//      String distString = calculated.getDistanceText();
//      String avgSpeed = calculated.getAvgSpeedText();
//      String duration = calculated.getDurationText();
//      String tweetText = String.format(getString(R.string.tweettext, name, distString, avgSpeed, duration));
//      if (mTweetView.getText().toString().equals(""))
//      {
//         mTweetView.setText(tweetText);
//      }
//      findViewById(R.id.tweet_progress).setVisibility(View.GONE);
//   }
//
//   private void adjustTargetToType(int position)
//   {
//      switch (position)
//      {
//         case EXPORT_TYPE_KMZ:
//            setKmzExportTargets();
//            mFileNameView.setVisibility(View.VISIBLE);
//            mTweetView.setVisibility(View.GONE);
//            break;
//         case EXPORT_TYPE_GPX:
//            setGpxExportTargets();
//            mFileNameView.setVisibility(View.VISIBLE);
//            mTweetView.setVisibility(View.GONE);
//            break;
//         case EXPORT_TYPE_TEXTLINE:
//            setTextLineExportTargets();
//            mFileNameView.setVisibility(View.GONE);
//            mTweetView.setVisibility(View.VISIBLE);
//            createTweetText();
//            break;
//         default:
//            break;
//      }
//   }
//
//   public static void sendFile(Context context, Uri fileUri, String fileContentType, String body)
//   {
//      Intent sendActionIntent = new Intent(Intent.ACTION_SEND);
//      sendActionIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.email_subject));
//      sendActionIntent.putExtra(Intent.EXTRA_TEXT, body);
//      sendActionIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
//      sendActionIntent.setType(fileContentType);
//      context.startActivity(Intent.createChooser(sendActionIntent, context.getString(R.string.sender_chooser)));
//   }
//
//   public static String queryForTrackName(ContentResolver resolver, Uri trackUri)
//   {
//      Cursor trackCursor = null;
//      String name = null;
//
//      try
//      {
//         trackCursor = resolver.query(trackUri, new String[] { Tracks.NAME }, null, null, null);
//         if (trackCursor.moveToFirst())
//         {
//            name = trackCursor.getString(0);
//         }
//      }
//      finally
//      {
//         if (trackCursor != null)
//         {
//            trackCursor.close();
//         }
//      }
//      return name;
//   }
//
//   public static Uri storeScreenBitmap(Bitmap bm)
//   {
//      Uri fileUri = null;
//      FileOutputStream stream = null;
//      try
//      {
//         clearScreenBitmap();
//
//         sTempBitmap = File.createTempFile("shareimage", ".png");
//         fileUri = Uri.fromFile(sTempBitmap);
//         stream = new FileOutputStream(sTempBitmap);
//         bm.compress(CompressFormat.PNG, 100, stream);
//      }
//      catch (IOException e)
//      {
//         Log.e(TAG, "Bitmap extra storing failed", e);
//      }
//      finally
//      {
//         try
//         {
//            if (stream != null)
//            {
//               stream.close();
//            }
//         }
//         catch (IOException e)
//         {
//            Log.e(TAG, "Bitmap extra close failed", e);
//         }
//      }
//      return fileUri;
//   }
//
//   public static void clearScreenBitmap()
//   {
//      if (sTempBitmap != null && sTempBitmap.exists())
//      {
//         sTempBitmap.delete();
//         sTempBitmap = null;
//      }
//   }
//
//   private void readScreenBitmap()
//   {
//      mImageView.setVisibility(View.GONE);
//      mCloseImageView.setVisibility(View.GONE);
//      if (getIntent().getExtras() != null && getIntent().hasExtra(Intent.EXTRA_STREAM))
//      {
//         mImageUri = getIntent().getExtras().getParcelable(Intent.EXTRA_STREAM);
//         if (mImageUri != null)
//         {
//            InputStream is = null;
//            try
//            {
//               is = getContentResolver().openInputStream(mImageUri);
//               mImageView.setImageBitmap(BitmapFactory.decodeStream(is));
//               mImageView.setVisibility(View.VISIBLE);
//               mCloseImageView.setVisibility(View.VISIBLE);
//               mCloseImageView.setOnClickListener(new View.OnClickListener()
//                  {
//
//                     @Override
//                     public void onClick(View v)
//                     {
//                        removeScreenBitmap();
//                     }
//                  });
//            }
//            catch (FileNotFoundException e)
//            {
//               Log.e(TAG, "Failed reading image from file", e);
//            }
//            finally
//            {
//               if (is != null)
//               {
//                  try
//                  {
//                     is.close();
//                  }
//                  catch (IOException e)
//                  {
//                     Log.e(TAG, "Failed close image from file", e);
//                  }
//               }
//            }
//         }
//      }
//   }
//
//   private void removeScreenBitmap()
//   {
//      mImageView.setVisibility(View.GONE);
//      mCloseImageView.setVisibility(View.GONE);
//      mImageUri = null;
//   }
//
//   private ServiceConnection mConnection = new ServiceConnection()
//      {
//         @Override
//         public void onServiceConnected(ComponentName className, IBinder service)
//         {
//            // We've bound to LocalService, cast the IBinder and get LocalService instance
//            LocalBinder binder = (LocalBinder) service;
//            mService = binder.getService();
//            mBound = true;
//         }
//
//         @Override
//         public void onServiceDisconnected(ComponentName arg0)
//         {
//            mBound = false;
//            mService = null;
//         }
//      };
//
//   private OnClickListener mBreadcrumbsDialogListener = new OnClickListener()
//   {
//      @Override
//      public void onClick(DialogInterface dialog, int which)
//      {
//         mService.collectBreadcrumbsOauthToken();
//      }
//   };
//
//   public class ShareProgressListener implements ProgressListener
//   {
//      private String mFileName;
//      private int mProgress;
//
//      public ShareProgressListener(String sharename)
//      {
//         mFileName = sharename;
//      }
//
//      public void startNotification()
//      {
//         String ns = Context.NOTIFICATION_SERVICE;
//         mNotificationManager = (NotificationManager) ShareTrack.this.getSystemService(ns);
//         int icon = android.R.drawable.ic_menu_save;
//         CharSequence tickerText = getString(R.string.ticker_saving) + "\"" + mFileName + "\"";
//
//         mNotification = new Notification();
//         PendingIntent contentIntent = PendingIntent.getActivity(ShareTrack.this, 0, new Intent(ShareTrack.this, LoggerMap.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
//               PendingIntent.FLAG_UPDATE_CURRENT);
//
//         mNotification.contentIntent = contentIntent;
//         mNotification.tickerText = tickerText;
//         mNotification.icon = icon;
//         mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
//         mContentView = new RemoteViews(getPackageName(), R.layout.savenotificationprogress);
//         mContentView.setImageViewResource(R.id.icon, icon);
//         mContentView.setTextViewText(R.id.progresstext, tickerText);
//
//         mNotification.contentView = mContentView;
//      }
//
//      private void updateNotification()
//      {
//         //         Log.d( "TAG", "Progress " + progress + " of " + goal );
//         if (mProgress > 0 && mProgress < Window.PROGRESS_END)
//         {
//            if ((mProgress * PROGRESS_STEPS) / Window.PROGRESS_END != barProgress)
//            {
//               barProgress = (mProgress * PROGRESS_STEPS) / Window.PROGRESS_END;
//               mContentView.setProgressBar(R.id.progress, Window.PROGRESS_END, mProgress, false);
//               mNotificationManager.notify(R.layout.savenotificationprogress, mNotification);
//            }
//         }
//         else if (mProgress == 0)
//         {
//            mContentView.setProgressBar(R.id.progress, Window.PROGRESS_END, mProgress, true);
//            mNotificationManager.notify(R.layout.savenotificationprogress, mNotification);
//         }
//         else if (mProgress >= Window.PROGRESS_END)
//         {
//            mContentView.setProgressBar(R.id.progress, Window.PROGRESS_END, mProgress, false);
//            mNotificationManager.notify(R.layout.savenotificationprogress, mNotification);
//         }
//      }
//
//      public void endNotification(Uri file)
//      {
//         mNotificationManager.cancel(R.layout.savenotificationprogress);
//      }
//
//      @Override
//      public void setIndeterminate(boolean indeterminate)
//      {
//         Log.w(TAG, "Unsupported indeterminate progress display");
//      }
//
//      @Override
//      public void started()
//      {
//         startNotification();
//      }
//
//      @Override
//      public void setProgress(int value)
//      {
//         mProgress = value;
//         updateNotification();
//      }
//
//      @Override
//      public void finished(Uri result)
//      {
//         endNotification(result);
//      }
//
//      @Override
//      public void showError(String task, String errorDialogMessage, Exception errorDialogException)
//      {
//         endNotification(null);
//
//         mErrorDialogMessage = errorDialogMessage;
//         mErrorDialogException = errorDialogException;
//         if (!isFinishing())
//         {
//            showDialog(DIALOG_ERROR);
//         }
//         else
//         {
//            Toast toast = Toast.makeText(ShareTrack.this, errorDialogMessage, Toast.LENGTH_LONG);
//            toast.show();
//         }
//      }
//
//   }
//}
