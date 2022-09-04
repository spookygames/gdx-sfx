/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2022 Spooky Games
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
package games.spooky.gdx.sfx.demo;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Locale;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters.LoadedCallback;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.AbsoluteFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;
import games.spooky.gdx.sfx.FadeIn;
import games.spooky.gdx.sfx.FadeOut;
import games.spooky.gdx.sfx.SfxMusic;
import games.spooky.gdx.sfx.SfxMusicLoader;
import games.spooky.gdx.sfx.SfxMusicLoader.MusicParameters;
import games.spooky.gdx.sfx.SfxMusicPlaylist;

public class GdxSfxDemo implements ApplicationListener {

	SpriteBatch batch;
	
	AssetManager assetManager;

	SfxMusicPlaylist player;
	FadeIn fadein;
	FadeOut fadeout;

	Stage stage;
	Skin skin;

	Preferences prefs;
	
	Button playPauseButton;
	
	final NativeFileChooser fileChooser;
	
	public GdxSfxDemo(NativeFileChooser fileChooser) {
		super();
		this.fileChooser = fileChooser;
	}

	@Override
	public void create() {

		/* **************** */
		/*  Initialization  */
		/* **************** */
		
		prefs = Gdx.app.getPreferences("GdxSfxDemo");

		batch = new SpriteBatch();

		Camera camera = new OrthographicCamera();

		FileHandleResolver resolver = new AbsoluteFileHandleResolver();
		assetManager = new AssetManager(resolver);
		assetManager.setLoader(SfxMusic.class, new SfxMusicLoader(resolver));

		skin = new Skin(Gdx.files.internal("skin/uiskin.json"));

		player = new SfxMusicPlaylist();
		fadein = new FadeIn();
		fadeout = new FadeOut();

		/* ********** */
		/*  Playlist  */
		/* ********** */

		final VerticalGroup playlistGroup = new VerticalGroup();
		playlistGroup.fill().left();

		final Label invitationLabel = new Label("Add some music and let's have fun", skin, "subtitle");
		invitationLabel.setAlignment(Align.center);

		ScrollPane scrollablePlaylistTable = new ScrollPane(playlistGroup, skin);
		scrollablePlaylistTable.setScrollingDisabled(true, false);
		scrollablePlaylistTable.setOverscroll(false, false);

		/* ****** */
		/*  File  */
		/* ****** */

		Button fileFinder = new TextButton("Add music", skin, "round");
		fileFinder.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				
				NativeFileChooserConfiguration conf = new NativeFileChooserConfiguration();
				conf.directory = Gdx.files.absolute(prefs.getString("last",
						Gdx.files.isExternalStorageAvailable() ? 
								Gdx.files.getExternalStoragePath()
								: (Gdx.files.isLocalStorageAvailable() ?
										Gdx.files.getLocalStoragePath()
										: System.getProperty("user.home"))));
				conf.nameFilter = new FilenameFilter() {
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
				};
				conf.mimeFilter = "audio/*";
				conf.title = "Choose audio file";
				
				fileChooser.chooseFile(conf, new NativeFileChooserCallback() {
					@Override
					public void onFileChosen(FileHandle file) {
						if(file == null)
							return;
						
						prefs.putString("last", file.parent().file().getAbsolutePath());
						MusicParameters parameters = new MusicParameters();
						parameters.title = file.name();
						parameters.loadedCallback = new LoadedCallback() {
							@Override
							public void finishedLoading(AssetManager assetManager, String fileName, Class type) {
								final SfxMusic music = assetManager.get(fileName, SfxMusic.class);
								player.addMusic(music);
								final MusicWidget widget = new MusicWidget(skin, music);
								playlistGroup.addActor(widget);
								widget.removeButton.addListener(new ChangeListener() {
									@Override
									public void changed(ChangeEvent event, Actor actor) {
										if(player.removeMusic(music))
											widget.remove();
									}
								});
							}
						};

						assetManager.load(new AssetDescriptor<SfxMusic>(file, SfxMusic.class, parameters));
					}

					@Override
					public void onCancellation() {
					}

					@Override
					public void onError(Exception exception) {
					}
				});
			}
		});

		/* ********* */
		/*  Control  */
		/* ********* */

		playPauseButton = new Button(skin, "music");
		playPauseButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (playPauseButton.isChecked()) {
					if (player.isEmpty())
						playPauseButton.setChecked(false);
					player.play();
				} else {
					if (player.isPlaying())
						player.pause();
				}
			}
		});

		Button previousButton = new Button(skin, "left");
		previousButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				player.previous();
			}
		});

		Button nextButton = new Button(skin, "right");
		nextButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				player.next();
			}
		});

		final Button muteButton = new Button(skin, "sound");
		muteButton.setChecked(true);
		muteButton.addListener(new ChangeListener() {
			float formerVolume = player.getVolume();

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				float newVolume;
				if (muteButton.isChecked()) {
					// Un-mute
					newVolume = formerVolume;
				} else {
					// Mute
					formerVolume = player.getVolume();
					newVolume = 0f;
				}
				player.setVolume(newVolume);
			}
		});

		final Slider panSlider = new Slider(-1f, 1f, 0.02f, false, skin);
		panSlider.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				player.setPan(panSlider.getValue());
			}
		});
		panSlider.setValue(player.getPan());

		final Slider volumeSlider = new Slider(0f, 1f, 0.01f, false, skin);
		volumeSlider.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				muteButton.setChecked(true);
				player.setVolume(volumeSlider.getValue());
			}
		});
		volumeSlider.setValue(player.getVolume());

		final CheckBox repeatCheckBox = new CheckBox("Repeat", skin, "switch");
		repeatCheckBox.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				player.setLooping(repeatCheckBox.isChecked());
			}
		});
		repeatCheckBox.setChecked(player.isLooping());

		final Table fadeTable = new Table(skin);

		final Label fadeDurationLabel = new Label("0.0s", skin);
		final Slider fadeDurationSlider = new Slider(0.1f, 5f, 0.1f, false, skin);
		fadeDurationSlider.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				float value = fadeDurationSlider.getValue();
				fadein.setDuration(value);
				fadeout.setDuration(value);
				fadeDurationLabel.setText(String.format(Locale.ROOT, "%.1fs", value));
			}
		});
		fadeDurationSlider.setValue(2f);

		final SelectBox<String> fadeInterpolationSelectBox = new SelectBox<String>(skin);
		final ObjectMap<String, Interpolation> interpolations = new ObjectMap<String, Interpolation>();
		interpolations.put("Linear", Interpolation.linear);
		interpolations.put("Fade", Interpolation.fade);
		interpolations.put("Circle", Interpolation.circle);
		interpolations.put("Sine", Interpolation.sine);
		interpolations.put("Bounce", Interpolation.bounce);
		interpolations.put("Elastic", Interpolation.elastic);
		interpolations.put("Swing", Interpolation.swing);
		fadeInterpolationSelectBox.setItems(interpolations.keys().toArray());
		fadeInterpolationSelectBox.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				Interpolation interpolation = interpolations.get(fadeInterpolationSelectBox.getSelected());
				fadein.setInterpolation(interpolation);
				fadeout.setInterpolation(interpolation);
			}
		});

		fadeTable.defaults().left().pad(1f);
		fadeTable.row();
		fadeTable.add("Duration");
		fadeTable.add(fadeDurationSlider).expandX().fill();
		fadeTable.add(fadeDurationLabel).width(40);
		fadeTable.row();
		fadeTable.add("Interpolation");
		fadeTable.add(fadeInterpolationSelectBox).colspan(2).expandX().fill();

		final CheckBox fadeCheckBox = new CheckBox("Fade", skin, "switch");
		fadeCheckBox.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				boolean fade = fadeCheckBox.isChecked();
				if (fade) {
					player.addEffect(fadein);
					player.addEffect(fadeout);
				} else {
					player.removeEffect(fadein);
					player.removeEffect(fadeout);
				}
				fadeTable.setVisible(fade);
			}
		});
		fadeCheckBox.setChecked(true);
		fadeTable.setVisible(true);

		Table controlTable = new Table(skin);
		
		Table leftControlTable = new Table(skin);
		leftControlTable.defaults().padTop(4f);
		leftControlTable.row();
		leftControlTable.add(fileFinder).colspan(3);
		leftControlTable.row();
		leftControlTable.add(previousButton).right();
		leftControlTable.add(playPauseButton);
		leftControlTable.add(nextButton).left();
		leftControlTable.row();
		leftControlTable.add("Volume").padRight(10f).left();
		leftControlTable.add(volumeSlider);
		leftControlTable.add(muteButton);
		leftControlTable.row();
		leftControlTable.add("Pan").left();
		leftControlTable.add(panSlider).colspan(2).left();
		
		Table rightControlTable = new Table(skin);
		rightControlTable.defaults().padTop(12f).left();
		rightControlTable.row();
		rightControlTable.add(repeatCheckBox).colspan(2);
		rightControlTable.row();
		rightControlTable.add(fadeCheckBox).colspan(2);
		rightControlTable.row();
		rightControlTable.add(fadeTable).colspan(2);

		controlTable.row().growX();
		controlTable.add(leftControlTable);
		controlTable.add(rightControlTable);

		/* ******* */
		/*  Debug  */
		/* ******* */
		
		Label debugLabel = new Label("", skin) {
			@Override
			public void act(float delta) {
				setText(player.toString());
				super.act(delta);
			}
		};

		/* ************* */
		/*  Stage setup  */
		/* ************* */

		Table rootTable = new Table(skin);
		rootTable.setFillParent(true);
		rootTable.row();
		rootTable.add(invitationLabel).growX().padTop(20f);
		rootTable.row();
		rootTable.add(scrollablePlaylistTable).expand().fillX().top();
		rootTable.row();
		rootTable.add(controlTable).growX();
		rootTable.row();
		rootTable.add(debugLabel).growX().padTop(20f).padLeft(120f).padBottom(12f);
		
		stage = new Stage(new ScreenViewport(camera), batch);
		stage.addActor(rootTable);

		/* ******* */
		/*  Input  */
		/* ******* */

		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void render() {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		float delta = Gdx.graphics.getDeltaTime();

		Gdx.graphics.setTitle("gdx-sfx -- Music player demo -- " + player.getTitle());

		assetManager.update();
		if (player.update(delta))
			playPauseButton.setChecked(false);
		stage.act(delta);
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void pause() {
		if(player.isPlaying())
			player.pause();
	}

	@Override
	public void resume() {
		if(player.isPaused())
			player.play();
	}

	@Override
	public void dispose() {
		batch.dispose();
		assetManager.dispose();
		stage.dispose();
		skin.dispose();
	}

	private static class MusicWidget extends Table {

		SfxMusic music;
		Label playingLabel;
		Label titleLabel;
		ProgressBar timeProgress;
		Label timeLabel;
		Label volumeLevel;
		Label panLevel;
		TextButton removeButton;

		boolean wasPlaying;
		
		public MusicWidget(Skin skin, SfxMusic music) {
			super(skin);

			this.music = music;

			playingLabel = new Label(">", skin, "title");
			titleLabel = new Label(music.getTitle(), skin);
			titleLabel.setAlignment(Align.left);
			titleLabel.setEllipsis(true);
			timeProgress = new ProgressBar(0f, music.getDuration(), 0.1f, false, skin);
			timeLabel = new Label("", skin);
			timeLabel.setAlignment(Align.center);
			volumeLevel = new Label("vol: 0%", skin);
			panLevel = new Label("pan: 0.00", skin);
			removeButton = new TextButton("X", skin, "round");

			wasPlaying = true;

			row().pad(2f);
			add(playingLabel);
			add(titleLabel).expandX().fillX();
			stack(timeProgress, timeLabel).padLeft(20f).padRight(20f);
			add(volumeLevel).width(80f);
			add(panLevel).width(80f);
			add(removeButton);
		}

		@Override
		public void act(float delta) {
			if (music.isPlaying()) {
				if (!wasPlaying) {
					wasPlaying = true;
					playingLabel.setVisible(true);
					timeLabel.setVisible(true);
					volumeLevel.setVisible(true);
					panLevel.setVisible(true);
				}
				float position = music.getPosition();
				timeProgress.setValue(position);
				timeLabel.setText(String.format(Locale.ROOT, "%.0f/%.0f", position, music.getDuration()));
				volumeLevel.setText(String.format(Locale.ROOT, "vol: %.0f%%", music.getVolume() * 100f));
				panLevel.setText(String.format(Locale.ROOT, "pan: %.2f", music.getPan()));
			} else if (wasPlaying) {
				wasPlaying = false;
				playingLabel.setVisible(false);
				timeProgress.setValue(0f);
				timeLabel.setVisible(false);
				volumeLevel.setVisible(false);
				panLevel.setVisible(false);
			}

			super.act(delta);
		}

	}
	
}
