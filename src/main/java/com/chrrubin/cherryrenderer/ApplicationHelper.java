package com.chrrubin.cherryrenderer;

import com.chrrubin.cherryrenderer.upnp.states.RendererState;
import javafx.util.Duration;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;

import java.net.URI;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ApplicationHelper {
    private static RendererState rendererState = null;
    private static UnsignedIntegerFourBytes instanceID = null;
    private static URI uri = null;
    private static String metadata = null;
    private static Duration videoCurrentTime = null;
    private static Duration videoTotalTime = null;

    private static ReadWriteLock lock = new ReentrantReadWriteLock();
    private static Lock readLock = lock.readLock();
    private static Lock writeLock = lock.writeLock();

    private ApplicationHelper(){}

    public static RendererState getRendererState() {
        try {
            readLock.lock();
            return rendererState;
        }
        finally {
            readLock.unlock();
        }
    }

    public static void setRendererState(RendererState rendererState) {
        try{
            writeLock.lock();
            System.out.println("ApplicationHandler.rendererState set to " + rendererState.name());
            ApplicationHelper.rendererState = rendererState;
        }
        finally {
            writeLock.unlock();
        }
    }

    public static UnsignedIntegerFourBytes getInstanceID() {
        try {
            readLock.lock();
            return instanceID;
        }
        finally {
            readLock.unlock();
        }
    }

    public static void setInstanceID(UnsignedIntegerFourBytes instanceID) {
        try{
            writeLock.lock();
            ApplicationHelper.instanceID = instanceID;
        }
        finally {
            writeLock.unlock();
        }
    }

    public static URI getUri() {
        try{
            readLock.lock();
            return uri;
        }
        finally {
            readLock.unlock();
        }
    }

    public static void setUri(URI uri) {
        try{
            writeLock.lock();
            ApplicationHelper.uri = uri;
        }
        finally {
            writeLock.unlock();
        }
    }

    public static String getMetadata() {
        try{
            readLock.lock();
            return metadata;
        }
        finally {
            readLock.unlock();
        }
    }

    public static void setMetadata(String metadata) {
        try {
            writeLock.lock();
            ApplicationHelper.metadata = metadata;
        }
        finally {
            writeLock.unlock();
        }
    }

    public static Duration getVideoCurrentTime() {
        try{
            readLock.lock();
            return videoCurrentTime;
        }
        finally {
            readLock.unlock();
        }

    }

    public static void setVideoCurrentTime(Duration videoCurrentTime) {
        try{
            writeLock.lock();
            ApplicationHelper.videoCurrentTime = videoCurrentTime;
        }
        finally {
            writeLock.unlock();
        }
    }

    public static Duration getVideoTotalTime() {
        try{
            readLock.lock();
            return videoTotalTime;
        }
        finally {
            readLock.unlock();
        }
    }

    public static void setVideoTotalTime(Duration videoTotalTime) {
        try{
            writeLock.lock();
            ApplicationHelper.videoTotalTime = videoTotalTime;
        }
        finally {
            writeLock.unlock();
        }
    }

    public static String durationToString(Duration duration){
        int intSeconds = (int)Math.floor(duration.toSeconds());
        int hours = intSeconds / 60 / 60;
        if(hours > 0){
            intSeconds -= (hours * 60 * 60);
        }
        int minutes = intSeconds / 60;
        int seconds = intSeconds - (hours * 60 * 60) - (minutes * 60);

        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }
}
