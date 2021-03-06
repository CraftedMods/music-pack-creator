package craftedMods.audioPlayer.provider;

import java.io.IOException;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.log.FormatterLogger;
import org.osgi.service.log.LoggerFactory;

import craftedMods.audioPlayer.api.AudioPlayer;
import craftedMods.eventManager.api.EventManager;

@Component(scope = ServiceScope.PROTOTYPE, property = { AudioPlayer.SUPPORTED_FORMATS_PROPERTY_KEY + "=ogg",
		AudioPlayer.PLAYING_MODE_PROPERTY_KEY + "=" + AudioPlayer.PLAYING_MODE_CLIP }, service = AudioPlayer.class)
public class ClipAudioPlayerImpl extends AbstractAudioPlayer {

	@Reference(service=LoggerFactory.class)
	private FormatterLogger logger;

	@Reference
	private EventManager eventManager;

	@Activate
	public void onActivate(Configuration config, Map<String, Object> properties) {
		super.onActivate(logger, eventManager, config, properties);
	}

	@Modified
	public void onModify(Configuration config) {
		super.onModify(config);
	}

	@Deactivate
	public void onDeactivate() {
		super.onDeactivate();
	}

	@Override
	public boolean canSetPlayingPosition() {
		return true;
	}

	@Override
	protected DataLine constructDataLine(AudioFormat decodedAudioFormat) throws LineUnavailableException {
		return AudioSystem.getClip();
	}

	private volatile Clip clip;

	@Override
	public void setPlayingPositionMillis(long playingPosition) {
		super.setPlayingPositionMillis(playingPosition);
		clip.setMicrosecondPosition(playingPositionMillis * 1000);
	}

	@Override
	protected void openDataLine(DataLine dataLine, AudioInputStream decodedAudioInputStream) throws LineUnavailableException, IOException {
		clip = (Clip) dataLine;
		clip.open(decodedAudioInputStream);
		this.trackLengthMillis = clip.getMicrosecondLength() / 1000;
	}

	@Override
	protected boolean playbackLoop(DataLine dataLine, AudioInputStream decodedAudioInputStream) throws IOException {
		return this.trackLengthMillis > this.playingPositionMillis;
	}

	@Override
	protected boolean onTrackEnd() {
		this.pause();
		this.setPlayingPositionMillis(0);
		return true;
	}

}
