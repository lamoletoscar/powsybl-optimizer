/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.openreac.parameters.input;

import com.powsybl.ampl.converter.AmplSubset;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensatorModelType;
import com.powsybl.openreac.Reports;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Nicolas Pierre {@literal <nicolas.pierre at artelys.com>}
 *
 * List of shunts which section that can be modified by OpenReac, each flagged when its sections are rounded by the
 * optimizer. Only the shunts with a linear model can be: the AMPL model derives their sections from the exported shunt
 * data, which is exact for this model only.
 */
public class VariableShuntCompensators extends AbstractElementsInput {

    public static final String PARAM_SHUNT_FILE_NAME = "param_shunts.txt";

    private final Set<String> discreteElementIds;

    public VariableShuntCompensators(List<String> elementIds, boolean discreteSections, Network network, ReportNode reportNode) {
        super(elementIds);
        if (discreteSections) {
            Map<Boolean, List<String>> idsByLinearModel = elementIds.stream()
                    .collect(Collectors.partitioningBy(id -> network.getShuntCompensator(id).getModelType() == ShuntCompensatorModelType.LINEAR));
            this.discreteElementIds = Set.copyOf(idsByLinearModel.get(true));
            List<String> nonLinearIds = idsByLinearModel.get(false);
            if (!nonLinearIds.isEmpty()) {
                Reports.reportNonLinearShuntCompensatorsKeptContinuous(reportNode, nonLinearIds);
            }
        } else {
            this.discreteElementIds = Set.of();
        }
    }

    /**
     * @return the ids of the shunts whose sections are rounded by the optimizer.
     */
    public Set<String> getDiscreteElementIds() {
        return discreteElementIds;
    }

    @Override
    AmplSubset getElementAmplSubset() {
        return AmplSubset.SHUNT;
    }

    @Override
    public String getFileName() {
        return PARAM_SHUNT_FILE_NAME;
    }

    @Override
    List<String> getExtraColumnNames() {
        return List.of("discrete");
    }

    @Override
    List<String> getExtraColumnValues(String elementId) {
        return List.of(discreteElementIds.contains(elementId) ? "1" : "0");
    }
}
