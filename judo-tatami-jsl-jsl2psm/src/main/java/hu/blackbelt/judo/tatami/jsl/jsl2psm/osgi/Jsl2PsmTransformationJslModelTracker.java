package hu.blackbelt.judo.tatami.jsl.jsl2psm.osgi;

import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import hu.blackbelt.judo.tatami.core.AbstractModelTracker;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component(immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
@Slf4j
public class Jsl2PsmTransformationJslModelTracker extends AbstractModelTracker<JslDslModel> {

    @Reference
    Jsl2PsmTransformationService jsl2PsmTransformationService;

    Map<String, ServiceRegistration<PsmModel>> registrations = new ConcurrentHashMap<>();
    Map<String, PsmModel> models = new HashMap<>();


    @Activate
    protected void activate(ComponentContext contextPar) {
        componentContext = contextPar;
        openTracker(contextPar.getBundleContext());
    }

    @Deactivate
    protected void deactivate() {
        closeTracker();
        registrations.forEach((k, v) -> { v.unregister(); });
    }

    private ComponentContext componentContext;

    @Override
    public void install(JslDslModel jslModel) {
        String key = jslModel.getName();
        PsmModel psmModel = null;
        if (models.containsKey(key)) {
            log.error("Model already loaded: " + jslModel.getName());
            return;
        }

        try {
            psmModel = jsl2PsmTransformationService.install(jslModel);
            log.info("Registering model: " + psmModel);
            ServiceRegistration<PsmModel> modelServiceRegistration =
                    componentContext.getBundleContext()
                            .registerService(PsmModel.class, psmModel, psmModel.toDictionary());
            models.put(key, psmModel);
            registrations.put(key, modelServiceRegistration);
        } catch (Exception e) {
            log.error("Could not register PSM Model: " + jslModel.getName(), e);
        }
    }

    @Override
    public void uninstall(JslDslModel jslModel) {
        String key = jslModel.getName();
        if (!registrations.containsKey(key)) {
            log.error("Model is not registered: " + jslModel.getName());
        } else {
            jsl2PsmTransformationService.uninstall(jslModel);
            registrations.get(key).unregister();
            registrations.remove(key);
            models.remove(key);
        }
    }

    @Override
    public Class<JslDslModel> getModelClass() {
        return JslDslModel.class;
    }
}
