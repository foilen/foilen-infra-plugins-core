<VirtualHost *>
    ServerName ${domainName}
    ServerAlias ${domainName}
    
    ErrorLog "|/usr/bin/rotatelogs -n 1 /var/log/apache2/${domainName}-error.log ${logMaxSizeM}M"
    CustomLog "|/usr/bin/rotatelogs -n 1 /var/log/apache2/${domainName}-access.log ${logMaxSizeM}M" combined
    
    RewriteEngine On
<#if redirectionIsExact>
    RewriteRule /.* ${redirectionUrl} [R,L]
<#else>
    RewriteRule /(.*) ${redirectionUrl}$1 [R,L]
</#if>
</VirtualHost>
