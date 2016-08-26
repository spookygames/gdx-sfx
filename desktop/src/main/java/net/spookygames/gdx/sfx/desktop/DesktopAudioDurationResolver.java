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
package net.spookygames.gdx.sfx.desktop;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.lwjgl.audio.Mp3;
import com.badlogic.gdx.backends.lwjgl.audio.Ogg;
import com.badlogic.gdx.backends.lwjgl.audio.OpenALSound;
import com.badlogic.gdx.backends.lwjgl.audio.Wav;
import com.badlogic.gdx.files.FileHandle;
import com.jcraft.jorbis.JOrbisException;
import com.jcraft.jorbis.VorbisFile;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Header;
import net.spookygames.gdx.sfx.MusicDurationResolver;
import net.spookygames.gdx.sfx.SfxMusicLoader;
import net.spookygames.gdx.sfx.SfxSoundLoader;
import net.spookygames.gdx.sfx.SoundDurationResolver;

public class DesktopAudioDurationResolver implements MusicDurationResolver, SoundDurationResolver {

	@Override
	public float resolveMusicDuration(Music music, FileHandle musicFile) {
		// TODO Change the world, make this happen
		// return ((OpenALMusic) music).duration();

		if (music instanceof Wav.Music) {
			try {
				return wavDuration(musicFile);
			} catch (UnsupportedAudioFileException e) {
				Gdx.app.error("gdx-sfx", "Unable to resolve duration of wav file " + musicFile.toString(), e);
			} catch (IOException e) {
				Gdx.app.error("gdx-sfx", "Unable to resolve duration of wav file " + musicFile.toString(), e);
			}
		} else if (music instanceof Mp3.Music) {
			try {
				return mp3Duration(musicFile);
			} catch (BitstreamException e) {
				Gdx.app.error("gdx-sfx", "Unable to resolve duration of mp3 file " + musicFile.toString(), e);
			}
		} else if (music instanceof Ogg.Music) {
			try {
				return oggDuration(musicFile);
			} catch (JOrbisException e) {
				Gdx.app.error("gdx-sfx", "Unable to resolve duration of ogg file " + musicFile.toString(), e);
			}
		}

		return -1f;
	}

	private float wavDuration(FileHandle musicFile) throws UnsupportedAudioFileException, IOException {
		AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(musicFile.file());
		AudioFormat format = audioInputStream.getFormat();
		long frames = audioInputStream.getFrameLength();
		float durationInSeconds = frames / format.getFrameRate();
		return durationInSeconds;
	}

	private float mp3Duration(FileHandle musicFile) throws BitstreamException {
		Bitstream bitstream = new Bitstream(musicFile.read());
		int length = (int) musicFile.length();
		int streamPos = bitstream.header_pos();
		Header header = bitstream.readFrame();
		if ((streamPos > 0) && (length != AudioSystem.NOT_SPECIFIED) && (streamPos < length))
			length -= streamPos;
		float totalMilliseconds = header.total_ms(length);
		float durationInSeconds = totalMilliseconds / 1000f;
		return durationInSeconds;
	}

	private float oggDuration(FileHandle musicFile) throws JOrbisException {
		String path = null;
		File file = musicFile.file();
		FileHandle tmpFile = null;
		
		if(file.exists()) {
			path = file.getAbsolutePath();
		} else {
			tmpFile = FileHandle.tempFile("gdx-sfx.ogg.");
			musicFile.copyTo(tmpFile);
			path = tmpFile.file().getAbsolutePath();
		}
		
		VorbisFile vorbis = new VorbisFile(path);
		float durationInSeconds =  vorbis.time_total(-1);
		
		if(tmpFile != null)
			tmpFile.delete();
		
		return durationInSeconds;
	}

	@Override
	public float resolveSoundDuration(Sound sound, FileHandle soundFile) {
		return ((OpenALSound) sound).duration();
	}

	public static void initialize() {
		DesktopAudioDurationResolver resolver = new DesktopAudioDurationResolver();
		SfxMusicLoader.setDurationResolver(resolver);
		SfxSoundLoader.setDurationResolver(resolver);
	}
}
