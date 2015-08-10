package com.example.smsapp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private EditText phoneNumber, messageText;
	private Button intentButton, sendAndSaveButton;
	ArrayList<String> str = new ArrayList<String>();
	public Boolean firstLvl = true;
	public static final String TAG = "F_PATH";
	public Item[] fileList;
	public File path = new File(Environment.getExternalStorageDirectory() + "");
	public String chosenFile;
	ListAdapter adapter;
	public static final int DIALOG_LOAD_FILE = 1000;	
	public String message;
	public String mainmessage;
	public String mainphone;
	public String phone;
	public String phone2;
	public int i;
	public int stop;
	public int j;
	public TextView Chars;
	public final TextWatcher mTextEditorWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
           //This sets a textview to the current length
           Chars.setText("Characters : " + String.valueOf(s.length()));
        }

        public void afterTextChanged(Editable s) {
        }
	};
	//public SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Chars = (TextView) findViewById(R.id.Chars);
		phoneNumber = (EditText) findViewById(R.id.phoneNumber);
		messageText = (EditText) findViewById(R.id.messageText);
		intentButton = (Button) findViewById(R.id.intentButton);
		sendAndSaveButton = (Button) findViewById(R.id.sendAndSaveButton);		
		
		messageText.addTextChangedListener(mTextEditorWatcher);

		intentButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent smsintent = new Intent(Intent.ACTION_MAIN);
				smsintent.addCategory(Intent.CATEGORY_LAUNCHER);
				smsintent.setClassName("com.android.mms",
						"com.android.mms.ui.ConversationList");
				startActivity(smsintent);

			}

		});

		sendAndSaveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (phoneNumber.getText().toString().trim().length() == 0) {
					Toast.makeText(getApplicationContext(),
							"Please enter a Phone Number.", Toast.LENGTH_LONG)
							.show();
					return;
				}

				if (messageText.getText().toString().trim().length() == 0) {
					Toast.makeText(getApplicationContext(),
							"Please enter your message.", Toast.LENGTH_LONG)
							.show();
					return;
				}

				if (messageText.getText().toString().trim().length() > 160) {
					sendLongSMS();
					saveInSent();

				} else {
					sendSMS();
					// Save in SENT folder
					saveInSent();
				}
			}

		});

	}

	public void sendSMS() {

		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(phoneNumber.getText().toString(), null,
				messageText.getText().toString(), null, null);

		Toast.makeText(getApplicationContext(), "Message Sent!",
				Toast.LENGTH_LONG).show();
	}

	public void sendLongSMS() {
		SmsManager smsManager = SmsManager.getDefault();
		ArrayList<String> parts = smsManager.divideMessage(messageText
				.getText().toString());
		smsManager.sendMultipartTextMessage(phoneNumber.getText().toString(),
				null, parts, null, null);

		Toast.makeText(getApplicationContext(), "Message Sent!",
				Toast.LENGTH_LONG).show();
	}

	public void invokeSMSApp() {
		Intent smsintent = new Intent(Intent.ACTION_MAIN);

		// smsIntent.putExtra("sms_body", messageText.getText().toString());
		// smsIntent.putExtra("address", phoneNumber.getText().toString());
		// smsIntent.setType("vnd.android-dir/mms-sms");
		// smsIntent.setData(Uri.parse("content://sms/inbox"));
		smsintent.setType("vnd.android-dir/mms-sms");
		smsintent.setData(Uri.parse("content://sms/inbox"));
		// smsintent.setData(Uri.parse("sms:"));
		startActivity(smsintent);

		// String SMS_MIME_TYPE = "vnd.android-dir/mms-sms";
		// Intent defineIntent = new Intent(Intent.ACTION_MAIN);
		// defineIntent.setType(SMS_MIME_TYPE);
		// PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
		// defineIntent, 0);

	}

	public void saveInSent() {
		ContentValues values = new ContentValues();

		values.put("address", phoneNumber.getText().toString());

		values.put("body", messageText.getText().toString());

		getContentResolver().insert(Uri.parse("content://sms/sent"), values);
	}

	public void file_explorer(View view) {

		view = findViewById(R.id.button_send);
		loadFileList();
		showDialog(DIALOG_LOAD_FILE);
		

	}

	public void loadFileList() {
		path.mkdirs();

		if (path.exists()) {
			FilenameFilter filter = new FilenameFilter() {
				@Override
				public boolean accept(File dir, String filename) {
					File sel = new File(dir, filename);
					return (sel.isFile() || sel.isDirectory())
							&& !sel.isHidden();

				}
			};

			String[] fList = path.list(filter);
			fileList = new Item[fList.length];
			for (int i = 0; i < fList.length; i++) {
				fileList[i] = new Item(fList[i], R.drawable.files_icon);
				File sel = new File(path, fList[i]);

				if (sel.isDirectory()) {
					fileList[i].icon = R.drawable.directory_icon;

				}
			}

			if (!firstLvl) {
				Item temp[] = new Item[fileList.length + 1];
				for (int i = 0; i < fileList.length; i++) {
					temp[i + 1] = fileList[i];
				}
				temp[0] = new Item("Up", R.drawable.directory_up);
				fileList = temp;
			}
		}

		adapter = new ArrayAdapter<Item>(this,
				android.R.layout.select_dialog_item, android.R.id.text1,
				fileList) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				TextView textView = (TextView) view
						.findViewById(android.R.id.text1);
				textView.setCompoundDrawablesWithIntrinsicBounds(
						fileList[position].icon, 0, 0, 0);
				int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);
				textView.setCompoundDrawablePadding(dp5);

				return view;
			}
		};

	}

	public class Item {
		public String file;
		public int icon;

		public Item(String file, Integer icon) {
			this.file = file;
			this.icon = icon;
		}

		@Override
		public String toString() {
			return file;
		}
	}

	@Override
	public Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		if (fileList == null) {
			Log.e(TAG, "No files loaded");
			dialog = builder.create();
			return dialog;
		}

		switch (id) {
		case DIALOG_LOAD_FILE:
			builder.setTitle("Choose your file");
			builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					chosenFile = fileList[which].file;
					File sel = new File(path + "/" + chosenFile);
					if (sel.isDirectory()) {
						firstLvl = false;
						str.add(chosenFile);
						fileList = null;
						path = new File(sel + "");

						loadFileList();
						removeDialog(DIALOG_LOAD_FILE);
						showDialog(DIALOG_LOAD_FILE);

						Log.d(TAG, path.getAbsolutePath());

					} else if (chosenFile.equalsIgnoreCase("up")
							&& !sel.exists()) {
						String s = str.remove(str.size() - 1);
						path = new File(path.toString().substring(0,
								path.toString().lastIndexOf(s)));
						fileList = null;
						if (str.isEmpty()) {
							firstLvl = true;
						}
						loadFileList();
						removeDialog(DIALOG_LOAD_FILE);
						showDialog(DIALOG_LOAD_FILE);

						Log.d(TAG, path.getAbsolutePath());

					}
					// File picked
					else {
						Log.d(TAG, "everything okay till here");
						//String csvList = pref.getString("mylist", "911,121");
						//String[] items = csvList.split(",");
						//List<String> list = new ArrayList<String>();
						//for(integer i=0; i < items.length; i++){
						//     list.add(items[i]);     
						//}
						/*String[] phone2; 
						File sel2 = new File("/storage/emulated/0/main.txt");
						InputStream in2 = null;
						try {
							in2 = new BufferedInputStream(new FileInputStream(
									sel2));
							Log.d(TAG, "read stream");
						} catch (Exception e) {
							Log.d(TAG, "could not read database stream");
							Toast.makeText(getApplicationContext(),
									"Database File error", Toast.LENGTH_LONG).show();
						}
						BufferedReader reader2 = new BufferedReader(
								new InputStreamReader(in2));
						String line2;
						try {
							while ((line2 = reader2.readLine()) != null) {
								Log.d(TAG, line2);
								phone2.append(line2);
							}							
							
						} catch(Exception e) {

							Log.d(TAG, "could not read database line");
							Toast.makeText(getApplicationContext(),
									"Database Read error", Toast.LENGTH_LONG).show();							
						}*/						
						

						Log.d(TAG, "this is the place");
						removeDialog(DIALOG_LOAD_FILE);
						Log.d(TAG, sel.getAbsolutePath());

						InputStream in = null;
						try {
							in = new BufferedInputStream(new FileInputStream(
									sel));
							Log.d(TAG, "read stream");
						} catch (Exception e) {
							Log.d(TAG, "could not read stream");
							Toast.makeText(getApplicationContext(),
									"File error", Toast.LENGTH_LONG).show();
						}
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(in));
						String line;
						Boolean dont = true;
						j = 0;
						try {							
							while ((line = reader.readLine()) != null) {
								
								Log.d(TAG, line);
								line.trim();
								if (line.indexOf("#") >= 0){
									mainmessage = line;
									mainmessage = mainmessage.replaceFirst("#","");
									mainmessage = mainmessage.replaceAll(",","");									
									message = mainmessage;									
									mainphone = "#";
									phone = mainphone;
									dont = false;
									continue ;
																		
								} else if (dont==true){								
									message = messageText.getText().toString();								
									phone = phoneNumber.getText().toString();
								}
								else {
									try{
										message = mainmessage;
										phone = mainphone;
										j++ ;
									} catch(Exception e) {
										Toast.makeText(getApplicationContext(),"Message error",Toast.LENGTH_LONG).show();
										return;										
									}
								}
								
								Log.d(TAG, phone);
								Log.d(TAG, message);								
								if (phone.length() == 0) {
									Toast.makeText(getApplicationContext(),
											"Please enter a Phone Number.",
											Toast.LENGTH_LONG).show();
									return;
								}
								//stop = 0;
								if (phone.equals("#")) {
									if (line.indexOf(",") >= 0) {
										String[] RowData = line.split(",");
										phone = phone.replace("#", RowData[0]);
										Log.d(TAG, "hjdjd");
										Log.d(TAG, "hjdjd1");
										for (i = 1; i < RowData.length; i++) {
											message = message.replaceFirst("#",
													RowData[i]);

										}

									} else {
										phone = phone.replace("#", line);
									}

								} else {
									if (line.indexOf(",") >= 0) {
										String[] RowData = line.split(",");
										for (i = 0; i < RowData.length; i++) {
											message = message.replaceFirst("#",
													RowData[i]);
										}

									} else {
										message = message.replace("#", line);
									}

								}
								Log.d(TAG, phone);
								Log.d(TAG, message);
								//for(int j=0; j < items.length; j++){
								 //    if (items[j].equals(phone)) { 
								 //   	 stop=1;
								 //    }   
								     
								//}
								
								//if (stop !=1){
									//csvList = csvList.concat(",").concat(phone);
									//Editor editor = pref.edit();
									//editor.remove("mylist");
									//editor.putString("mylist", csvList);
									//editor.commit();
									phone.trim();
									if (phone.length() <= 10){
										phone = "0" + phone;
									}
									Log.d(TAG, phone);
									Log.d(TAG, message);
									if (message.length() > 160) {
										customsendLongSMS();
										customsaveInSent();
	
									} else {
										customsendSMS();
										customsaveInSent();
									}
								//}

							};
							
							Toast.makeText(getApplicationContext(), String.valueOf(j)+ " Messages Sent", Toast.LENGTH_LONG).show();
							Calendar c = Calendar.getInstance(); 
							int date = c.get(Calendar.DATE);
							int hours = c.get(Calendar.HOUR_OF_DAY);
							int minutes = c.get(Calendar.MINUTE);
							int seconds = c.get(Calendar.SECOND);
							String root = Environment.getExternalStorageDirectory().getPath();
							String path = root + "/SMSlogs/";
						    File file = new File(path);
						    file.mkdirs();
						    Log.d(TAG,String.valueOf(date));
						    
						    path += String.valueOf(date) +"_" + String.valueOf(hours) + "_" + String.valueOf(minutes) +"_" + String.valueOf(seconds)+ ".txt";
						    Log.d(TAG,path);
						    OutputStream myOutput;
						    try {
						    myOutput = new BufferedOutputStream(new FileOutputStream(path,true));
						    myOutput.write(new String(String.valueOf(j)+ " Messages Sent").getBytes());
						    myOutput.flush();
						    myOutput.close();
						    } catch (FileNotFoundException e) {
						        e.printStackTrace();
						    } catch (IOException e) {
						        e.printStackTrace();
						    }	
							
						} catch (Exception e) {
							Log.d(TAG, "could not read line");
							Toast.makeText(getApplicationContext(),
									"Read error", Toast.LENGTH_LONG).show();
						}

					}

				}
			});
			break;
		}
		dialog = builder.show();
		return dialog;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	public void customsendLongSMS() {
		SmsManager smsManager = SmsManager.getDefault();
		ArrayList<String> parts = smsManager.divideMessage(message);
		smsManager.sendMultipartTextMessage(phone, null, parts, null, null);

		Toast.makeText(getApplicationContext(), "Sending" +" Message " + String.valueOf(j) + " ...", Toast.LENGTH_LONG)
				.show();
	}

	public void customsendSMS() {

		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(phone, null, message, null, null);

		Toast.makeText(getApplicationContext(), "Sending" +" Message " + String.valueOf(j) + " ...", Toast.LENGTH_LONG)
				.show();
	}

	public void customsaveInSent() {
		ContentValues values = new ContentValues();

		values.put("address", phone);

		values.put("body", message);

		getContentResolver().insert(Uri.parse("content://sms/sent"), values);
	}
	

}
