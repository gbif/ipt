<#macro publish resource>
    <form action='publish.do' method='post'>
        <input name="r" type="hidden" value="${resource.shortname}"/>
        <input name="publish" type="hidden" value="Publish"/>

        <textarea id="summary" name="summary" cols="40" rows="5" style="display: none"></textarea>


        <#if missingMetadata>
            <!-- resources cannot be published if the mandatory metadata is missing -->
        <#elseif resource.isRegistered() && !resource.isAssignedGBIFSupportedLicense()>
            <!-- resources that are already registered cannot be re-published if they haven't been assigned a GBIF-supported license -->

        <!-- previously published resources without a DOI, or that haven't been registered yet can be republished whenever by any manager -->
        <#elseif resource.lastPublished?? && resource.identifierStatus == "UNRESERVED" && resource.status != "REGISTERED">

            <button class="confirmPublishMinorVersion btn btn-sm overview-action-button" id="publishButton" name="publish" type="submit">
                <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                    <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                </svg>
                <@s.text name="button.publish"/>
            </button>

        <!-- resources with a reserved DOI, existing registered DOI, or registered with GBIF can only be republished by managers with registration rights -->
        <#elseif (resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION")
        || (resource.identifierStatus == "PUBLIC" && resource.isAlreadyAssignedDoi())
        || resource.status == "REGISTERED">

            <#if !currentUser.hasRegistrationRights()>
                <!-- the user must have registration rights -->

            <!-- an organisation with DOI account be activated (if resource has a reserved DOI or existing registered DOI) -->
            <#elseif ((resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION" && resource.isAlreadyAssignedDoi())
            || (resource.identifierStatus == "PUBLIC" && resource.isAlreadyAssignedDoi()))
            && !organisationWithPrimaryDoiAccount??>

            <!-- when a DOI is reserved -->
            <#elseif resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION">
                <!-- and the resource has no existing DOI and its status is private..  -->
                <#if !resource.isAlreadyAssignedDoi() && resource.status == "PRIVATE">
                    <!-- and the resource has never been published before, the first publication is a new major version -->
                    <#if !resource.lastPublished??>
                        <button class="confirmPublishMajorVersionWithoutDOI btn btn-sm overview-action-button" id="publishButton" name="publish" type="submit">
                            <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                                <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                            </svg>
                            <@s.text name="button.publish"/>
                        </button>
                    <!-- and the resource has been published before, the next publication is a new minor version -->
                    <#else>
                        <button class="confirmPublishMinorVersion btn btn-sm overview-action-button" id="publishButton" name="publish" type="submit">
                            <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                                <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                            </svg>
                            <@s.text name="button.publish"/>
                        </button>
                    </#if>

                <!-- and its status is public (or registered), its reserved DOI can be registered during next publication  -->
                <#elseif resource.status == "PUBLIC" || resource.status == "REGISTERED">
                    <@s.submit cssClass="confirmPublishMajorVersion btn btn-sm overview-action-button" id="publishButton" name="publish" key="button.publish"/>
                </#if>

            <!-- publishing a new minor version -->
            <#elseif resource.identifierStatus == "PUBLIC" && resource.isAlreadyAssignedDoi()>
                <button class="confirmPublishMinorVersion btn btn-sm overview-action-button" id="publishButton" name="publish" type="submit">
                    <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                        <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                    </svg>
                    <@s.text name="button.publish"/>
                </button>

            <!-- publishing a new version registered with GBIF -->
            <#elseif resource.status == "REGISTERED">
                <button class="confirmPublishMinorVersion btn btn-sm overview-action-button" id="publishButton" name="publish" type="submit">
                    <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                        <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                    </svg>
                    <@s.text name="button.publish"/>
                </button>
            </#if>

        <!-- first time any resource not assigned a DOI is published is always new major version -->
        <#elseif !resource.lastPublished?? && resource.identifierStatus == "UNRESERVED">
            <button class="confirmPublishMajorVersionWithoutDOI btn btn-sm overview-action-button" id="publishButton" name="publish" type="submit">
                <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                    <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                </svg>
                <@s.text name="button.publish"/>
            </button>
        <#else>
            <!-- otherwise prevent publication from happening just to be safe -->
        </#if>
    </form>
</#macro>
