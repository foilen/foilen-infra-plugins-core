/*
    Foilen Infra Plugins Core
    https://github.com/foilen/foilen-infra-plugins-core
    Copyright (c) 2018-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.resource.letsencrypt.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.infra.plugin.v1.model.resource.InfraPluginResourceCategory;
import com.foilen.infra.resource.apachephp.ApachePhp;
import com.foilen.infra.resource.composableapplication.AttachablePart;
import com.foilen.infra.resource.composableapplication.AttachablePartContext;
import com.foilen.infra.resource.webcertificate.WebsiteCertificate;
import com.foilen.smalltools.tools.CollectionsTools;
import com.foilen.smalltools.tools.StringTools;

/**
 * This is to add a file to an {@link ApachePhp} application to provide LetsEncrypt certificate. <br/>
 * Links to:
 * <ul>
 * <li>{@link WebsiteCertificate}: (1) USES - The LetsEncrypt certificate to help populating</li>
 * </ul>
 */
public class LetsEncryptWithFileAttachable extends AttachablePart implements Comparable<LetsEncryptWithFileAttachable> {

    private static final Logger logger = LoggerFactory.getLogger(LetsEncryptWithFileAttachable.class);

    public static final String RESOURCE_TYPE = "Letsencrypt With File Attachable";

    public static final String META_FILE_NAME = "file_name";
    public static final String META_FILE_CONTENT = "file_content";

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_BASE_PATH = "basePath";

    private String name;
    private String basePath;

    public LetsEncryptWithFileAttachable() {
    }

    /**
     * Primary key.
     *
     * @param name
     *            the name
     */
    public LetsEncryptWithFileAttachable(String name) {
        this.name = name;
    }

    @Override
    public void attachTo(AttachablePartContext context) {
        String fileName = getMeta().get(META_FILE_NAME);
        String fileContent = getMeta().get(META_FILE_CONTENT);
        logger.info("[{}] File: {} ; With content: {}", name, fileName, fileContent);
        if (CollectionsTools.isAllItemNotNullOrEmpty(fileName, fileContent)) {
            String challengesPath = basePath + ".well-known/acme-challenge";
            fileName = challengesPath + "/" + fileName;
            String htAccessFileName = challengesPath + "/.htaccess";
            logger.info("[{}] Full file name: {}", name, fileName);
            context.getApplicationDefinition().addExecuteWhenStartedCommand("mkdir -p " + challengesPath);
            context.getApplicationDefinition().addExecuteWhenStartedCommand("find " + challengesPath + " -type f -mtime +1 -exec rm -f {} \\; || true");
            context.getApplicationDefinition().addExecuteWhenStartedCommand("echo " + fileContent + " > " + fileName);
            context.getApplicationDefinition().addExecuteWhenStartedCommand("echo Satisfy any > " + htAccessFileName);
        }
    }

    @Override
    public int compareTo(LetsEncryptWithFileAttachable o) {
        return StringTools.safeComparisonNullFirst(name, o.name);
    }

    public String getBasePath() {
        return basePath;
    }

    public String getName() {
        return name;
    }

    @Override
    public InfraPluginResourceCategory getResourceCategory() {
        return InfraPluginResourceCategory.NET;
    }

    @Override
    public String getResourceDescription() {
        return "Using files";
    }

    @Override
    public String getResourceName() {
        return name;
    }

    public LetsEncryptWithFileAttachable setBasePath(String basePath) {
        this.basePath = basePath;
        return this;
    }

    public LetsEncryptWithFileAttachable setName(String name) {
        this.name = name;
        return this;
    }

}
