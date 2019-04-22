# CherryRenderer

A standalone UPnP MediaRenderer video player for Windows, Linux and (probably[*](#more-info)) macOS.

Cast videos from your Android/iOS phone etc to your PC as if it was a Smart TV!

![screenshot](https://cdn.discordapp.com/attachments/480408561290182667/568332775543799823/cherryrendererwhite.png)
*(Screenshot was taken running [Web Video Caster](http://www.webvideocaster.com) on Android and CherryRenderer on Arch Linux + XFCE)*

CherryRenderer is built with JavaFX and [Cling](https://github.com/4thline/cling) (yes, I know it is no longer being maintained).

## Instructions
 1. Make sure Java 8 is installed.
 2. [Download](https://github.com/ChrRubin/CherryRenderer/releases) and run CherryRenderer_*[version]*.jar.
 3. Open the control point application on your remote device.
 4. Connect to the CherryRenderer device.
 5. Start casting!

*&ast; Depending on your operating system, you may need to install third party codecs to play videos. View more info about this [here](MOREINFO.md#what-video-formats-are-supported-by-cherryrenderer).*

## Features
 - Basic UPnP MediaRenderer support
   - Cast videos to CherryRenderer via a control point application
   - Control video playback such as Play, Pause, Stop, Rewind, Fast Forward from a control point application
 - Change CherryRenderer's friendly name (announced name) via Menu - Preferences
 - Keyboard support:
 
 | Function          | Key   |
 |----------         |:-----:|
 | Play/Pause        | Space |
 | Toggle fullscreen | F     |
 | Stop              | S     |
 | Rewind            | Left  |
 | Fast Forward      | Right |
 | Volume Up         | Up    |
 | Volume Down       | Down  |
 | Toggle Mute       | M     |
 
 - **&ast;NEW&ast;** Dark theme!
   - Switch between the default white theme and dark theme via Menu - Preferences
 - **&ast;NEW&ast;** Volume control support
   - Control the volume of CherryRenderer from control point applications that support this feature

## Known bugs
 - There is currently no buffer handling, as in there are no indications of when a video is in the middle of buffering.
   - AFAIK this is a JavaFX limitation. There is MediaPlayer.Status.STALLED, but it doesn't seem to be called except in obscure situations that I still don't understand.
 - Certain control point applications that provide "queuing" functions may try to cast the previously played video in its history right before casting the new video, causing 2 videos to play at the same time.
   - While I try to minimize this from happening by stopping previous videos before loading a new video, on some edge cases this may still occur.
 - Certain video providers will expire their video links after a period of time. If you try to cast expired video links (such as queuing it and playing it after the previous one finished), a MEDIA_UNAVAILABLE or MEDIA_INACCESSIBLE error will be thrown.

## More Info
For more info such as what control point apps have been tested and what video formats CherryRenderer supports, click [here](MOREINFO.md).

*&ast; I do not own a macOS device, nor do my alpha testers, but technically as long as Java 8 is installed it should run fine.*

## Credits
 - [Cling](https://github.com/4thline/cling)
 - [easy-events](https://github.com/Fylipp/easy-events)
 - Special thanks to my alpha testers for testing this program when it was extremely buggy.

## License
CherryRenderer is released under [GNU GPLv3](https://www.gnu.org/licenses/gpl-3.0.en.html). The full license can be found in the [COPYING](COPYING) file in the repository's root directory.