package hu.blackbelt.judo.tatami.jsl.jsl2ui.etl.osgi;

import hu.blackbelt.judo.tatami.core.etl.EtlEngineRegistry;
import hu.blackbelt.judo.tatami.jsl.jsl2ui.Jsl2UiEtlEngine;
import hu.blackbelt.judo.tatami.jsl.jsl2ui.etl.Jsl2UiEtlEngineImpl;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * Registers the ETL {@link Jsl2UiEtlEngine} in the {@link EtlEngineRegistry} while this bundle is active. The script root is
 * the bundle entry the former Jsl2UiTransformationService used. See change: split-etl-artifacts.
 */
@Component(immediate = true, service = Jsl2UiEtlEngineComponent.class)
public class Jsl2UiEtlEngineComponent {

    private Jsl2UiEtlEngine engine;

    @Activate
    public void activate(BundleContext bundleContext) throws Exception {
        java.net.URI scriptRoot = bundleContext.getBundle()
                .getEntry("/tatami/jsl2ui/transformations/ui/jslToUi.etl")
                .toURI()
                .resolve(".");
        engine = new Jsl2UiEtlEngineImpl(scriptRoot);
        EtlEngineRegistry.register(Jsl2UiEtlEngine.class, engine);
    }

    @Deactivate
    public void deactivate() {
        EtlEngineRegistry.unregister(Jsl2UiEtlEngine.class, engine);
    }
}
