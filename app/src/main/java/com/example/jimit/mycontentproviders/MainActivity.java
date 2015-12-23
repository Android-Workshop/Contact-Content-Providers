package com.example.jimit.mycontentproviders;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static  final String TAG = MainActivity.class.getSimpleName();

    private EditText txtName, txtPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!AppPermission.isPermissionRequestRequired(MainActivity.this, new String[] {"android.permission.READ_CONTACTS"}, 100)) {
            readContacts();
        }

        txtName = (EditText) findViewById(R.id.txt_displayname);
        txtPhone = (EditText) findViewById(R.id.txt_phone);
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!AppPermission.isPermissionRequestRequired(MainActivity.this, new String[] {"android.permission.WRITE_CONTACTS"}, 101)) {
                    if (!TextUtils.isEmpty(txtName.getText().toString())
                            && !TextUtils.isEmpty(txtPhone.getText().toString())
                            && txtPhone.getText().length() == 10) {
                        insertContact(txtName.getText().toString(), txtPhone.getText().toString());
                    }
                }
            }
        });

        Button btnDelete = (Button) findViewById(R.id.button2);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!AppPermission.isPermissionRequestRequired(MainActivity.this, new String[] {"android.permission.WRITE_CONTACTS"}, 102)) {
                    if (!TextUtils.isEmpty(txtName.getText().toString())) {
                        deleteContact(txtName.getText().toString());
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (100 == requestCode) {
            if (permissions.length > 0 && permissions[0].equalsIgnoreCase("android.permission.READ_CONTACTS")
                    && grantResults.length > 0 &&  PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                readContacts();
            }
        } else if (101 == requestCode) {
            if (!AppPermission.isPermissionRequestRequired(MainActivity.this, new String[] {"android.permission.WRITE_CONTACTS"}, 101)) {
                if (!TextUtils.isEmpty(txtName.getText().toString())) {
                    deleteContact(txtName.getText().toString());
                }
            }
        } else if (102 == requestCode) {
            if (!AppPermission.isPermissionRequestRequired(MainActivity.this, new String[] {"android.permission.WRITE_CONTACTS"}, 101)) {
                if (!TextUtils.isEmpty(txtName.getText().toString())) {
                    deleteContact(txtName.getText().toString());
                }
            }
        }
    }

    private void readContacts() {
        ContentResolver resolver = getContentResolver();
        String[] projection = new String[]{BaseColumns._ID, ContactsContract.Contacts.DISPLAY_NAME};
        Cursor cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, projection, null, null, null);

        Log.d(TAG, "onCreate: " + cursor.getCount());
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(0);
                String displayName = cursor.getString(1);

                Log.d(TAG, "onCreate: Contact = {id=" + id + ", displayName=" + displayName + "}");
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void insertContact(String displayName, String number) {
        ContentResolver resolver = getContentResolver();
        ArrayList<ContentProviderOperation> contentProviderOperations = new ArrayList<>();

        //insert raw contact using RawContacts.CONTENT_URI
        contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());

        //insert display name using Data.CONTENT_URI
        contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
                .build());

        //insert mobile number using Data.CONTENT_URI
        contentProviderOperations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build());
        try {
            ContentProviderResult[] results = resolver.applyBatch(ContactsContract.AUTHORITY, contentProviderOperations);

            for (ContentProviderResult result : results) {
                Log.d(TAG, "insertContact: [" + result.uri + "]");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    private void deleteContact(String displayName) {
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null,
                ContactsContract.Contacts.DISPLAY_NAME + "=?", new String[]{displayName}, null);

        Log.d(TAG, "onCreate: " + cursor.getCount());
        if (cursor.moveToFirst()) {
            do {
                Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI,
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)));

                Log.d(TAG, "onCreate: Word = {uri=" + uri + ", name=" + cursor.getString(
                        cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)) + "}");
                int count = resolver.delete(uri, null, null);
                Log.d(TAG, "contact deleted: " + (count > 0));
            } while (cursor.moveToNext());
        }
        cursor.close();
    }
}
