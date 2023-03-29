package hu.blackbelt.judo.tatami.jsl.jsl2ui.osgi;

/*-
 * #%L
 * JUDO Tatami JSL parent
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import com.google.common.collect.Maps;
import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.epsilon.runtime.execution.impl.*;
import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.ui.runtime.UiModel;
import hu.blackbelt.judo.tatami.core.TransformationTrace;
import hu.blackbelt.judo.tatami.jsl.jsl2ui.Jsl2Ui;
import hu.blackbelt.judo.tatami.jsl.jsl2ui.Jsl2UiTransformationTrace;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.URI;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import java.util.Hashtable;
import java.util.Map;

import static hu.blackbelt.judo.tatami.jsl.jsl2ui.Jsl2Ui.executeJsl2UiTransformation;


@Component(immediate = true, service = Jsl2UiTransformationService.class)
@Slf4j
public class Jsl2UiTransformationService {

    Map<JslDslModel, ServiceRegistration<TransformationTrace>> jsl2UiTransformationTraceRegistration = Maps.newHashMap();

    BundleContext bundleContext;

    @Activate
    public void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public UiModel install(JslDslModel jslModel) throws Exception {
        UiModel uiModel = UiModel.buildUiModel()
                .uri(URI.createURI("ui:" + jslModel.getName() + ".model"))
                .build();

        java.net.URI scriptUri =
                bundleContext.getBundle()
                        .getEntry("/tatami/jsl2ui/transformations/ui/jslToUi.etl")
                        .toURI()
                        .resolve(".");
        try (Log bufferedLog = new BufferedSlf4jLogger(log)) {
            Jsl2UiTransformationTrace transformationTrace = executeJsl2UiTransformation(Jsl2Ui.Jsl2UiParameter.jsl2UiParameter()
                .jslModel(jslModel)
                .uiModel(uiModel)
                .log(bufferedLog)
                .scriptUri(scriptUri));

            jsl2UiTransformationTraceRegistration.put(jslModel,
                    bundleContext.registerService(TransformationTrace.class, transformationTrace, new Hashtable<>()));
        }
        return uiModel;
    }


    public void uninstall(JslDslModel jslModel) {
        if (jsl2UiTransformationTraceRegistration.containsKey(jslModel)) {
            jsl2UiTransformationTraceRegistration.get(jslModel).unregister();
        } else {
            log.error("UI model is not installed: " + jslModel.toString());
        }
    }
}
