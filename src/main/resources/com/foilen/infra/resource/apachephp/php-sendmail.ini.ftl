[PHP]
max_execution_time = 300

upload_max_filesize = 64M
post_max_size = 128M
memory_limit = 256M

<#if defaultEmailFrom??>
mail.force_extra_parameters = -f${defaultEmailFrom}
</#if>
