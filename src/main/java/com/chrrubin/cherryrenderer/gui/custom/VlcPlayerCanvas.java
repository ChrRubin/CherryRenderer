package com.chrrubin.cherryrenderer.gui.custom;

import com.chrrubin.cherryrenderer.MediaObject;
import javafx.animation.Animation;
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

public class VlcPlayerCanvas extends Canvas implements IPlayer{
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

    public VlcPlayerCanvas(){
        mediaPlayer.videoSurface().set(new JavaFxVideoSurface());

        timeline.setCycleCount(Timeline.INDEFINITE);
        double duration = 1000.0 / 60.0;
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(duration), event -> renderFrame()));

        mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter(){
            @Override
            public void buffering(MediaPlayer mediaPlayer, float newCache) {
                if(onBuffering != null){
                    Platform.runLater(onBuffering);
                }
            }

            @Override
            public void playing(MediaPlayer mediaPlayer) {
                if(timeline.getStatus() != Animation.Status.RUNNING){
                    timeline.playFromStart();
                }

                if(onPlaying != null) {
                    Platform.runLater(onPlaying);
                }
            }

            @Override
            public void paused(MediaPlayer mediaPlayer) {
                if(timeline.getStatus() == Animation.Status.RUNNING){
                    timeline.pause();
                }

                if(onPaused != null) {
                    Platform.runLater(onPaused);
                }
            }

            @Override
            public void stopped(MediaPlayer mediaPlayer) {
                timeline.stop();

                if(onStopped != null) {
                    Platform.runLater(onStopped);
                }
            }

            @Override
            public void finished(MediaPlayer mediaPlayer) {
                if(onFinished != null) {
                    Platform.runLater(onFinished);
                }
            }

            @Override
            public void error(MediaPlayer mediaPlayer) {
                if(onError != null) {
                    Platform.runLater(onError);
                }
            }

            @Override
            public void mediaPlayerReady(MediaPlayer mediaPlayer) {
                if(onMediaPlayerReady != null) {
                    Platform.runLater(onMediaPlayerReady);
                }
            }

            @Override
            public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
                if(timeline.getStatus() == Animation.Status.PAUSED){
                    timeline.play();
                    timeline.pause();
                }
                currentTime.set(Duration.millis(newTime));
            }

            @Override
            public void muted(MediaPlayer mediaPlayer, boolean muted) {
                isMute.set(muted);
            }

            @Override
            public void volumeChanged(MediaPlayer mediaPlayer, float volume) {
                VlcPlayerCanvas.this.volume.set(volume);
            }
        });

        mediaPlayer.events().addMediaEventListener(new MediaEventAdapter(){
            @Override
            public void mediaStateChanged(Media media, State newState) {
                state.set(newState);
            }
        });
    }

    /**
     * Releases all (including native) resources associated with mediaPlayer and mediaPlayerFactory
     * Best to run on application close.
     */
    public void releaseResources(){
        mediaPlayer.release();
        mediaPlayerFactory.release();
    }

    private class JavaFxVideoSurface extends CallbackVideoSurface {
        JavaFxVideoSurface() {
            super(new JavaFxBufferFormatCallback(), new JavaFxRenderCallback(), true, VideoSurfaceAdapters.getVideoSurfaceAdapter());
        }

    }

    private class JavaFxBufferFormatCallback implements BufferFormatCallback {
        @Override
        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
            videoImage = new WritableImage(sourceWidth, sourceHeight);
            pixelWriter = videoImage.getPixelWriter();

            return new RV32BufferFormat(sourceWidth, sourceHeight);
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
        GraphicsContext g = this.getGraphicsContext2D();

        double width = this.getWidth();
        double height = this.getHeight();

        g.setFill(new Color(0, 0, 0, 1));
        g.fillRect(0, 0, width, height);

        if (videoImage != null) {
            double imageWidth = videoImage.getWidth();
            double imageHeight = videoImage.getHeight();

            double sx = width / imageWidth;
            double sy = height / imageHeight;

            double sf = Math.min(sx, sy);

            double scaledW = imageWidth * sf;
            double scaledH = imageHeight * sf;

            Affine ax = g.getTransform();

            g.translate(
                    (width - scaledW) / 2,
                    (height - scaledH) / 2
            );

            if (sf != 1.0) {
                g.scale(sf, sf);
            }

            try {
                renderingSemaphore.acquire();
                g.drawImage(videoImage, 0, 0);
                renderingSemaphore.release();
            }
            catch (InterruptedException e) {
            }

            g.setTransform(ax);
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
        return mediaPlayer.audio().isMute();
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
        switch(mediaPlayer.status().state()){
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
