package craftedMods.audioPlayer.provider;

import java.io.IOException;
import java.io.InputStream;
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
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.log.LogService;
import org.tritonus.share.sampled.file.TAudioFileFormat;

import craftedMods.audioPlayer.api.AudioPlayer;
import craftedMods.eventManager.api.EventDispatchPolicy;
import craftedMods.eventManager.api.EventManager;
import craftedMods.eventManager.api.WriteableEventProperties;
import craftedMods.eventManager.base.DefaultWriteableEventProperties;
import craftedMods.utils.Utils;

@Component(scope = ServiceScope.PROTOTYPE, property = AudioPlayer.SUPPORTED_FORMATS_PROPERTY_KEY + "=ogg")
public class AudioPlayerImpl implements AudioPlayer {

	public @interface Configuration {
		long shutdownTimeout() default 1000l;
	}

	@Reference
	private LogService logger;

	@Reference
	private EventManager eventManager;

	private ExecutorService audioPlayerThread;

	private volatile SourceDataLine sourceDataLine = null;
	private volatile boolean isPaused = false;
	private long maxPlayingTime = 0l;
	private volatile long currentPlayingTime = 0l;

	private volatile int volume = 50;
	private float maximumVolume;
	private float minimumVolume;

	private volatile int playTrackFlag = 0;

	private long shutdownTimeout;

	private volatile String currentTrack;

	@Activate
	public void onActivate(Configuration config) throws UnsupportedAudioFileException, IOException {
		this.shutdownTimeout = config.shutdownTimeout();
		this.audioPlayerThread = Executors.newSingleThreadExecutor();
	}

	@Deactivate
	public void onModify(Configuration config) {
		this.shutdownTimeout = config.shutdownTimeout();
	}

	@Deactivate
	public void onDeactivate() {
		this.stop();
		this.audioPlayerThread.shutdown();
		try {
			this.audioPlayerThread.awaitTermination(this.shutdownTimeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			logger.log(LogService.LOG_ERROR, "Couldn't shutdown the audio player: ", e);
		}
	}

	@Override
	public boolean play(InputStream trackInputStream, String name) {
		if (this.sourceDataLine == null) {
			this.isPaused = false;
			this.maxPlayingTime = 0;
			this.currentPlayingTime = 0;
			this.playTrackFlag = 0;
			this.currentTrack = name;
			this.audioPlayerThread.execute(() -> {
				try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(trackInputStream)) {
					if (audioInputStream != null) {
						AudioFormat baseAudioFormat = audioInputStream.getFormat();
						try {
							AudioFileFormat baseAudioFileFormat = AudioSystem.getAudioFileFormat(trackInputStream);
							if (baseAudioFileFormat instanceof TAudioFileFormat)
								this.maxPlayingTime = (long) ((TAudioFileFormat) baseAudioFileFormat).properties()
										.get("duration");
						} catch (Exception e) {
							this.maxPlayingTime = -100000;
							logger.log(LogService.LOG_ERROR,
									String.format("Couldn't get the duration of the track \"%s\"", name), e);
						}
						AudioFormat decodedAudioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
								baseAudioFormat.getSampleRate(), 16, baseAudioFormat.getChannels(),
								baseAudioFormat.getChannels() * 2, baseAudioFormat.getSampleRate(), false);
						try (AudioInputStream decodedAudioInputStream = AudioSystem
								.getAudioInputStream(decodedAudioFormat, audioInputStream);
								SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem
										.getLine(new DataLine.Info(SourceDataLine.class, decodedAudioFormat))) {
							if (sourceDataLine != null) {
								this.sourceDataLine = sourceDataLine;
								byte[] dataBuffer = new byte[4096];
								sourceDataLine.open(decodedAudioFormat);
								sourceDataLine.start();
								int bytesRead = 0;
								FloatControl volume = (FloatControl) sourceDataLine.getControl(Type.MASTER_GAIN);
								this.maximumVolume = this.getLinearGain(volume.getMaximum());
								this.minimumVolume = this.getLinearGain(volume.getMinimum());
								this.volume = (int) (this.getLinearGain(volume.getValue())
										/ (this.maximumVolume - this.minimumVolume) * 100.0f);
								this.playTrackFlag = 1;
								while (bytesRead != -1) {
									if (this.sourceDataLine == null)
										break;
									if (!this.isPaused) {
										volume.setValue(this.getGain(
												(this.maximumVolume - this.minimumVolume) * (this.volume / 100.0f)
														+ this.minimumVolume));
										bytesRead = decodedAudioInputStream.read(dataBuffer, 0, dataBuffer.length);
										if (bytesRead != -1) {
											sourceDataLine.write(dataBuffer, 0, bytesRead);
										}
										this.currentPlayingTime = sourceDataLine.getMicrosecondPosition();
									}

								}
								this.currentPlayingTime = this.maxPlayingTime;
								sourceDataLine.drain();
								sourceDataLine.stop();
								sourceDataLine.close();
								decodedAudioInputStream.close();
								this.stop();
							}
						}
					}
				} catch (Exception e) {
					this.playTrackFlag = 2;
					this.isPaused = false;
					this.sourceDataLine = null;
					this.maxPlayingTime = 0;
					this.currentPlayingTime = 0;
					this.currentTrack = null;
					logger.log(LogService.LOG_ERROR, String.format("Couldn't play the track \"%s\"", name), e);
					WriteableEventProperties properties = new DefaultWriteableEventProperties();
					properties.put(AudioPlayer.PLAY_TRACK_ERROR_EVENT_NAME, name);
					properties.put(AudioPlayer.PLAY_TRACK_ERROR_EVENT_EXCEPTION, e);
					eventManager.dispatchEvent(AudioPlayer.PLAY_TRACK_ERROR_EVENT, properties,
							EventDispatchPolicy.ASYNCHRONOUS);
				}
			});
			while (this.playTrackFlag == 0) {
			}
			return this.playTrackFlag != 2;
		}
		return false;
	}

	@Override
	public String getCurrentTrack() {
		return this.currentTrack;
	}

	private float getLinearGain(float gain) {
		return (float) Math.pow(10.0f, gain / 20.0f);
	}

	private float getGain(float linearGain) {
		return (float) (Math.log10(linearGain) * 20.0f);
	}

	public void pause() {
		if (this.sourceDataLine != null) {
			this.isPaused = true;
			this.sourceDataLine.stop();
		}
	}

	public void resume() {
		if (this.sourceDataLine != null) {
			this.isPaused = false;
			this.sourceDataLine.start();
		}
	}

	public void stop() {
		this.pause();
		this.sourceDataLine = null;
		this.currentPlayingTime = 0;
		this.maxPlayingTime = 0;
		this.currentTrack = null;
	}

	public long getCurrentPlayingTime() {
		return this.currentPlayingTime / 1000;
	}

	public long getMaxPlayingTime() {
		return this.maxPlayingTime / 1000;
	}

	public int getVolume() {
		return this.volume;
	}

	public void setVolume(int volume) {
		this.volume = Utils.clamp(100, 0, volume);
	}

	public boolean isTrackActive() {
		return this.sourceDataLine != null;
	}

	public boolean isPaused() {
		return this.isPaused;
	}
}
