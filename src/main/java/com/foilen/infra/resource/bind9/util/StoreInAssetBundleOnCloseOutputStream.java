/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.bind9.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.foilen.infra.plugin.v1.model.base.IPApplicationDefinitionAssetsBundle;
import com.foilen.smalltools.tools.CharsetTools;

public class StoreInAssetBundleOnCloseOutputStream extends OutputStream {

    private IPApplicationDefinitionAssetsBundle assetsBundle;
    private String filename;

    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    public StoreInAssetBundleOnCloseOutputStream(IPApplicationDefinitionAssetsBundle assetsBundle, String filename) {
        this.assetsBundle = assetsBundle;
        this.filename = filename;
    }

    @Override
    public void close() throws IOException {
        byteArrayOutputStream.close();

        // Store in asset bundle
        assetsBundle.addAssetContent(filename, byteArrayOutputStream.toString(CharsetTools.UTF_8.name()));
    }

    @Override
    public void flush() throws IOException {
        byteArrayOutputStream.flush();
    }

    @Override
    public void write(byte[] b) throws IOException {
        byteArrayOutputStream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        byteArrayOutputStream.write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
        byteArrayOutputStream.write(b);
    }

}
