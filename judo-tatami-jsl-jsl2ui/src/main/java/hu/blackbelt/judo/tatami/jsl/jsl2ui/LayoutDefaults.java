package hu.blackbelt.judo.tatami.jsl.jsl2ui;

/*-
 * #%L
 * Judo :: Tatami :: JSL :: Jsl2Ui
 * %%
 * Copyright (C) 2018 - 2023 BlackBelt Technology
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

import com.google.common.collect.ImmutableList;
import hu.blackbelt.judo.meta.ui.LayoutType;
import hu.blackbelt.judo.meta.ui.util.builder.UiBuilders;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LayoutDefaults {
    public static LayoutType mobile() {
        return UiBuilders.newLayoutTypeBuilder()
                .withName("mobile")
                .withCols(4)
                .withDefault_(false)
                .withMinimumResolution(0)
                .withMaximumResolution(599)
                .withMenuCollapsed(true)
                .withRowHeight(56)
                .build();
    }

    public static LayoutType tablet() {
        return UiBuilders.newLayoutTypeBuilder()
                .withName("tablet")
                .withCols(8)
                .withDefault_(false)
                .withMinimumResolution(600)
                .withMaximumResolution(839)
                .withMenuCollapsed(true)
                .withRowHeight(56)
                .build();
    }

    public static LayoutType desktop() {
        return UiBuilders.newLayoutTypeBuilder()
                .withName("desktop")
                .withCols(12)
                .withDefault_(true)
                .withMinimumResolution(840)
                .withMaximumResolution(32767)
                .withMenuCollapsed(false)
                .withRowHeight(56)
                .build();
    }

    public static LayoutTypeResolver defaultLayouts() {
        return application -> ImmutableList.<LayoutType>builder()
                .add(mobile(), tablet(), desktop())
                .build();
    }
}

