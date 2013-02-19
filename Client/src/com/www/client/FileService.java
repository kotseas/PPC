package com.www.client;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import dalvik.system.DexClassLoader;
public class FileService extends Service {
	private static final String TAG = "FileService";
	private static final int API = android.os.Build.VERSION.SDK_INT;
	public static final String WIFI = "Wi-Fi";
	public static final String ANY = "Any";
	private static final String SERVER = "http://83.212.101.72:8080/Server/webresources/file";
	//private static final String SERVER = "http://192.168.2.3:8084/Server/webresources/file";
	private static final String TEST = SERVER + "/test";
	private static final String DOWNLOAD = SERVER + "/download";
	private static final String CHECK_DOWNLOAD = SERVER + "/check_download";
	private static final String RE_DOWNLOAD = SERVER + "/re_download";
	private static final String UPLOAD = SERVER + "/upload";
	private static final String CHECK_UPLOAD = SERVER + "/check_upload";
	private static final String SDCARD = Environment.getExternalStorageDirectory().getPath();
	private static final String CLIENT = SDCARD + "/Client";
	private static File file = null;
	private static String fileName = "";
	private static String className = "";
	private static Class<Object> classToLoad = null;
	private static Object object = null;
	private static Method method = null;
	private static LogTask logTask = null;
	private static String log = "";
	private static boolean wifiConnected = false;
	private static boolean mobileConnected = false;
	public static boolean isConnected = true;
	private static NetworkReceiver networkReceiver = null;
	private static HttpGet httpGet = null;
	private static HttpPost httpPost = null;
	private static DownloadTask downloadTask = null;
	private static CheckDownloadTask checkDownloadTask = null;
	private static ReDownloadTask reDownloadTask = null;
	private static UploadTask uploadTask = null;
	private static CheckUploadTask checkUploadTask = null;
	private static TestTask testTask = null;
	private static SharedPreferences sharedPreferences = null;
	private static SharedPreferences.Editor editor = null;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "onCreate: ON");
		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		networkReceiver = new NetworkReceiver();
		this.registerReceiver(networkReceiver, filter);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "onStartCommand: Received start id " + startId + ": " + intent);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		editor = sharedPreferences.edit();
		fileName = sharedPreferences.getString("fileName", "");
		//////////
		//onTest();
		//editor.putString("serviceStatus", "DOWNLOAD");
		//editor.commit();
		onStart();
		return START_STICKY;
	}

	private void onTest() {
		testTask = new TestTask();
		testTask.execute(TEST);
	}
	private class TestTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			Log.i(TAG, "TestTask: Testing...");
			String result = "";
			HttpResponse response = httpResponse(urls[0], 0);
			if(response != null) {
				Log.i(TAG, "TestTask: not null");
				try {
					result = writeToString(response.getEntity().getContent());
					Log.i(TAG, "TestTask: " + result);
				} catch (IllegalStateException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				if(result == null || result.isEmpty()) {
					result = "Oops!";
				}
				else result = "OK";
				return result;
			}
			else {
				Log.i(TAG, "TestTask: Oops!");
				return getResources().getString(R.string.connection_error);
			}
		}
		protected void onPostExecute(String result) {
			Log.i(TAG, "TestTask: " + result);
			//Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
			onTest();
		}
	}

	public void onStart() {
		Log.i(TAG, "onStart: Starting/Resuming...");
		if(sharedPreferences.getString("serviceStatus", "DOWNLOAD").equals("DOWNLOAD")) {
			download();
		}
		else if(sharedPreferences.getString("serviceStatus", "DOWNLOAD").equals("CHECK_DOWNLOAD")) {
			checkDownload();
		}
		else if(sharedPreferences.getString("serviceStatus", "DOWNLOAD").equals("RE_DOWNLOAD")) {
			reDownload();
		}
		else if(sharedPreferences.getString("serviceStatus", "EXECUTE").equals("EXECUTE")) {
			if(sharedPreferences.getString("exeStatus", "EXECUTE").equals("EXECUTE")) {
				execute();
			}
			else if(sharedPreferences.getString("exeStatus", "EXECUTE").equals("LOG")) {
				logTask = new LogTask();
				logTask.execute();
			}
			else if(sharedPreferences.getString("exeStatus", "EXECUTE").equals("UPLOAD")) {
				upload();
			}
			else if(sharedPreferences.getString("exeStatus", "EXECUTE").equals("CHECK_UPLOAD")) {
				checkUpload();
			}
			else if(sharedPreferences.getString("exeStatus", "EXECUTE").equals("END")) {
				classToLoad = null;
				Log.i(TAG, "END");
				editor.putString("serviceStatus", "DOWNLOAD");
				editor.putString("fileName", "");
				editor.commit();
				download();
			}
		}
	}

	public void onStop() {
		if(httpGet != null) {
			httpGet.abort();
			if(downloadTask != null) downloadTask.cancel(true);
			if(checkDownloadTask != null) checkDownloadTask.cancel(true);
			if(reDownloadTask != null) {
				reDownloadTask.cancel(true);
				if(sharedPreferences.getString("serviceStatus", "DOWNLOAD").equals("RE_DOWNLOAD")) {
					editor.putString("serviceStatus", "CHECK_DOWNLOAD");
					editor.commit();
				}
			}
		}
		if(httpPost != null) httpPost.abort();
		if(logTask != null) logTask.cancel(true);
		if(uploadTask != null) uploadTask.cancel(true);
		if(checkUploadTask != null) checkUploadTask.cancel(true);
		if(classToLoad != null) {
			try {
				method = classToLoad.getMethod("destroy");
				method.invoke(object);
				method = classToLoad.getMethod("getCounter");
				editor.putInt("counter", (Integer)method.invoke(object));
				editor.commit();
				method = classToLoad.getMethod("getLog");
				String newLog = (String)method.invoke(object);
				log = log + newLog;
				classToLoad = null;
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		if(log != null && !log.isEmpty()) {
			try {
				writeToFile(CLIENT + "/" + fileName + "/" + fileName + ".log.part", log);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(searchStringFor(log, "{EOL}")) {
				editor = sharedPreferences.edit();
				editor.putString("exeStatus", "UPLOAD");
				editor.commit();				
			}
			log = "";
		}
		if(sharedPreferences.getBoolean("logStatus", true)) {
			editor.putString("exeStatus", "EXECUTE");
			editor.commit();
		}
		if(testTask != null) testTask.cancel(true);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.e(TAG, "onDestroy: Shutting down...");
		if(networkReceiver != null) this.unregisterReceiver(networkReceiver);
		onStop();
		Log.e(TAG, "OFF");
	}

	private void download() {
		if(isOnline()) {
			Log.i(TAG, "download: Online");
			downloadTask = new DownloadTask();
			downloadTask.execute(DOWNLOAD);
		}
		else {
			Log.i(TAG, "download: Offline");
			isConnected = false;
		}
	}

	private void checkDownload() {
		if(isOnline()) {
			Log.i(TAG, "checkDownload: Online");
			file = new File(CLIENT + "/" + fileName + "/" + fileName + ".zip");
			Log.i(TAG, "checkDownload: " + fileName + "_" + file.length());
			checkDownloadTask = new CheckDownloadTask();
			checkDownloadTask.execute(CHECK_DOWNLOAD + "/" + fileName + "_" + file.length());
		}
		else {
			Log.i(TAG, "checkDownload: Offline");
			isConnected = false;
		}
	}

	private void reDownload() {
		if(isOnline()) {
			Log.i(TAG, "reDownload: Online");
			file = new File(CLIENT + "/" + fileName + "/" + fileName + ".zip");
			Log.i(TAG, "reDownload: " + fileName + "_" + file.length());
			reDownloadTask = new ReDownloadTask();
			reDownloadTask.execute(RE_DOWNLOAD + "/" + fileName + "_" + file.length());
		}
		else {
			Log.i(TAG, "reDownload: Offline");
			isConnected = false;
		}
	}

	private void execute() {
		Log.i(TAG, "execute: Executing...");
		try {
			className = sharedPreferences.getString("className", "");
			String url = CLIENT + "/" + fileName + "/" + fileName + ".zip";
			Log.i(TAG, "execute: " + fileName + "|" + className);
			File dir = getDir("dex", 0);
			DexClassLoader classLoader = new DexClassLoader(url, dir.getAbsolutePath(), null, this.getClass().getClassLoader());
			classToLoad = (Class<Object>) classLoader.loadClass(className);
			object = classToLoad.newInstance();
			method = classToLoad.getMethod("main", new Class[] { Context.class, int.class });
			method.invoke(object, new Object[] { this.getApplicationContext(), sharedPreferences.getInt("counter", 0) });
			editor.putString("exeStatus", "LOG");
			editor.commit();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		logTask = new LogTask();
		logTask.execute();
	}

	private void upload() {
		if(isOnline()) {
			Log.i(TAG, "upload: Online");
			Log.i(TAG, "upload: " + fileName);
			uploadTask = new UploadTask();
			uploadTask.execute(UPLOAD + "/" + fileName);
		}
		else {
			Log.i(TAG, "upload: Offline");
			isConnected = false;
			if(sharedPreferences.getBoolean("logStatus", true)) {
				editor.putString("exeStatus", "LOG");
				editor.commit();
				logTask = new LogTask();
				logTask.execute();
			}
		}
	}

	private void checkUpload() {
		if(isOnline()) {
			Log.i(TAG, "checkUpload: Online");
			File file = new File(CLIENT + "/" + fileName + "/" + fileName + ".log");
			Log.i(TAG, "checkUpload: " + fileName + "_" + file.length());
			checkUploadTask = new CheckUploadTask();
			checkUploadTask.execute(CHECK_UPLOAD + "/" + fileName + "_" + file.length());
		}
		else {
			Log.i(TAG, "checkUpload: Offline");
			isConnected = false;
		}
	}

	private class DownloadTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			Log.i(TAG, "DownloadTask: Downloading...");
			String result = "";
			HttpResponse response = httpResponse(urls[0], 0);
			if(response != null) {
				try {
					Header header = response.getFirstHeader("Content-Disposition");
					if(header != null) {
						fileName = getParameter(header.toString(), 1);
						className = getParameter(header.toString(), 2);
						Log.i(TAG, "DownloadTask: " + fileName + "|" + className);
						new File(CLIENT + "/" + fileName).mkdir();
						editor.putString("serviceStatus", "CHECK_DOWNLOAD");
						editor.putString("className", className);
						editor.putString("fileName", fileName);
						editor.commit();
						writeToFile(response.getEntity().getContent(), CLIENT + "/" + fileName + "/" + fileName + ".zip");
					}
					else return "The Server did not respond";
					return "File downloaded successfully";
				}
				catch (IllegalStateException e) {
					e.printStackTrace();
					return "IllegalStateException";
				} catch (IOException e) {
					e.printStackTrace();
					return "IOException";
				}
			}
			else return "Connection error";
		}
		protected void onPostExecute(String result) {
			Log.i(TAG, "DownloadTask: " + result);
			onStart();
		}
	}

	private class CheckDownloadTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			Log.i(TAG, "CheckDownloadTask: Checking...");
			HttpResponse response = httpResponse(urls[0], 0);
			if(response != null) {
				try {
					return writeToString(response.getEntity().getContent());
				}
				catch (IllegalStateException e) {
					e.printStackTrace();
					return e.toString();
				} catch (IOException e) {
					e.printStackTrace();
					return e.toString();
				}
			}
			else return getResources().getString(R.string.connection_error);
		}
		protected void onPostExecute(String result) {
			Log.i(TAG, "CheckDownloadTask: " + result);
			if(result.equals("OK")) {
				editor.putString("serviceStatus", "EXECUTE");
				editor.putString("exeStatus", "EXECUTE");
				editor.commit();
			}
			else if(result.equals("Retry")) {
				editor.putString("serviceStatus", "RE_DOWNLOAD");
				editor.commit();
			}
			else {
				editor.putString("serviceStatus", "DOWNLOAD");
				editor.putString("fileName", "");
				editor.commit();
				file.delete();
			}
			onStart();
		}
	}

	private class ReDownloadTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			Log.i(TAG, "ReDownloadTask: Redownloading...");
			HttpResponse response = httpResponse(urls[0], 0);
			if(response != null) {
				editor.putString("serviceStatus", "CHECK_DOWNLOAD");
				editor.commit();
				try {
					mergeParts(response.getEntity().getContent(), CLIENT + "/" + fileName + "/" + fileName + ".zip");
					return "File resumed";
				}
				catch (IllegalStateException e) {
					e.printStackTrace();
					return "IllegalStateException";
				} catch (IOException e) {
					e.printStackTrace();
					return "IOException";
				}
			}
			else return getResources().getString(R.string.connection_error);
		}
		protected void onPostExecute(String result) {
			Log.i(TAG, "ReDownloadTask: " + result);
			onStart();
		}
	}

	private class LogTask extends AsyncTask<String, Void, Boolean> {
		@Override
		protected Boolean doInBackground(String... arg0) {
			while(true) {
				try {
					method = classToLoad.getMethod("getLog");
					log = (String)method.invoke(object);
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				if(log != null && !log.isEmpty()) {
					Log.i(TAG, "LogTask: " + log);
					try {
						//counter = (Integer)method.invoke(object);
						method = classToLoad.getMethod("getCounter");
						editor.putInt("counter", (Integer)method.invoke(object));
						editor.putString("exeStatus", "UPLOAD");
						editor.commit();
						writeToFile(CLIENT + "/" + fileName + "/" + fileName + ".log.part", log);
						log = "";
					} catch (IOException e) {
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
					break;
				}
			}
			return true;
		}
		protected void onPostExecute(Boolean result) {
			upload();
		}
	}

	private class UploadTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			HttpResponse response = httpResponse(urls[0], 1);
			if(response != null) {
				editor.putString("exeStatus", "CHECK_UPLOAD");
				editor.commit();
				return "Log uploaded successfully";
			}
			else return getResources().getString(R.string.connection_error);
		}
		protected void onPostExecute(String result) {
			Log.i(TAG, "UploadTask: " + result);
			try {
				File part = new File(CLIENT + "/" + fileName + "/" + fileName + ".log.part");
				writeToFile(CLIENT + "/" + fileName + "/" + fileName + ".log", FileUtils.readFileToString(part));
			} catch (IOException e) {
				e.printStackTrace();
			}
			onStart();
		}
	}

	private class CheckUploadTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			Log.i(TAG, "CheckUploadTask: Checking...");
			HttpResponse response = httpResponse(urls[0], 0);
			if(response != null) {
				try {
					return writeToString(response.getEntity().getContent());
				}
				catch (IllegalStateException e) {
					e.printStackTrace();
					return e.toString();
				} catch (IOException e) {
					e.printStackTrace();
					return e.toString();
				}
			}
			else return getResources().getString(R.string.connection_error);
		}
		protected void onPostExecute(String result) {
			Log.i(TAG, "CheckUploadTask: " + result);
			if(result.equals("OK")) {
				Log.i(TAG, "CheckUploadTask: deleting " + fileName + ".log.part");
				File part = new File(CLIENT + "/" + fileName + "/" + fileName + ".log.part");
				part.delete();
				if(sharedPreferences.getBoolean("logStatus", true)) {
					editor.putString("exeStatus", "LOG");
					editor.commit();
				}
				else {
					editor.putString("exeStatus", "END");
					editor.putString("fileName", "");
					editor.putString("serviceStatus", "DOWNLOAD");
					editor.commit();
				}
			}
			onStart();
		}
	}

	private HttpResponse httpResponse(String url, int task) {
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse httpResponse = null;
		try {
			if(task == 0) {
				httpGet = new HttpGet(url);
				httpResponse = httpClient.execute(httpGet);
			}
			else if(task == 1) {
				httpPost = new HttpPost(url);
				String s = FileUtils.readFileToString(new File(CLIENT + "/" + fileName + "/" + fileName + ".log.part"));
				StringEntity entity = new StringEntity(s, HTTP.UTF_8);
				httpPost.setEntity(entity);
				httpResponse = httpClient.execute(httpPost);
			}
		}
		catch (Exception e) {
			Log.e(TAG, e.getLocalizedMessage(), e);
		}
		return httpResponse;
	}

	private void writeToFile(InputStream input, String url) throws IOException {
		OutputStream output = new FileOutputStream(new File(url));
		int read;
		byte[] buffer = new byte[1024];
		while((read = input.read(buffer)) > 0) {
			output.write(buffer, 0, read);
		}
		close(output);
		close(input);
	}

	public void mergeParts(InputStream input, String fileName) throws IOException {
		File file = new File(fileName);
		Long length = file.length();
		int read = 0;
		byte[] buffer = new byte[1024];
		RandomAccessFile output;
		output = new RandomAccessFile(file, "rw");
		output.seek(length);
		while((read = input.read(buffer)) > 0) {
			output.write(buffer, 0, read);
		}
		close(input);
		close(output);
	}

	public void mergeParts(String fileName, String partName) throws IOException {
		File file = new File(fileName);
		File part = new File(partName);
		Long length = file.length();
		InputStream input = new FileInputStream(partName);
		int read = 0;
		byte[] buffer = new byte[1024];
		RandomAccessFile output;
		output = new RandomAccessFile(file, "rw");
		output.seek(length);
		while((read = input.read(buffer)) > 0) {
			output.write(buffer, 0, read);
		}
		close(input);
		close(output);
		part.delete();
	}

	private String writeToString(InputStream input) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(input));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		String result = sb.toString();
		close(br);
		close(input);
		return result;
	}

	private String getParameter(String line, int element) {
		String parameter = "";
		int counter = 0;
		Pattern p = Pattern.compile("\"([^\"]*)\"");
		Matcher m = p.matcher(line);
		while (m.find()) {
			if(element - counter == 1) {
				parameter = parameter + m.group(1);
				break;
			}
			else counter++;
		}
		return parameter;
	}

	public void writeToFile(String filePath, String string) throws IOException {
		File file = new File(filePath);
		if(!file.exists()) file.createNewFile();
		FileWriter fileWriter = new FileWriter(file, true);
		fileWriter.write(string);
		close(fileWriter);
		if(searchStringFor(string, "{EOL}")) {
			editor.putBoolean("logStatus", false);
			editor.commit();
		}
	}

	public boolean searchStringFor(String string, String phrase) {
		int index = string.indexOf(phrase);
		if(index >= 0)return true;
		else return false;
	}

	public class NetworkReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if((WIFI.equals(sharedPreferences.getString("listPref", "Wi-Fi")) && networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) ||
					(ANY.equals(sharedPreferences.getString("listPref", "Wi-Fi")) && networkInfo != null)) {
				//isConnected = true;
				//if(!isRunning) onStart();
				if(!isConnected) {
					onStop();
					isConnected = true;
					onStart();
				}
			}
			else isConnected = false;
		}
	}

	private boolean isOnline() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
		if (activeInfo != null && activeInfo.isConnected()) {
			wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
			mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
		}
		else {
			wifiConnected = false;
			mobileConnected = false;
		}
		if(((sharedPreferences.getString("listPref", "Wi-Fi").equals(ANY)) && (wifiConnected || mobileConnected)) || ((sharedPreferences.getString("listPref", "Wi-Fi").equals(WIFI)) && (wifiConnected))) {
			return true;
		}
		else {
			return false;
		}
	}

	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	public boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) ||
				Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}

	private static void close(Closeable resource) {
		if (resource != null) {
			try {
				resource.close();
			}
			catch (IOException ignore) {}
		}
	}

}
