<!-- Represents metadata section on resource overview page -->
<div class="my-3 p-3 bg-body rounded shadow-sm" id="metadata">
    <h6 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 text-success">
        <#assign metadataHeaderInfo>
            <@s.text name='manage.metadata.basic.required.message'/>
            <#if resource.coreType?has_content && resource.coreType==metadataType>
                </br></br>
                <@s.text name='manage.overview.source.hidden'>
                    <@s.param><a href="${baseURL}/manage/metadata-basic.do?r=${resource.shortname}&amp;edit=Edit"><@s.text name="submenu.basic"/></a></@s.param>
                </@s.text>
            </#if>
        </#assign>
        <@popoverTextInfo metadataHeaderInfo/>

        <@s.text name='manage.overview.metadata'/>
    </h6>

    <div class="row">
        <div class="col-sm-9 order-sm-last">
            <p class="text-muted mx-md-4 mx-2">
                <@s.text name="manage.overview.metadata.description"/>
            </p>

            <div class="table-responsive">
                <table class="table table-sm">
                    <tr>
                        <#if metadataModifiedSinceLastPublication>
                            <@s.text name='manage.home.last.modified'/> ${resource.getMetadataModified()?date?string.medium!}
                        <#elseif resource.lastPublished??>
                            <@s.text name="manage.overview.notModified"/>
                        </#if>
                    </tr>
                </table>
            </div>
        </div>

        <div class="col-sm-3">
            <div class="mx-md-4 mx-2">
                <form action='metadata-basic.do' method='get'>
                    <input name="r" type="hidden" value="${resource.shortname}"/>
                    <#if missingMetadata>
                        <@s.submit cssClass="btn btn-outline-success" name="edit" key="button.edit"/>
                        <span data-bs-container="body" data-bs-toggle="popover" data-bs-placement="top" data-bs-html="true" data-bs-content="
                            <@s.text name="manage.overview.missing.metadata"/>
                        ">
                        <i class="bi bi-exclamation-triangle-fill text-warning"></i>
                        </span>
                    <#else>
                        <@s.submit cssClass="btn btn-outline-success" name="edit" key="button.edit"/>
                    </#if>
                </form>
            </div>
        </div>
    </div>
</div>
