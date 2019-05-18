package com.chrrubin.cherryrenderer.upnp;

import com.chrrubin.cherryrenderer.CherryUtil;
import com.chrrubin.cherryrenderer.upnp.states.RendererNoMediaPresent;
import com.chrrubin.cherryrenderer.upnp.states.RendererStateMachine;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.binding.LocalServiceBindingException;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.*;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.support.avtransport.impl.AVTransportService;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser;
import org.fourthline.cling.support.lastchange.LastChangeAwareServiceManager;
import org.fourthline.cling.support.lastchange.LastChangeParser;
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlLastChangeParser;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RendererService {
    private final Logger LOGGER = Logger.getLogger(RendererService.class.getName());

    private String friendlyName;

    private ExecutorService mainExecutor = Executors.newSingleThreadExecutor();
    private ScheduledExecutorService avTransportLastChangeExecutor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledExecutorService renderingControlLastChangeExecutor = Executors.newSingleThreadScheduledExecutor();

    public RendererService(String friendlyName){
        this.friendlyName = friendlyName;
    }

    private LocalDevice createDevice() throws ValidationException, LocalServiceBindingException, IOException {

        DeviceIdentity identity = new DeviceIdentity(UDN.uniqueSystemIdentifier("CherryRenderer"));

        DeviceType type = new UDADeviceType("MediaRenderer", 1);

        DeviceDetails details = new DeviceDetails(
                friendlyName,
                new ManufacturerDetails("ChrRubin", "http://chrrubin.com"),
                new ModelDetails(
                        "CherryRenderer",
                        "CherryRenderer - Standalone UPnP Media Renderer",
                        CherryUtil.VERSION,
                        "http://chrrubin.com"
                        )
        );

        Icon icon = new Icon("image/png", 64, 64, 32, "CherryRendererIcon.png",
                this.getClass().getClassLoader().getResourceAsStream("icons/cherry64.png"));

        LocalService<AVTransportService> avTransportService = new AnnotationLocalServiceBinder().read(AVTransportService.class);

        LastChangeParser avTransportLastChangeParser = new AVTransportLastChangeParser();

        avTransportService.setManager(
                new LastChangeAwareServiceManager<AVTransportService>(avTransportService, avTransportLastChangeParser) {
                    @Override
                    protected AVTransportService createServiceInstance() throws Exception {
                        return new AVTransportService(
                                RendererStateMachine.class,   // All states
                                RendererNoMediaPresent.class  // Initial state
                        );
                    }
                }
        );

        avTransportLastChangeExecutor.scheduleWithFixedDelay(() ->{
            LastChangeAwareServiceManager manager = (LastChangeAwareServiceManager)avTransportService.getManager();
            manager.fireLastChange();
        },0,500, TimeUnit.MILLISECONDS);


        LocalService<RenderingControlService> renderingControlService = new AnnotationLocalServiceBinder().read(RenderingControlService.class);

        LastChangeParser renderingControlLastChangeParser = new RenderingControlLastChangeParser();

        renderingControlService.setManager(
                new LastChangeAwareServiceManager<RenderingControlService>(renderingControlService, renderingControlLastChangeParser){
                    @Override
                    protected RenderingControlService createServiceInstance() throws Exception {
                        return new RenderingControlService();
                    }
                }
        );

        renderingControlLastChangeExecutor.scheduleWithFixedDelay(() ->{
            LastChangeAwareServiceManager manager = (LastChangeAwareServiceManager)renderingControlService.getManager();
            manager.fireLastChange();
        },0,500,TimeUnit.MILLISECONDS);

        return new LocalDevice(identity, type, details, icon, new LocalService[]{avTransportService, renderingControlService});
    }

    public void startService() {
        mainExecutor.submit(() -> {
            try{
                final UpnpService upnpService = new UpnpServiceImpl();

                Runtime.getRuntime().addShutdownHook(new Thread(() ->{
                    // TODO: The service doesn't shutdown properly? Sometimes it doesn't show signs of it in the output/log
                    LOGGER.info("Running shutdown hooks");
                    upnpService.shutdown();
                    avTransportLastChangeExecutor.shutdown();
                    renderingControlLastChangeExecutor.shutdown();
                }));

                upnpService.getRegistry().addDevice(createDevice());
            }
            catch (Exception e){
                LOGGER.log(Level.SEVERE, e.toString(), e);
                System.exit(1);
            }
        });

    }
}
