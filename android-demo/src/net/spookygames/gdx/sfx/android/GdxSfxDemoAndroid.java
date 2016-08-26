/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Spooky Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.spookygames.gdx.sfx.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;
import net.spookygames.gdx.sfx.demo.GdxSfxDemo;
import net.spookygames.gdx.sfx.demo.GdxSfxDemo.FileChooser;
import net.spookygames.gdx.sfx.demo.GdxSfxDemo.FileChooser.Callback;

public class GdxSfxDemoAndroid extends AndroidApplication {

	Callback chooseFileCallback = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useAccelerometer = false;
		config.useCompass = false;

		AndroidAudioDurationResolver.initialize(this.getPackageName());

		initialize(new GdxSfxDemo(new FileChooser() {
			@Override
			public void chooseFile(FileHandle directory, Callback callback) {

				chooseFileCallback = callback;

				Intent intent = new Intent();
				intent.setType("audio/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);

				try {
					startActivityForResult(Intent.createChooser(intent, "Choose audio file"), 1);
				} catch (android.content.ActivityNotFoundException ex) {
					// Potentially direct the user to the Market with a Dialog
					Toast.makeText(GdxSfxDemoAndroid.this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
				}

			}

		}), config);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		FileHandle file = null;

		if (resultCode == RESULT_CANCELED) {
			// action cancelled
		} else if (resultCode == RESULT_OK) {

			try {

				// Get the Uri of the selected file
				Uri uri = data.getData();

				File f = new File(uri.toString());
				if (!f.exists()) {
					f.createNewFile();

					// // Get the path
					String path = getPath(GdxSfxDemoAndroid.this, uri);

					File ff = new File(path);
					copyFile(ff, f);
				}

				file = new FileHandle(f);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace(System.out);
			}
		}

		if (chooseFileCallback != null)
			chooseFileCallback.onFileChosen(file);

	}

	private void copyFile(File sourceFile, File destFile) throws IOException {
		if (!sourceFile.exists()) {
			return;
		}

		FileInputStream sourceStream = null;
		FileInputStream destinationStream = null;

		FileChannel source = null;
		FileChannel destination = null;
		try {
			sourceStream = new FileInputStream(sourceFile);
			destinationStream = new FileInputStream(destFile);
			source = sourceStream.getChannel();
			destination = destinationStream.getChannel();
			if (destination != null && source != null) {
				destination.transferFrom(source, 0, source.size());
			}
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
			if (sourceStream != null) {
				sourceStream.close();
			}
			if (destinationStream != null) {
				destinationStream.close();
			}
		}

	}

	public static String getPath(Context context, Uri uri) throws URISyntaxException {
		String scheme = uri.getScheme();
		if ("content".equalsIgnoreCase(scheme)) {
			String[] projection = { MediaStore.Video.Media.DATA };
			Cursor cursor = null;

			try {
				cursor = context.getContentResolver().query(uri, projection, null, null, null);
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
				if (cursor.moveToFirst()) {
					return cursor.getString(column_index);
				}
			} catch (Exception e) {
				// Eat it
			}
		} else if ("file".equalsIgnoreCase(scheme)) {
			return uri.getPath();
		}

		return null;
	}

}
