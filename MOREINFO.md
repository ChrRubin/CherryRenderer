# More Info

## Table of Contents
 - [Application Specific](#application-specific)
   - [What control point applications have been tested with CherryRenderer?](#what-control-point-applications-have-been-tested-with-cherryrenderer)
   - [What video formats are supported by CherryRenderer?](#what-video-formats-are-supported-by-cherryrenderer)
   - [Some of the buttons are turning into white boxes. How do I fix this?](#some-of-the-buttons-are-turning-into-white-boxes-how-do-i-fix-this)
 - [General](#general)
   - [What is a UPnP MediaRenderer and how does it work?](#what-is-a-upnp-mediarenderer-and-how-does-it-work)
   - [What about UPnP Media Servers and UPnP Clients? How are they different from a MediaRenderer?](#what-about-upnp-media-servers-and-upnp-clients-how-are-they-different-from-a-mediarenderer)
   - [DLNA? Chromecast? Airplay? Miracast? This is so confusing.](#dlna-chromecast-airplay-miracast-this-is-so-confusing)
   - [Why was CherryRenderer created?](#why-was-cherryrenderer-created)
   

## Application Specific
### What control point applications have been tested with CherryRenderer?
 - Tested and works:
   - [Localcast](https://www.localcast.app/)
   - [All Screen](https://play.google.com/store/apps/details?id=com.toxic.apps.chrome&hl=en)
   - [All Cast](https://www.allcast.io/)
   - [Web Video Caster](http://www.webvideocaster.com)
 - Tested and does not work:
   - [BubbleUPnP](https://play.google.com/store/apps/details?id=com.bubblesoft.android.bubbleupnp&hl=en)
     - It does not detect CheryRenderer for some reason. Still figuring out the reasons for this.

Note that all of the above are tested on Android only. Please do let me know if other applications work as well.

### What video formats are supported by CherryRenderer?
 - As per [javafx.scene.media docs](https://docs.oracle.com/javase/8/javafx/api/javafx/scene/media/package-summary.html), the following are the supported video formats:
 
 | File Extension   | Container | Video Encoding | Audio Encoding | MIME Type                                    |
 |----------------  |-----------|----------------|----------------|-----------                                   |
 | .fxm, .flv       | FXM, FLV  | VP6            | MP3            | video/x-javafx, video/x-flv                  |
 | .m3u8            | HLS       | H.264/AVC      | AAC            | application/vnd.apple.mpegurl, audio/mpegurl |
 | .mp4, .m4a, .m4v | MP4       | H.264/AVC      | AAC            | video/mp4, audio/x-m4a, video/x-m4v          |
 
 - On Windows 7 to 10, the required codecs *should* be included by your Windows installation by default. Older Windows versions or Windows Server editions may have to install third party codecs like MainConcept manually.
 - On Linux, you may need to install distro-dependant codecs. Examples of the codecs are ffmpeg/libavcodec/libavformat, x264 and Xvid.
 - If your system lacks the required codecs, or you try to play a non-supported video format, a MEDIA_UNSUPPORTED error will be thrown.

### Some of the buttons are turning into white boxes. How do I fix this?
 - This is a JavaFX rendering issue that occurs on certain systems.
 - Try disabling hardware acceleration via Menu - Preferences.
 - However, do note that disabling hardware acceleration may slow down UI rendering significantly in older systems. Use with care.


## General
### What is a UPnP MediaRenderer and how does it work?
 - A UPnP MediaRenderer is a device that is able to play (render) media content when connected with a UPnP Control Point application.
 - The Control Point sends the URL of the media resource to the MediaRenderer (also known as casting), which then loads and plays the media content.
 - All the loading and buffering of the content are handled by the MediaRenderer device.
 - The Control Point also acts as a remote control, allowing users to control the media playback on the MediaRenderer from the Control Point application.
 - If you have a "Smart TV", chances are it can be used as a MediaRenderer device.

### What about UPnP Media Servers and UPnP Clients? How are they different from a MediaRenderer?
 - A UPnP Media Server is like a media file server that shares its media content to UPnP Clients on the network.
 - A Client can connect to a Media Server, browse the files and select whichever media file to play on the Client's device.
 - A MediaRenderer has no browsing ability, and can only play media files that are specified by a Control Point. However, a Control Point *can* specify the MediaRenderer to play media files from a Media Server.

### DLNA? Chromecast? Airplay? Miracast? This is so confusing.
 - Yes, my child. Yes it is.
 - **DLNA** is a specification derived from UPnP that allows sharing of media content between DLNA-certified devices. While the UPnP protocol covers basically any type of network device, DLNA focuses on media devices, adding functions such as DRM protection that were absent in UPnP.
   - Although pure UPnP devices and pure DLNA devices are technically not compatible with each other, most consumer devices that support one would support the other, some even calling the two equivalent.
   - As such, most control point apps that support DLNA supports UPnP, and by proxy, CherryRenderer.
 - **Chromecast** (also known as Google Cast) and **Airplay** are proprietary protocols developed by Google and Apple respectively. In *very* oversimplification terms, the two serves the same purpose as DLNA and UPnP AV with the added "proprietary-ness".
   - No, CherryRenderer does not support Google Cast or Airplay.
 - **Miracast** is a standard that allows users to mirror their screens to a remote device wirelessly. While Miracast has its benefits, it is not the most ideal choice for sharing media content as it mirrors the device's entire screen.
   - No, CherryRenderer does not support Miracast.
   
### Why was CherryRenderer created?
 - I was introduced to UPnP casting from an Android Anime app called [AnYme](https://anyme.app). While casting to a TV or Raspberry Pi running Kodi was fine, I wanted a way to cast to PC.
 - A quick Google search showed almost nothing that just worked as a MediaRenderer. Popular video players like VLC only works as a UPnP client, while the only other solutions were to install huge bundled software such as Kodi on my PC, which was overkill for just wanting a MediaRenderer implementation.
 - A small group of the AnYme community also wanted a way to watch via the app on their PC, most of which had to resort to screen mirroring or emulators. 
 