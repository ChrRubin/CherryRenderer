package com.chrrubin.cherryrenderer.gui.custom;

import com.chrrubin.cherryrenderer.MediaObject;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.util.Duration;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.media.Media;
import uk.co.caprica.vlcj.media.MediaEventAdapter;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.base.State;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapters;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;

import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

public class VlcPlayerCanvas extends Canvas implements IPlayer{
    private final Logger LOGGER = Logger.getLogger(VlcPlayerCanvas.class.getName());

    private PixelWriter pixelWriter = this.getGraphicsContext2D().getPixelWriter();
    private WritablePixelFormat<ByteBuffer> pixelFormat = PixelFormat.getByteBgraInstance();
    private MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory();
    private EmbeddedMediaPlayer mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
    private WritableImage videoImage;
    private final Semaphore renderingSemaphore = new Semaphore(1);
    private final Timeline timeline = new Timeline();

    private Runnable onBuffering;
    private Runnable onPlaying;
    private Runnable onPaused;
    private Runnable onStopped;
    private Runnable onFinished;
    private Runnable onError;
    private Runnable onMediaPlayerReady;
    private BooleanProperty isMute = new SimpleBooleanProperty(false);
    private DoubleProperty volume = new SimpleDoubleProperty();
    private ObjectProperty<Duration> currentTime = new SimpleObjectProperty<>();
    private ObjectProperty<State> state = new SimpleObjectProperty<>();
    private Integer videoWidth = null;
    private Integer videoHeight = null;

    public VlcPlayerCanvas(){
        mediaPlayer.videoSurface().set(new JavaFxVideoSurface());

        timeline.setCycleCount(Timeline.INDEFINITE);
        double duration = 1000.0 / 60.0;
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(duration), event -> renderFrame()));

        mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter(){
            @Override
            public void buffering(MediaPlayer mediaPlayer, float newCache) {
                LOGGER.finest("VLC media player buffering");
                if(onBuffering != null){
                    Platform.runLater(onBuffering);
                }
            }

            @Override
            public void playing(MediaPlayer mediaPlayer) {
                LOGGER.finest("VLC media player playing");

                if(onPlaying != null) {
                    Platform.runLater(onPlaying);
                }
            }

            @Override
            public void paused(MediaPlayer mediaPlayer) {
                LOGGER.finest("VLC media player paused");

                if(onPaused != null) {
                    Platform.runLater(onPaused);
                }
            }

            @Override
            public void stopped(MediaPlayer mediaPlayer) {
                LOGGER.finest("VLC media player stopped");
                timeline.stop();
                videoWidth = null;
                videoHeight = null;

                if(onStopped != null) {
                    Platform.runLater(onStopped);
                }
            }

            @Override
            public void finished(MediaPlayer mediaPlayer) {
                LOGGER.finest("VLC media player finished");
                if(onFinished != null) {
                    Platform.runLater(onFinished);
                }
            }

            @Override
            public void error(MediaPlayer mediaPlayer) {
                LOGGER.finest("VLC media player reached an error");
                if(onError != null) {
                    Platform.runLater(onError);
                }
            }

            @Override
            public void mediaPlayerReady(MediaPlayer mediaPlayer) {
                LOGGER.finest("VLC media player is ready");
                System.out.println("Video dimensions are " + mediaPlayer.video().videoDimension().toString());
                timeline.playFromStart();
                if(onMediaPlayerReady != null) {
                    Platform.runLater(onMediaPlayerReady);
                }
            }

            @Override
            public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
                currentTime.set(Duration.millis(newTime));
            }

            @Override
            public void muted(MediaPlayer mediaPlayer, boolean muted) {
                LOGGER.finest("VLC muted changed to " + muted);
                isMute.set(muted);
            }

            @Override
            public void volumeChanged(MediaPlayer mediaPlayer, float volume) {
                LOGGER.finest("VLC volume changed to " + volume);  // For some reason this gets triggered with 1.0 whenever toggling between pause and play
                VlcPlayerCanvas.this.volume.set(volume);
            }
        });

        mediaPlayer.events().addMediaEventListener(new MediaEventAdapter(){
            @Override
            public void mediaStateChanged(Media media, State newState) {
                LOGGER.finest("VLC state changed to " + newState.name());  // I like how this doesn't get triggered when player is buffering even though there's a BUFFERING state.
                state.set(newState);
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.fine("Releasing VLC player resources...");
            mediaPlayer.release();
            mediaPlayerFactory.release();
        }));
    }

    private class JavaFxVideoSurface extends CallbackVideoSurface {
        JavaFxVideoSurface() {
            super(new JavaFxBufferFormatCallback(), new JavaFxRenderCallback(), true, VideoSurfaceAdapters.getVideoSurfaceAdapter());
        }
    }

    private class JavaFxBufferFormatCallback implements BufferFormatCallback {
        @Override
        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
            if(videoWidth == null && videoHeight == null){
                videoWidth = sourceWidth;  // Workaround for libvlc 3.0 giving buffer dimensions instead of video resolution
                videoHeight = sourceHeight;
            }
            videoImage = new WritableImage(videoWidth, videoHeight);
            pixelWriter = videoImage.getPixelWriter();

            return new RV32BufferFormat(videoWidth, videoHeight);
        }
    }

    private class JavaFxRenderCallback implements RenderCallback {
        @Override
        public void display(MediaPlayer mediaPlayer, ByteBuffer[] nativeBuffers, BufferFormat bufferFormat) {
            try {
                renderingSemaphore.acquire();
                pixelWriter.setPixels(0, 0, bufferFormat.getWidth(), bufferFormat.getHeight(), pixelFormat, nativeBuffers[0], bufferFormat.getPitches()[0]);
                renderingSemaphore.release();
            }
            catch (InterruptedException e) {
            }
        }
    }

    private void renderFrame() {
        GraphicsContext graphicsContext = this.getGraphicsContext2D();

        double canvasWidth = this.getWidth();
        double canvasHeight = this.getHeight();

        graphicsContext.setFill(new Color(0, 0, 0, 1));
        graphicsContext.fillRect(0, 0, canvasWidth, canvasHeight);

        if (videoImage != null) {
            double imageWidth = videoImage.getWidth();  //FIXME: this is using buffer dimensions rather than video resolution
            double imageHeight = videoImage.getHeight();

            double sx = canvasWidth / imageWidth;
            double sy = canvasHeight / imageHeight;

            double sf = Math.min(sx, sy);

            double scaledWidth = imageWidth * sf;
            double scaledHeight = imageHeight * sf;

            Affine ax = graphicsContext.getTransform();

            graphicsContext.translate(
                    (canvasWidth - scaledWidth) / 2,
                    (canvasHeight - scaledHeight) / 2
            );

            if (sf != 1.0) {
                graphicsContext.scale(sf, sf);
            }

            try {
                renderingSemaphore.acquire();
                graphicsContext.drawImage(videoImage, 0, 0);
                renderingSemaphore.release();
            }
            catch (InterruptedException e) {
            }

            graphicsContext.setTransform(ax);
        }
    }

    @Override
    public void playNewMedia(MediaObject mediaObject){
        mediaPlayer.media().play(mediaObject.getUriString());
    }

    @Override
    public void play(){
        mediaPlayer.controls().play();
    }

    @Override
    public void pause(){
        mediaPlayer.controls().pause();
    }

    @Override
    public void stop(){
        mediaPlayer.controls().stop();
    }

    @Override
    public void seek(Duration target){
        mediaPlayer.controls().setTime((long)target.toMillis());
    }

    @Override
    public boolean isMute(){
        return isMute.get();
    }

    @Override
    public void setMute(boolean mute){
        mediaPlayer.audio().setMute(mute);
    }

    @Override
    public double getVolume(){
        return mediaPlayer.audio().volume();
    }

    @Override
    public void setVolume(double volume){
        mediaPlayer.audio().setVolume((int)volume);
    }

    @Override
    public Duration getCurrentTime(){
        return Duration.millis(mediaPlayer.status().time());
    }

    @Override
    public Duration getTotalTime(){
        return Duration.millis(mediaPlayer.status().length());
    }

    @Override
    public PlayerStatus getStatus(){
        switch(state.get()){
            case OPENING:
                return PlayerStatus.OPENING;
            case STOPPED:
                return PlayerStatus.STOPPED;
            case PLAYING:
                return PlayerStatus.PLAYING;
            case PAUSED:
                return PlayerStatus.PAUSED;
            case ERROR:
                return PlayerStatus.ERROR;
            case BUFFERING:
                return PlayerStatus.BUFFERING;
            default:
                return PlayerStatus.UNKNOWN;
        }
    }

    @Override
    public void setOnBuffering(Runnable onBuffering) {
        this.onBuffering = onBuffering;
    }

    @Override
    public void setOnPlaying(Runnable onPlaying) {
        this.onPlaying = onPlaying;
    }

    @Override
    public void setOnPaused(Runnable onPaused) {
        this.onPaused = onPaused;
    }

    @Override
    public void setOnStopped(Runnable onStopped) {
        this.onStopped = onStopped;
    }

    @Override
    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    @Override
    public void setOnError(Runnable onError) {
        this.onError = onError;
    }

    @Override
    public void setOnReady(Runnable onMediaPlayerReady) {
        this.onMediaPlayerReady = onMediaPlayerReady;
    }

    @Override
    public void disposePlayer() {
    }

    @Override
    public BooleanProperty muteProperty() {
        return isMute;
    }

    @Override
    public DoubleProperty volumeProperty() {
        return volume;
    }

    @Override
    public ReadOnlyObjectProperty<Duration> currentTimeProperty() {
        return currentTime;
    }

    @Override
    public Node getNode() {
        return this;
    }

    // TODO: Figure out if there's a way to get VLC's error messages
    @Override
    public Throwable getError() {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return "Unknown VLC player error";
    }
}
