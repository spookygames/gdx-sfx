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
package net.spookygames.gdx.sfx.demo;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Locale;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.files.FileHandle;

import net.spookygames.gdx.sfx.demo.GdxSfxDemo.FileChooser;
import net.spookygames.gdx.sfx.desktop.DesktopAudioDurationResolver;

public class GdxSfxDemoDesktop {
	public static void main(String[] args) throws Exception {
		DesktopAudioDurationResolver.initialize();
		new LwjglApplication(new GdxSfxDemo(new FileChooser() {
			@Override
			public void chooseFile(FileHandle directory, Callback callback) {
				FileDialog fileDialog = new FileDialog((Frame) null, "Choose audio file");
				fileDialog.setFilenameFilter(new FilenameFilter() {
					final String[] extensions = { "wav", "mp3", "ogg" };

					@Override
					public boolean accept(File dir, String name) {
						int i = name.lastIndexOf('.');
						if (i > 0 && i < name.length() - 1) {
							String desiredExtension = name.substring(i + 1).toLowerCase(Locale.ENGLISH);
							for (String extension : extensions) {
								if (desiredExtension.equals(extension)) {
									return true;
								}
							}
						}
						return false;
					}
				});
				if(directory != null)
					fileDialog.setDirectory(directory.path());
				fileDialog.setVisible(true);

				File[] files = fileDialog.getFiles();
				FileHandle result = null;

				if (files == null || files.length == 0) {

				} else {
					File f = files[0];
					if (f != null) {
						result = new FileHandle(f);
						if (!result.exists())
							result = null;
					}
				}

				if (callback != null)
					callback.onFileChosen(result);
			}
		}), "", 800, 600);
	}
}
