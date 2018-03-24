<#if licenseFirst??>
${licenseFirst}
</#if>
${licensePrefix}Copyright ${date?date?string("yyyy")} ${project.organization!user}. All rights reserved.
<#if licenseLast??>
${licenseLast}
</#if>