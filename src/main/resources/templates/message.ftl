<#import "/spring.ftl" as spring/>

<#macro status>
<div>
    <#if statusError??>
        <div>
            ${statusError}
        </div>
    </#if>

    <#if statusSuccess??>
        <div>
            ${statusSuccess}
        </div>
    </#if>
</div>
<div>
<#if indexCount??>
    <div>
        Indexed pages: <b>${indexCount!"-"}</b> (${indexTime!"-"})
    </div>
</#if>
</div>
</#macro>