package craftedMods.audioPlayer.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.FloatControl.Type;
import javax.sound.sampled.LineUnavailableException;

import org.osgi.service.log.Logger;

import craftedMods.audioPlayer.api.AudioPlayer;
import craftedMods.audioPlayer.api.PlayableTrack;
import craftedMods.eventManager.api.EventDispatchPolicy;
import craftedMods.eventManager.api.EventManager;
import craftedMods.eventManager.api.WriteableEventProperties;
import craftedMods.eventManager.base.DefaultWriteableEventProperties;
import craftedMods.utils.Utils;

public abstract class AbstractAudioPlayer implements AudioPlayer {

	public @interface Configuration {
		long shutdownTimeout() default 1000l;
	}
	
	private String playingMode;

	private Logger logger;
	private EventManager eventManager;

	private ExecutorService audioPlayerThread;

	private long shutdownTimeout;

	private volatile PlayableTrack currentTrack;

	protected volatile boolean isPaused = false;

	protected volatile long trackLengthMillis = UNDEFINED;
	protected volatile long playingPositionMillis = 0l;

	protected volatile int volume = 50;
	protected volatile float maximumVolume;
	protected volatile float minimumVolume;

	protected volatile EnumState currentState = EnumState.INIT;

	private enum EnumState {
		INIT, PLAYING, ERROR;
	}

	protected Map<PlayableTrack, Long> trackDurationsCache;

	protected void onActivate(Logger logger, EventManager eventManager, Configuration config, Map<String, Object> properties) {
		this.playingMode = (String) properties.get(AudioPlayer.PLAYING_MODE_PROPERTY_KEY);
		
		this.logger = logger;
		this.eventManager = eventManager;

		this.shutdownTimeout = config.shutdownTimeout();
		this.audioPlayerThread = Executors.newSingleThreadExecutor();

		trackDurationsCache = new HashMap<>();
	}

	protected void onModify(Configuration config) {
		this.shutdownTimeout = config.shutdownTimeout();
	}

	protected void onDeactivate() {
		this.stop();
		trackDurationsCache.clear();
		this.audioPlayerThread.shutdown();
		try {
			this.audioPlayerThread.awaitTermination(this.shutdownTimeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			logger.error("Couldn't shutdown the audio player: ", e);
		}
	}

	public static float getLinearGain(float gain) {
		return (float) Math.pow(10.0f, gain / 20.0f);
	}

	public static float getGain(float linearGain) {
		return (float) (Math.log10(linearGain) * 20.0f);
	}

	@Override
	public String getCurrentTrack() {
		return this.currentTrack.getName();
	}

	@Override
	public boolean play(InputStream in, String name) {
		return this.play(new UncacheablePlayableTrack(name, () -> in));
	}

	@Override
	public boolean play(PlayableTrack track) {
		if (this.currentTrack == null) {
			this.isPaused = false;
			this.currentTrack = track;
			this.trackLengthMillis = UNDEFINED;
			this.playingPositionMillis = 0;
			this.currentState = EnumState.INIT;

			this.audioPlayerThread.submit(() -> this.audioPlayerThread(track));

			while (this.currentState == EnumState.INIT) {
			}

			return this.currentState != EnumState.ERROR;
		}
		return false;
	}

	private void audioPlayerThread(PlayableTrack track) {
		try (InputStream in = track.openInputStream(this.playingMode);
				AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(in)) {
			AudioFormat baseAudioFormat = audioInputStream.getFormat();
			AudioFileFormat baseAudioFileFormat = AudioSystem.getAudioFileFormat(in);
			if (baseAudioFileFormat.properties().containsKey("duration")) {
				this.trackLengthMillis = (long) baseAudioFileFormat.properties().get("duration");
			} else if (!(track instanceof UncacheablePlayableTrack) && this.trackDurationsCache.containsKey(track)) {
				this.trackLengthMillis = this.trackDurationsCache.get(track);
			}
			AudioFormat decodedAudioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
					baseAudioFormat.getSampleRate(), 16, baseAudioFormat.getChannels(),
					baseAudioFormat.getChannels() * 2, baseAudioFormat.getSampleRate(), false);
			try (AudioInputStream decodedAudioInputStream = AudioSystem.getAudioInputStream(decodedAudioFormat,
					audioInputStream); DataLine dataLine = this.constructDataLine(decodedAudioFormat)) {
				this.openDataLine(dataLine, decodedAudioInputStream);

				dataLine.start();

				FloatControl volume = (FloatControl) dataLine.getControl(Type.MASTER_GAIN);
				this.maximumVolume = AbstractAudioPlayer.getLinearGain(volume.getMaximum());
				this.minimumVolume = AbstractAudioPlayer.getLinearGain(volume.getMinimum());

				this.currentState = EnumState.PLAYING;
				
				boolean replay = true;

				while (replay) {
					while (this.currentTrack != null) {
						if (!this.isPaused) {
							if (!dataLine.isActive())
								dataLine.start();

							dataLine.start();

							volume.setValue(getGain((this.maximumVolume - this.minimumVolume) * (this.volume / 100.0f)
									+ this.minimumVolume));
							this.playingPositionMillis = dataLine.getMicrosecondPosition() / 1000;
							if (!this.playbackLoop(dataLine, decodedAudioInputStream)) {
								if (!(track instanceof UncacheablePlayableTrack)) {
									this.trackDurationsCache.put(track, this.playingPositionMillis);
									break;
								}
							}
						} else {
							if (dataLine.isActive())
								dataLine.stop();
						}
					}
					replay = isTrackActive() ? this.onTrackEnd() : false;
				}
			}
		} catch (Exception e) {
			this.currentState = EnumState.ERROR;

			String trackName = currentTrack != null ? currentTrack.getName() : UNDEFINED_TRACK_NAME;

			logger.error("Couldn't play the track \"%s\"", trackName, e);

			WriteableEventProperties properties = new DefaultWriteableEventProperties();
			properties.put(AudioPlayer.PLAY_TRACK_ERROR_EVENT_NAME, trackName);
			properties.put(AudioPlayer.PLAY_TRACK_ERROR_EVENT_EXCEPTION, e);
			eventManager.dispatchEvent(AudioPlayer.PLAY_TRACK_ERROR_EVENT, properties,
					EventDispatchPolicy.ASYNCHRONOUS);

		} finally {
			this.isPaused = false;
			this.currentTrack = null;
			this.playingPositionMillis = 0;
			this.trackLengthMillis = UNDEFINED;
		}
	}

	protected abstract DataLine constructDataLine(AudioFormat decodedAudioFormat) throws LineUnavailableException;

	protected abstract void openDataLine(DataLine dataLine, AudioInputStream decodedAudioInputStream)
			throws LineUnavailableException, IOException;

	protected abstract boolean playbackLoop(DataLine dataLine, AudioInputStream decodedAudioInputStream)
			throws IOException;

	/*
	 * Invoked if the end of the track is reached.
	 */
	protected abstract boolean onTrackEnd();

	public void pause() {
		this.isPaused = true;
	}

	public void resume() {
		this.isPaused = false;
	}

	public void stop() {
		this.pause();
		currentTrack = null;
	}

	public long getPlayingPositionMillis() {
		return this.playingPositionMillis;
	}

	@Override
	public boolean canSetPlayingPosition() {
		return trackLengthMillis != AudioPlayer.UNDEFINED;
	}

	@Override
	public void setPlayingPositionMillis(long playingPosition) {
		if (!canSetPlayingPosition())
			throw new UnsupportedOperationException("Cannot set the playing position");
		if (!isTrackActive())
			throw new IllegalStateException("No track is active");
		this.playingPositionMillis = playingPosition;
	}

	public long getTrackLengthMillis() {
		return this.trackLengthMillis;
	}

	public int getVolume() {
		return this.volume;
	}

	public void setVolume(int volume) {
		this.volume = Utils.clamp(100, 0, volume);
	}

	public boolean isTrackActive() {
		return currentTrack != null;
	}

	public boolean isPaused() {
		return this.isPaused;
	}

}
