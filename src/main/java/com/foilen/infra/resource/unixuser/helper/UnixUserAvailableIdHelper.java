/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.unixuser.helper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.foilen.infra.plugin.v1.core.exception.ProblemException;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.smalltools.tools.SearchingAvailabilityLongTools;

public class UnixUserAvailableIdHelper {

    private static SearchingAvailabilityLongTools cachedSearchingAvailability;

    public static long getNextAvailableId() {
        if (cachedSearchingAvailability == null) {
            throw new ProblemException("UnixUserAvailableIdHelper has not been initialised");
        }

        Optional<Long> next = cachedSearchingAvailability.getNext();
        if (!next.isPresent()) {
            throw new ProblemException("There is no more unix user id available");
        }
        return next.get();
    }

    public static void init(IPResourceService resourceService) {
        cachedSearchingAvailability = new SearchingAvailabilityLongTools(70000, Long.MAX_VALUE, 1000, //
                (from, to) -> {
                    long range = to - from + 1;
                    List<UnixUser> unixUsers = resourceService.resourceFindAll(resourceService.createResourceQuery(UnixUser.class) //
                            .propertyGreaterAndEquals(UnixUser.PROPERTY_ID, from) //
                            .propertyLesserAndEquals(UnixUser.PROPERTY_ID, to)).stream() //
                            .sorted((a, b) -> Long.compare(a.getId(), b.getId())) //
                            .collect(Collectors.toList());

                    // There is one id available
                    if (range != unixUsers.size()) {
                        long foundId = from;
                        for (UnixUser unixUser : unixUsers) {
                            if (foundId != unixUser.getId()) {
                                break;
                            } else {
                                ++foundId;
                            }
                        }
                        return Optional.of(foundId);

                    }

                    return Optional.empty();
                });

    }

}
