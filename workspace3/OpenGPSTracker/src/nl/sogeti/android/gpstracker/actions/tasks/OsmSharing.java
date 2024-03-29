///*------------------------------------------------------------------------------
// **     Ident: Delivery Center Java
// **    Author: rene
// ** Copyright: (c) Jul 9, 2011 Sogeti Nederland B.V. All Rights Reserved.
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
//package nl.sogeti.android.gpstracker.actions.tasks;
//
//import java.io.Closeable;
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.HttpURLConnection;
//import java.net.URL;
//
//import nl.sogeti.android.gpstracker.R;
//import nl.sogeti.android.gpstracker.actions.ShareTrack;
//import nl.sogeti.android.gpstracker.actions.utils.ProgressListener;
//import nl.sogeti.android.gpstracker.db.GPStracking;
//import nl.sogeti.android.gpstracker.db.GPStracking.Media;
//import nl.sogeti.android.gpstracker.db.GPStracking.MetaData;
//import nl.sogeti.android.gpstracker.oauth.PrepareRequestTokenActivity;
//import nl.sogeti.android.gpstracker.util.Constants;
//import nl.sogeti.android.gpstracker.util.MultipartStreamer;
//import oauth.signpost.basic.DefaultOAuthConsumer;
//import oauth.signpost.exception.OAuthCommunicationException;
//import oauth.signpost.exception.OAuthExpectationFailedException;
//import oauth.signpost.exception.OAuthMessageSignerException;
//
//import org.apache.http.HttpException;
//
//import android.app.Activity;
//import android.content.ContentResolver;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.content.SharedPreferences.Editor;
//import android.database.Cursor;
//import android.net.Uri;
//import android.preference.PreferenceManager;
//import android.util.Log;
//import android.widget.Toast;
//
//import static nl.sogeti.android.gpstracker.R.string.OSM_CONSUMER_KEY;
//
///**
// * ????
// *
// * @version $Id:$
// * @author rene (c) Jul 9, 2011, Sogeti B.V.
// */
//public class OsmSharing extends GpxCreator
//{
//
//   public static final String OAUTH_TOKEN = "openstreetmap_oauth_token";
//   public static final String OAUTH_TOKEN_SECRET = "openstreetmap_oauth_secret";
//   private static final String TAG = "OGT.OsmSharing";
//   public static final String OSM_FILENAME = "OSM_Trace";
//   private String responseText;
//   private Uri mFileUri;
//
//   public OsmSharing(Activity context, Uri trackUri, boolean attachments, ProgressListener listener)
//   {
//      super(context, trackUri, OSM_FILENAME, attachments, listener);
//   }
//
//   public void resumeOsmSharing(Uri fileUri, Uri trackUri)
//   {
//      mFileUri = fileUri;
//      mTrackUri = trackUri;
//      execute();
//   }
//
//   @Override
//   protected Uri doInBackground(Void... params)
//   {
//      if (mFileUri == null)
//      {
//         mFileUri = super.doInBackground(params);
//      }
//      sendToOsm(mFileUri, mTrackUri);
//      return mFileUri;
//   }
//
//   @Override
//   protected void onPostExecute(Uri resultFilename)
//   {
//      super.onPostExecute(resultFilename);
//
//      CharSequence text = mContext.getString(R.string.osm_success) + responseText;
//      Toast toast = Toast.makeText(mContext, text, Toast.LENGTH_LONG);
//      toast.show();
//   }
//
//   /**
//    * POST a (GPX) file to the 0.6 API of the OpenStreetMap.org website publishing this track to the public.
//    *
//    * @param fileUri
//    * @param contentType
//    */
//   private void sendToOsm(final Uri fileUri, final Uri trackUri)
//   {
//      DefaultOAuthConsumer consumer = osmConnectionSetup();
//      if (consumer == null)
//      {
//         requestOpenstreetmapOauthToken();
//         handleError(mContext.getString(R.string.osm_task), null, mContext.getString(R.string.osmauth_message));
//      }
//
//      String visibility = PreferenceManager.getDefaultSharedPreferences(mContext).getString(Constants.OSM_VISIBILITY,
//            "trackable");
//      File gpxFile = new File(fileUri.getEncodedPath());
//
//      int statusCode = 0;
//      Cursor metaData = null;
//      String sources = null;
//      HttpURLConnection connection = null;
//      MultipartStreamer multipart = null;
//      try
//      {
//         metaData = mContext.getContentResolver().query(Uri.withAppendedPath(trackUri, "metadata"),
//               new String[] { MetaData.VALUE }, MetaData.KEY + " = ? ", new String[] { Constants.DATASOURCES_KEY },
//               null);
//         if (metaData.moveToFirst())
//         {
//            sources = metaData.getString(0);
//         }
//         if (sources != null)
//         {
//            throw new IOException("Unable to upload track with materials derived from Google Maps.");
//         }
//
//         // The POST to the create node
//         URL url = new URL(mContext.getString(R.string.osm_post_url));
//         connection = (HttpURLConnection) url.openConnection();
//         multipart = new MultipartStreamer(connection);
//         consumer.sign(connection);
//
//         // Build the multipart body with the upload data
//         multipart.addFilePart("file", gpxFile);
//         multipart.addFormField("description", ShareTrack.queryForTrackName(mContext.getContentResolver(), mTrackUri));
//         String tags = mContext.getString(R.string.osm_tag) + " " + queryForNotes();
//         multipart.addFormField("tags", tags);
//         multipart.addFormField("visibility", visibility);
//
//         // Execute the POST to OpenStreetMap
//         statusCode = connection.getResponseCode();
//         InputStream stream = connection.getInputStream();
//         responseText = XmlCreator.convertStreamToString(stream);
//      }
//      catch (OAuthMessageSignerException e)
//      {
//         Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
//         editor.remove(OAUTH_TOKEN);
//         editor.remove(OAUTH_TOKEN_SECRET);
//         editor.commit();
//
//         responseText = mContext.getString(R.string.osm_failed) + e.getLocalizedMessage();
//         handleError(mContext.getString(R.string.osm_task), e, responseText);
//      }
//      catch (OAuthExpectationFailedException e)
//      {
//         Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
//         editor.remove(OAUTH_TOKEN);
//         editor.remove(OAUTH_TOKEN_SECRET);
//         editor.commit();
//
//         responseText = mContext.getString(R.string.osm_failed) + e.getLocalizedMessage();
//         handleError(mContext.getString(R.string.osm_task), e, responseText);
//      }
//      catch (OAuthCommunicationException e)
//      {
//         Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
//         editor.remove(OAUTH_TOKEN);
//         editor.remove(OAUTH_TOKEN_SECRET);
//         editor.commit();
//
//         responseText = mContext.getString(R.string.osm_failed) + e.getLocalizedMessage();
//         handleError(mContext.getString(R.string.osm_task), e, responseText);
//      }
//      catch (IOException e)
//      {
//         responseText = mContext.getString(R.string.osm_failed) + e.getLocalizedMessage();
//         handleError(mContext.getString(R.string.osm_task), e, responseText);
//      }
//      finally
//      {
//         close(multipart);
//         if (connection != null)
//            connection.disconnect();
//         if (metaData != null)
//         {
//            metaData.close();
//         }
//      }
//
//      if (statusCode != 200)
//      {
//         Log.e(TAG, "Failed to upload to error code " + statusCode + " " + responseText);
//         String text = mContext.getString(R.string.osm_failed) + responseText;
//         if (statusCode == 401)
//         {
//            Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
//            editor.remove(OAUTH_TOKEN);
//            editor.remove(OAUTH_TOKEN_SECRET);
//            editor.commit();
//         }
//
//         handleError(mContext.getString(R.string.osm_task), new HttpException("Unexpected status reported by OSM"),
//               text);
//      }
//   }
//
//   private DefaultOAuthConsumer osmConnectionSetup()
//   {
//      final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
//      String token = prefs.getString(OAUTH_TOKEN, "");
//      String secret = prefs.getString(OAUTH_TOKEN_SECRET, "");
//      boolean mAuthorized = !"".equals(token) && !"".equals(secret);
//      DefaultOAuthConsumer consumer = null;
//      if (mAuthorized)
//      {
//         consumer = new DefaultOAuthConsumer(mContext.getString(OSM_CONSUMER_KEY), mContext.getString(R.string.OSM_CONSUMER_SECRET));
//         consumer.setTokenWithSecret(token, secret);
//      }
//      return consumer;
//   }
//
//   private String queryForNotes()
//   {
//      StringBuilder tags = new StringBuilder();
//      ContentResolver resolver = mContext.getContentResolver();
//      Cursor mediaCursor = null;
//      Uri mediaUri = Uri.withAppendedPath(mTrackUri, "media");
//      try
//      {
//         mediaCursor = resolver.query(mediaUri, new String[] { Media.URI }, null, null, null);
//         if (mediaCursor.moveToFirst())
//         {
//            do
//            {
//               Uri noteUri = Uri.parse(mediaCursor.getString(0));
//               if (noteUri.getScheme().equals("content")
//                     && noteUri.getAuthority().equals(GPStracking.AUTHORITY + ".string"))
//               {
//                  String tag = noteUri.getLastPathSegment().trim();
//                  if (!tag.contains(" "))
//                  {
//                     if (tags.length() > 0)
//                     {
//                        tags.append(" ");
//                     }
//                     tags.append(tag);
//                  }
//               }
//            }
//            while (mediaCursor.moveToNext());
//         }
//      }
//      finally
//      {
//         if (mediaCursor != null)
//         {
//            mediaCursor.close();
//         }
//      }
//      return tags.toString();
//   }
//
//   public void requestOpenstreetmapOauthToken()
//   {
//      Intent intent = new Intent(mContext.getApplicationContext(), PrepareRequestTokenActivity.class);
//      intent.putExtra(PrepareRequestTokenActivity.OAUTH_TOKEN_PREF, OAUTH_TOKEN);
//      intent.putExtra(PrepareRequestTokenActivity.OAUTH_TOKEN_SECRET_PREF, OAUTH_TOKEN_SECRET);
//
//      intent.putExtra(PrepareRequestTokenActivity.CONSUMER_KEY, mContext.getString(OSM_CONSUMER_KEY));
//      intent.putExtra(PrepareRequestTokenActivity.CONSUMER_SECRET, mContext.getString(R.string.OSM_CONSUMER_SECRET));
//      intent.putExtra(PrepareRequestTokenActivity.REQUEST_URL, Constants.OSM_REQUEST_URL);
//      intent.putExtra(PrepareRequestTokenActivity.ACCESS_URL, Constants.OSM_ACCESS_URL);
//      intent.putExtra(PrepareRequestTokenActivity.AUTHORIZE_URL, Constants.OSM_AUTHORIZE_URL);
//
//      mContext.startActivity(intent);
//   }
//
//   private void close(Closeable connection)
//   {
//      try
//      {
//         if (connection != null)
//         {
//            connection.close();
//         }
//      }
//      catch (IOException e)
//      {
//         Log.w(TAG, "Failed to close ", e);
//      }
//   }
//}
