package hu.blackbelt.judo.tatami.jsl.jsl2psm.osgi;

import com.google.common.collect.Maps;
import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.*;
import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import hu.blackbelt.judo.tatami.core.TransformationTrace;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2Psm;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2PsmTransformationTrace;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.URI;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import java.util.Hashtable;
import java.util.Map;

import static hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2Psm.executeJsl2PsmTransformation;

@Component(immediate = true, service = Jsl2PsmTransformationService.class)
@Slf4j
public class Jsl2PsmTransformationService {

    Map<JslDslModel, ServiceRegistration<TransformationTrace>> jsl2PsmTransformationTraceRegistration = Maps.newHashMap();

    BundleContext bundleContext;

    @Activate
    public void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public PsmModel install(JslDslModel jslModel) throws Exception {
        PsmModel psmModel = PsmModel.buildPsmModel()
                .name(jslModel.getName())
                .version(jslModel.getVersion())
                .uri(URI.createURI("psm:" + jslModel.getName() + ".model"))
                .checksum(jslModel.getChecksum())
                .tags(jslModel.getTags())
                .build();

        java.net.URI scriptUri =
                bundleContext.getBundle()
                        .getEntry("/tatami/jsl2psm/transformations/psm/jslToPsm.etl")
                        .toURI()
                        .resolve(".");
        try (Log bufferedLog = new BufferedSlf4jLogger(log)) {
            Jsl2PsmTransformationTrace transformationTrace = executeJsl2PsmTransformation(Jsl2Psm.Jsl2PsmParameter.jsl2PsmParameter()
                .jslModel(jslModel)
                .psmModel(psmModel)
                .log(bufferedLog)
                .scriptUri(scriptUri));

            jsl2PsmTransformationTraceRegistration.put(jslModel,
                    bundleContext.registerService(TransformationTrace.class, transformationTrace, new Hashtable<>()));
        }
        return psmModel;
    }


    public void uninstall(JslDslModel jslModel) {
        if (jsl2PsmTransformationTraceRegistration.containsKey(jslModel)) {
            jsl2PsmTransformationTraceRegistration.get(jslModel).unregister();
        } else {
            log.error("PSM model is not installed: " + jslModel.toString());
        }
    }

}
