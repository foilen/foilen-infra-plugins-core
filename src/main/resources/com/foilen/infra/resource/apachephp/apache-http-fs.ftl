<VirtualHost *>
  DocumentRoot ${baseFolder}${mainSiteRelativePath}
  
  SetEnvIf x-forwarded-proto https HTTPS=on
  
  ErrorLog /var/log/apache2/error.log
  CustomLog /var/log/apache2/access.log combined
  
  <Directory ${baseFolder}${mainSiteRelativePath}>
    AllowOverride All
    Require all granted
  </Directory>
  
  <#list aliases as alias>
  Alias ${alias.alias} ${alias.folder}
  </#list>
  
  <#list aliases as alias>
  <Directory ${alias.folder}>
    AllowOverride All
    Require all granted
  </Directory>
  </#list>

</VirtualHost>
