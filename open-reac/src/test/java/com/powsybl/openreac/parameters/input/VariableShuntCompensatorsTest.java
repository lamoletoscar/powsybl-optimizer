/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.openreac.parameters.input;

import com.powsybl.ampl.converter.AmplSubset;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.util.StringToIntMapper;
import com.powsybl.iidm.network.Network;
import com.powsybl.openreac.network.ShuntNetworkFactory;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Set;

import static com.powsybl.openreac.parameters.input.ReportTestHelper.hasReportWithKey;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Oscar Lamolet {@literal <lamoletoscar at proton.me>}
 */
class VariableShuntCompensatorsTest {

    private static final String NON_LINEAR_REPORT_KEY = "optimizer.openreac.nonLinearShuntCompensatorsKeptContinuous";

    @Test
    void onlyLinearShuntCompensatorsHaveDiscreteSections() throws IOException {
        Network network = ShuntNetworkFactory.createWithLinearModel();
        network.getVoltageLevel("vl3").newShuntCompensator()
                .setId("NON_LINEAR_SHUNT")
                .setConnectableBus("b3")
                .setSectionCount(0)
                .newNonLinearModel()
                .beginSection()
                .setB(1e-3)
                .setG(0.)
                .endSection()
                .add()
                .add();
        List<String> shuntIds = List.of("SHUNT", "NON_LINEAR_SHUNT");

        ReportNode reportNode = createReportNode();
        VariableShuntCompensators variableShunts = new VariableShuntCompensators(shuntIds, true, network, reportNode);
        assertEquals(Set.of("SHUNT"), variableShunts.getDiscreteElementIds());
        assertTrue(hasReportWithKey(reportNode, NON_LINEAR_REPORT_KEY));

        StringToIntMapper<AmplSubset> mapper = new StringToIntMapper<>(AmplSubset.class);
        shuntIds.forEach(id -> mapper.newInt(AmplSubset.SHUNT, id));
        StringWriter writer = new StringWriter();
        try (BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
            variableShunts.write(bufferedWriter, mapper);
        }
        assertEquals(List.of("#num id discrete", "1 \"SHUNT\" 1", "2 \"NON_LINEAR_SHUNT\" 0", ""), writer.toString().lines().toList());

        // Without the option, no shunt has discrete sections and nothing is reported
        reportNode = createReportNode();
        assertTrue(new VariableShuntCompensators(shuntIds, false, network, reportNode).getDiscreteElementIds().isEmpty());
        assertFalse(hasReportWithKey(reportNode, NON_LINEAR_REPORT_KEY));
    }

    private static ReportNode createReportNode() {
        return ReportNode.newRootReportNode().withAllResourceBundlesFromClasspath().withMessageTemplate("optimizer.openreac.openReac").build();
    }
}
