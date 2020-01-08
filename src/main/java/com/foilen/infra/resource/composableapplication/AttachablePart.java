/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.composableapplication;

import com.foilen.infra.plugin.v1.model.resource.AbstractIPResource;

/**
 * Extend this class to provide attachable parts to a {@link ComposableApplication}.
 */
public abstract class AttachablePart extends AbstractIPResource {

    /**
     * What to do to attach this part to the application.
     *
     * @param context
     *            the services and resources
     */
    public abstract void attachTo(AttachablePartContext context);

}
