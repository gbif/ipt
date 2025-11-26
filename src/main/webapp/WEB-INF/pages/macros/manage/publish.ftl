<#macro publishIcon>
    <svg viewBox="0 0 24 24" class="overview-action-button-icon">
        <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
    </svg>
</#macro>

<#macro publishButton className>
    <button id="publishButton"
            class="${className} text-gbif-header-2 icon-button icon-material-actions overview-action-button"
            name="publish"
            value="Publish"
            type="submit">
        <@publishIcon/>
        <@s.text name="button.publish"/>
    </button>
</#macro>

<#macro showPublicationWarningButton>
    <a id="publish-button-show-warning"
       class="text-gbif-header-2 icon-button icon-material-actions overview-action-button"
       href="#">
        <@publishIcon/>
        <@s.text name="button.publish"/>
    </a>
</#macro>

<#macro publish resource>
    <#if resource.status == "DELETED">
        <button id="publish-button-show-warning"
                class="publishDeletedResource text-gbif-header-2 icon-button icon-material-actions overview-action-button"
                type="button">
            <@publishIcon />
            <@s.text name="button.publish"/>
        </button>

    <!-- resources cannot be published if the mandatory metadata is missing -->
    <!-- resources cannot be published if mappings are missing (for DPs) -->
    <!-- resources that are already registered cannot be re-published if they haven't been assigned a GBIF-supported license -->
    <#elseif !validMetadata
        || (dataPackageResource && dataPackageMappingsMissing)
        || (resource.isRegistered() && !resource.isAssignedGBIFSupportedLicense())>
        <@showPublicationWarningButton/>

    <!-- resources cannot be published if the publishing organisation is missing -->
    <#elseif !resource.organisation?has_content>
        <@showPublicationWarningButton/>

    <!-- previously published resources without a DOI, or that haven't been registered yet can be republished whenever by any manager -->
    <#elseif resource.lastPublished?? && resource.identifierStatus == "UNRESERVED" && resource.status != "REGISTERED">

        <form action="publish.do" method="post">
            <input name="r" type="hidden" value="${resource.shortname}"/>
            <textarea id="summary" name="summary" cols="40" rows="5" style="display: none"></textarea>

            <#if dataPackageResource>
                <#assign buttonClass="confirmPublish"/>
            <#else>
                <#assign buttonClass="confirmPublishMinorVersion"/>
            </#if>

            <@publishButton buttonClass/>
        </form>

    <!-- resources with a reserved DOI, existing registered DOI, or registered with GBIF can only be republished by managers with registration rights -->
    <#elseif (resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION")
        || (resource.identifierStatus == "PUBLIC" && resource.isAlreadyAssignedDoi())
        || resource.status == "REGISTERED">

        <!-- the user must have registration rights -->
        <#if !currentUser.hasRegistrationRights()>
            <@showPublicationWarningButton/>

        <!-- an organisation with DOI account be activated (if resource has a reserved DOI or existing registered DOI) -->
        <#elseif ((resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION" && resource.isAlreadyAssignedDoi())
        || (resource.identifierStatus == "PUBLIC" && resource.isAlreadyAssignedDoi()))
        && !organisationWithPrimaryDoiAccount??>
            <@showPublicationWarningButton/>

        <!-- when a DOI is reserved -->
        <#elseif resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION">
            <!-- and the resource has no existing DOI and its status is private..  -->
            <#if !resource.isAlreadyAssignedDoi() && resource.status == "PRIVATE">
                <!-- and the resource has never been published before, the first publication is a new major version -->
                <#if !resource.lastPublished??>
                    <form action="publish.do" method="post">
                        <input name="r" type="hidden" value="${resource.shortname}"/>
                        <textarea id="summary" name="summary" cols="40" rows="5" style="display: none"></textarea>

                        <#if dataPackageResource>
                            <#assign buttonClass="confirmPublish"/>
                        <#else>
                            <#assign buttonClass="confirmPublishMajorVersionWithoutDOI"/>
                        </#if>

                        <@publishButton buttonClass/>
                    </form>

                <!-- and the resource has been published before, the next publication is a new minor version -->
                <#else>
                    <form action="publish.do" method="post">
                        <input name="r" type="hidden" value="${resource.shortname}"/>
                        <textarea id="summary" name="summary" cols="40" rows="5" style="display: none"></textarea>

                        <#if dataPackageResource>
                            <#assign buttonClass="confirmPublish"/>
                        <#else>
                            <#assign buttonClass="confirmPublishMinorVersion"/>
                        </#if>

                        <@publishButton buttonClass/>
                    </form>
                </#if>

            <!-- and its status is public (or registered), its reserved DOI can be registered during next publication  -->
            <#elseif resource.status == "PUBLIC" || resource.status == "REGISTERED">
                <form action="publish.do" method="post">
                    <input name="r" type="hidden" value="${resource.shortname}"/>
                    <textarea id="summary" name="summary" cols="40" rows="5" style="display: none"></textarea>

                    <#if dataPackageResource>
                        <#assign buttonClass="confirmPublish"/>
                    <#else>
                        <#assign buttonClass="confirmPublishMajorVersion"/>
                    </#if>

                    <@publishButton buttonClass/>
                </form>
            </#if>

        <!-- publishing a new minor version -->
        <#elseif resource.identifierStatus == "PUBLIC" && resource.isAlreadyAssignedDoi()>
            <form action="publish.do" method="post">
                <input name="r" type="hidden" value="${resource.shortname}"/>
                <textarea id="summary" name="summary" cols="40" rows="5" style="display: none"></textarea>

                <#if dataPackageResource>
                    <#assign buttonClass="confirmPublish"/>
                <#else>
                    <#assign buttonClass="confirmPublishMinorVersion"/>
                </#if>

                <@publishButton buttonClass/>
            </form>

        <!-- publishing a new version registered with GBIF -->
        <#elseif resource.status == "REGISTERED">
            <form action="publish.do" method="post">
                <input name="r" type="hidden" value="${resource.shortname}"/>
                <textarea id="summary" name="summary" cols="40" rows="5" style="display: none"></textarea>

                <#if dataPackageResource>
                    <#assign buttonClass="confirmPublish"/>
                <#else>
                    <#assign buttonClass="confirmPublishMinorVersion"/>
                </#if>

                <@publishButton buttonClass/>
            </form>
        </#if>

    <!-- first time any resource not assigned a DOI is published is always new major version -->
    <#elseif !resource.lastPublished?? && resource.identifierStatus == "UNRESERVED">
        <form action="publish.do" method="post">
            <input name="r" type="hidden" value="${resource.shortname}"/>
            <textarea id="summary" name="summary" cols="40" rows="5" style="display: none"></textarea>

            <#if dataPackageResource>
                <#assign buttonClass="confirmPublish"/>
            <#else>
                <#assign buttonClass="confirmPublishMajorVersionWithoutDOI"/>
            </#if>

            <@publishButton buttonClass/>
        </form>
    <#else>
        <!-- otherwise prevent publication from happening just to be safe -->
    </#if>
</#macro>
