<!-- Represents metadata section on resource overview page -->
<div class="my-3 p-3 bg-body rounded shadow-sm" id="metadata">
    <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 text-success">
        <#assign metadataHeaderInfo>
            <@s.text name='manage.metadata.description'/>
            <#if resource.coreType?has_content && resource.coreType==metadataType>
                </br></br>
                <@s.text name='manage.overview.source.hidden'>
                    <@s.param><a href="${baseURL}/manage/metadata-basic.do?r=${resource.shortname}&amp;edit=Edit"><@s.text name="submenu.basic"/></a></@s.param>
                </@s.text>
            </#if>
        </#assign>
        <@popoverTextInfo metadataHeaderInfo/>

        <@s.text name='manage.overview.metadata'/>
    </h5>

    <div class="row">
        <div class="col-lg-9 order-lg-last">
            <p class="text-muted mx-md-4 mx-2">
                <@s.text name="manage.overview.metadata.description"/>
            </p>

            <div class="table-responsive mx-md-4 mx-2">
                <table class="table table-sm">
                    <tr>
                        <#if metadataModifiedSinceLastPublication>
                            <@s.text name='manage.home.last.modified'/> ${resource.getMetadataModified()?datetime?string.medium!}
                        <#elseif resource.lastPublished??>
                            <@s.text name="manage.overview.notModified"/>
                        </#if>
                    </tr>
                </table>
            </div>
        </div>

        <div class="col-lg-3 border-lg-right">
            <div class="mx-md-4 mx-2">
                <form action='metadata-basic.do' method='get'>
                    <input name="r" type="hidden" value="${resource.shortname}"/>
                    <#if missingMetadata>
                        <div class="btn-group" role="group">
                            <#assign metadataSubmitWarning>
                                <@s.text name="manage.overview.missing.metadata"/>
                            </#assign>
                            <button type="button" class="btn btn-sm btn-outline-warning" data-bs-container="body" data-bs-toggle="popover" data-bs-placement="top" data-bs-html="true" data-bs-content="${metadataSubmitWarning}">
                                <i class="bi bi-exclamation-triangle"></i>
                            </button>
                            <@s.submit cssClass="btn btn-sm btn-outline-warning" name="edit" key="button.edit"/>
                        </div>
                    <#else>
                        <@s.submit cssClass="btn btn-sm btn-outline-success" name="edit" key="button.edit"/>
                    </#if>
                </form>
                <form action='replace-eml.do' method='post' enctype="multipart/form-data" style="margin-top: 10px;">
                    <input name="r" type="hidden" value="${resource.shortname}"/>
                    <div class="row">
                        <div class="col-12">
                            <@s.file name="emlFile" cssClass="form-control form-control-sm my-1"/>
                        </div>
                        <div class="col-12">
                            <@s.submit name="emlReplace" cssClass="btn btn-sm btn-outline-success my-1 confirmEmlReplace" cssStyle="display: none" key="button.replace"/>
                            <@s.submit name="emlCancel" cssClass="btn btn-sm btn-outline-secondary my-1" cssStyle="display: none" key="button.cancel"/>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
