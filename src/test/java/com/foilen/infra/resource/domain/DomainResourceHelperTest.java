/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import com.foilen.smalltools.test.asserts.AssertTools;

public class DomainResourceHelperTest {

    @Test
    public void testGetParentString() {
        Optional<Domain> domain = Optional.of(new Domain("super.test.foilen-lab.com"));

        List<String> actual = new ArrayList<>();
        while (domain.isPresent()) {
            actual.add(domain.get().getName());
            domain = DomainResourceHelper.getParent(domain.get());
        }

        List<String> expected = Arrays.asList( //
                "super.test.foilen-lab.com", //
                "test.foilen-lab.com", //
                "foilen-lab.com" //
        );
        AssertTools.assertJsonComparison(expected, actual);
    }

}
