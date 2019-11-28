[PHP]
max_execution_time = 300

upload_max_filesize = <#if upload_max_filesize==0>0<#else>${upload_max_filesize?c}M</#if>
post_max_size = 0
max_file_uploads = ${max_file_uploads?c}

memory_limit = <#if memory_limit==0>-1<#else>${memory_limit?c}M</#if>

<#if defaultEmailFrom??>
mail.force_extra_parameters = -f${defaultEmailFrom}
</#if>
