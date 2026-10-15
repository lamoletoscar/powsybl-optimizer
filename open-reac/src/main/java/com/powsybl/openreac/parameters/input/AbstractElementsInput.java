/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.openreac.parameters.input;

import com.powsybl.ampl.converter.AmplSubset;
import com.powsybl.ampl.executor.AmplInputFile;
import com.powsybl.commons.util.StringToIntMapper;
import com.powsybl.openreac.parameters.AmplIOUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nicolas Pierre {@literal <nicolas.pierre at artelys.com>}
 */
public abstract class AbstractElementsInput implements AmplInputFile {

    private final List<String> elementIds;

    protected AbstractElementsInput(List<String> elementIds) {
        this.elementIds = elementIds;
    }

    @Override
    public void write(BufferedWriter writer, StringToIntMapper<AmplSubset> stringToIntMapper) throws IOException {
        List<String> header = new ArrayList<>(List.of("#num", "id"));
        header.addAll(getExtraColumnNames());
        writer.write(String.join(" ", header) + "\n");
        for (String elementID : elementIds) {
            int amplId = stringToIntMapper.getInt(getElementAmplSubset(), elementID);
            List<String> tokens = new ArrayList<>(List.of(Integer.toString(amplId), AmplIOUtils.addQuotes(elementID)));
            tokens.addAll(getExtraColumnValues(elementID));
            writer.write(String.join(" ", tokens));
            writer.newLine();
        }
        //add new line at the end of the file !
        writer.newLine();
        writer.flush();
    }

    abstract AmplSubset getElementAmplSubset();

    /**
     * @return the names of the columns written after the num and the id of the elements, none by default.
     */
    List<String> getExtraColumnNames() {
        return List.of();
    }

    /**
     * @return the values of these columns for an element.
     */
    List<String> getExtraColumnValues(String elementId) {
        return List.of();
    }
}
