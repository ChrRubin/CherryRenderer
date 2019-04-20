package com.chrrubin.cherryrenderer.upnp;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes;
import org.fourthline.cling.support.model.Channel;
import org.fourthline.cling.support.renderingcontrol.AbstractAudioRenderingControl;
import org.fourthline.cling.support.renderingcontrol.RenderingControlException;
import org.fourthline.cling.support.renderingcontrol.lastchange.ChannelMute;
import org.fourthline.cling.support.renderingcontrol.lastchange.ChannelVolume;
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlVariable;

import java.util.logging.Logger;

public class RenderingControlService extends AbstractAudioRenderingControl {
    private UnsignedIntegerFourBytes instanceID = new UnsignedIntegerFourBytes(0);
    private final Logger LOGGER = Logger.getLogger(RenderingControlService.class.getName());
    private RenderingControlHandler renderingControlHandler = RenderingControlHandler.getInstance();

    public RenderingControlService(){
        renderingControlHandler.getRendererMuteEvent().addListener(this::onVideoMuteChange);
        renderingControlHandler.getRendererVolumeEvent().addListener(this::onVideoVolumeChange);
    }

    @Override
    public boolean getMute(UnsignedIntegerFourBytes instanceID, String channelName) throws RenderingControlException {
        return renderingControlHandler.isMute();
    }

    @Override
    public void setMute(UnsignedIntegerFourBytes instanceID, String channelName, boolean desiredMute) throws RenderingControlException {
        renderingControlHandler.setVideoMute(desiredMute);
    }

    @Override
    public UnsignedIntegerTwoBytes getVolume(UnsignedIntegerFourBytes instanceID, String channelName) throws RenderingControlException {
        return new UnsignedIntegerTwoBytes((long)renderingControlHandler.getVolume());
    }

    @Override
    public void setVolume(UnsignedIntegerFourBytes instanceID, String channelName, UnsignedIntegerTwoBytes desiredVolume) throws RenderingControlException {
        renderingControlHandler.setVideoVolume(desiredVolume.getValue().doubleValue());
    }


    @Override
    protected Channel[] getCurrentChannels() {
        return new Channel[]{Channel.Master};
    }

    @Override
    public UnsignedIntegerFourBytes[] getCurrentInstanceIds() {
        return new UnsignedIntegerFourBytes[]{instanceID};
    }

    private void onVideoVolumeChange(double volume){
        getLastChange().setEventedValue(
                instanceID,
                new RenderingControlVariable.Volume(new ChannelVolume(Channel.Master, (int)volume))
        );
    }

    private void onVideoMuteChange(boolean mute){
        getLastChange().setEventedValue(
                instanceID,
                new RenderingControlVariable.Mute(new ChannelMute(Channel.Master, mute))
        );
    }
}
