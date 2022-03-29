<!-- Represents metadata section on resource overview page -->
<span class="anchor anchor-home-resource-page" id="anchor-metadata"></span>
<div class="py-5 mx-4 border-bottom section" id="metadata">
    <h5 class="pb-2 mb-4 text-gbif-header-2 fw-400">
        <#assign metadataHeaderInfo>
            <@s.text name='manage.metadata.description'/>
            <#if resource.coreType?has_content && resource.coreType==metadataType>
                <br><br>
                <@s.text name='manage.overview.source.hidden'>
                    <@s.param><a href="${baseURL}/manage/metadata-basic.do?r=${resource.shortname}&amp;edit=Edit"><@s.text name="submenu.basic"/></a></@s.param>
                </@s.text>
            </#if>
        </#assign>
        <@popoverTextInfo metadataHeaderInfo/>

        <@s.text name='manage.overview.metadata'/>
    </h5>

    <div class="row">
        <div class="col-lg-3 border-lg-right border-lg-max py-lg-max-2 pe-lg-5 mb-4 rounded">
            <div>
                <form action='replace-eml.do' method='post' enctype="multipart/form-data">
                    <input name="r" type="hidden" value="${resource.shortname}"/>
                    <div class="row">
                        <div class="col-12">
                            <@s.file name="emlFile" cssClass="form-control form-control-sm my-1"/>
                        </div>
                        <div class="col-12">
                            <@s.submit name="emlReplace" cssClass="btn btn-sm btn-outline-gbif-primary my-1 confirmEmlReplace" cssStyle="display: none" key="button.replace"/>
                            <@s.submit name="emlCancel" cssClass="btn btn-sm btn-outline-secondary my-1" cssStyle="display: none" key="button.cancel"/>
                        </div>
                    </div>
                </form>
                <form action='metadata-basic.do' method='get' class="my-1">
                    <input name="r" type="hidden" value="${resource.shortname}"/>
                    <@s.submit cssClass="btn btn-sm btn-outline-gbif-primary" name="edit" key="button.edit"/>
                </form>
            </div>
        </div>

        <div class="col-lg-9 ps-lg-5">
            <p>
                <@s.text name="manage.overview.metadata.description"/>
            </p>

            <#if missingMetadata>
                <p class="text-gbif-warning fst-italic">
                    <i class="bi bi-exclamation-triangle"></i>
                    <@s.text name="manage.overview.missing.metadata"/>
                </p>
            </#if>

            <div class="details mb-3">
                <#if metadataModifiedSinceLastPublication>
                    <@s.text name='manage.home.last.modified'/> ${resource.getMetadataModified()?datetime?string.medium!}
                <#elseif resource.lastPublished??>
                    <@s.text name="manage.overview.notModified"/>
                </#if>
            </div>
        </div>
    </div>
</div>
