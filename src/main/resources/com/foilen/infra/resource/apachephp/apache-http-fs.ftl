<VirtualHost *>
  DocumentRoot ${baseFolder}${mainSiteRelativePath}
  
  SetEnvIf x-forwarded-proto https HTTPS=on
  
  ErrorLog "|/usr/bin/rotatelogs -n 1 /var/log/apache2/error.log ${logMaxSizeM}M"
  CustomLog "|/usr/bin/rotatelogs -n 1 /var/log/apache2/access.log ${logMaxSizeM}M" combined
  
  <Directory ${baseFolder}${mainSiteRelativePath}>
    <#if useBasicAuth>
    AllowOverride All
    AuthType Basic
    AuthName "Restricted Content"
    AuthUserFile /htpasswd
    Require valid-user
    <#else>
    AllowOverride All
    Require all granted
    </#if>
  </Directory>
  
  <#list aliases as alias>
  Alias ${alias.alias} ${alias.folder}
  </#list>
  
  <#list aliases as alias>
  <Directory ${alias.folder}>
    <#if useBasicAuth>
    AllowOverride All
    AuthType Basic
    AuthName "Restricted Content"
    AuthUserFile /htpasswd
    Require valid-user
    <#else>
    AllowOverride All
    Require all granted
    </#if>
  </Directory>
  </#list>

</VirtualHost>
