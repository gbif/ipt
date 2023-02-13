<#macro publish resource>
    <#if missingMetadata>
        <!-- resources cannot be published if the mandatory metadata is missing -->
        <a id="publish-button-show-warning" class="text-gbif-header-2 icon-button icon-material-actions overview-action-button fs-smaller-2" type="button" href="#">
            <svg class="overview-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                    <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                </svg>
            </svg>
            <@s.text name="button.publish"/>
        </a>
    <#elseif resource.isRegistered() && !resource.isAssignedGBIFSupportedLicense()>
        <!-- resources that are already registered cannot be re-published if they haven't been assigned a GBIF-supported license -->
        <a id="publish-button-show-warning" class="text-gbif-header-2 icon-button icon-material-actions overview-action-button fs-smaller-2" type="button" href="#">
            <svg class="overview-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                    <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                </svg>
            </svg>
            <@s.text name="button.publish"/>
        </a>

        <!-- previously published resources without a DOI, or that haven't been registered yet can be republished whenever by any manager -->
    <#elseif resource.lastPublished?? && resource.identifierStatus == "UNRESERVED" && resource.status != "REGISTERED">

        <form action="publish.do" method="post">
            <input name="r" type="hidden" value="${resource.shortname}"/>
            <textarea id="summary" name="summary" cols="40" rows="5" style="display: none"></textarea>

            <button class="confirmPublishMinorVersion text-gbif-header-2 icon-button icon-material-actions overview-action-button fs-smaller-2" id="publishButton" name="publish" value="Publish" type="submit">
                <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                    <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                </svg>
                <@s.text name="button.publish"/>
            </button>
        </form>

        <!-- resources with a reserved DOI, existing registered DOI, or registered with GBIF can only be republished by managers with registration rights -->
    <#elseif (resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION")
    || (resource.identifierStatus == "PUBLIC" && resource.isAlreadyAssignedDoi())
    || resource.status == "REGISTERED">

        <#if !currentUser.hasRegistrationRights()>
            <!-- the user must have registration rights -->
            <a id="publish-button-show-warning" class="text-gbif-header-2 icon-button icon-material-actions overview-action-button fs-smaller-2" type="button" href="#">
                <svg class="overview-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                    <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                        <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                    </svg>
                </svg>
                <@s.text name="button.publish"/>
            </a>

            <!-- an organisation with DOI account be activated (if resource has a reserved DOI or existing registered DOI) -->
        <#elseif ((resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION" && resource.isAlreadyAssignedDoi())
        || (resource.identifierStatus == "PUBLIC" && resource.isAlreadyAssignedDoi()))
        && !organisationWithPrimaryDoiAccount??>
            <a id="publish-button-show-warning" class="text-gbif-header-2 icon-button icon-material-actions overview-action-button fs-smaller-2" type="button" href="#">
                <svg class="overview-action-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                    <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                        <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                    </svg>
                </svg>
                <@s.text name="button.publish"/>
            </a>

            <!-- when a DOI is reserved -->
        <#elseif resource.identifierStatus == "PUBLIC_PENDING_PUBLICATION">
            <!-- and the resource has no existing DOI and its status is private..  -->
            <#if !resource.isAlreadyAssignedDoi() && resource.status == "PRIVATE">
                <!-- and the resource has never been published before, the first publication is a new major version -->
                <#if !resource.lastPublished??>
                    <form action="publish.do" method="post">
                        <input name="r" type="hidden" value="${resource.shortname}"/>
                        <textarea id="summary" name="summary" cols="40" rows="5" style="display: none"></textarea>

                        <button class="confirmPublishMajorVersionWithoutDOI text-gbif-header-2 icon-button icon-material-actions overview-action-button fs-smaller-2" id="publishButton" name="publish" value="Publish" type="submit">
                            <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                                <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                            </svg>
                            <@s.text name="button.publish"/>
                        </button>
                    </form>

                    <!-- and the resource has been published before, the next publication is a new minor version -->
                <#else>
                    <form action="publish.do" method="post">
                        <input name="r" type="hidden" value="${resource.shortname}"/>
                        <textarea id="summary" name="summary" cols="40" rows="5" style="display: none"></textarea>

                        <button class="confirmPublishMinorVersion text-gbif-header-2 icon-button icon-material-actions overview-action-button fs-smaller-2" id="publishButton" name="publish" value="Publish" type="submit">
                            <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                                <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                            </svg>
                            <@s.text name="button.publish"/>
                        </button>
                    </form>
                </#if>

                <!-- and its status is public (or registered), its reserved DOI can be registered during next publication  -->
            <#elseif resource.status == "PUBLIC" || resource.status == "REGISTERED">
                <form action="publish.do" method="post">
                    <input name="r" type="hidden" value="${resource.shortname}"/>
                    <textarea id="summary" name="summary" cols="40" rows="5" style="display: none"></textarea>

                    <button class="confirmPublishMajorVersion text-gbif-header-2 icon-button icon-material-actions overview-action-button fs-smaller-2" id="publishButton" name="publish" value="Publish" type="submit">
                        <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                            <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                        </svg>
                        <@s.text name="button.publish"/>
                    </button>
                </form>
            </#if>

            <!-- publishing a new minor version -->
        <#elseif resource.identifierStatus == "PUBLIC" && resource.isAlreadyAssignedDoi()>
            <form action="publish.do" method="post">
                <input name="r" type="hidden" value="${resource.shortname}"/>
                <textarea id="summary" name="summary" cols="40" rows="5" style="display: none"></textarea>

                <button class="confirmPublishMinorVersion text-gbif-header-2 icon-button icon-material-actions overview-action-button fs-smaller-2" id="publishButton" name="publish" value="Publish" type="submit">
                    <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                        <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                    </svg>
                    <@s.text name="button.publish"/>
                </button>
            </form>

            <!-- publishing a new version registered with GBIF -->
        <#elseif resource.status == "REGISTERED">
            <form action="publish.do" method="post">
                <input name="r" type="hidden" value="${resource.shortname}"/>
                <textarea id="summary" name="summary" cols="40" rows="5" style="display: none"></textarea>

                <button class="confirmPublishMinorVersion text-gbif-header-2 icon-button icon-material-actions overview-action-button fs-smaller-2" id="publishButton" name="publish" value="Publish" type="submit">
                    <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                        <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                    </svg>
                    <@s.text name="button.publish"/>
                </button>
            </form>
        </#if>

        <!-- first time any resource not assigned a DOI is published is always new major version -->
    <#elseif !resource.lastPublished?? && resource.identifierStatus == "UNRESERVED">
        <form action="publish.do" method="post">
            <input name="r" type="hidden" value="${resource.shortname}"/>
            <textarea id="summary" name="summary" cols="40" rows="5" style="display: none"></textarea>

            <button class="confirmPublishMajorVersionWithoutDOI text-gbif-header-2 icon-button icon-material-actions overview-action-button fs-smaller-2" id="publishButton" name="publish" value="Publish" type="submit">
                <svg viewBox="0 0 24 24" class="overview-action-button-icon">
                    <path d="M5 4v2h14V4H5zm0 10h4v6h6v-6h4l-7-7-7 7z"></path>
                </svg>
                <@s.text name="button.publish"/>
            </button>
        </form>
    <#else>
        <!-- otherwise prevent publication from happening just to be safe -->
    </#if>
</#macro>
