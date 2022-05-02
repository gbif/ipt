<#-- @ftlvariable name="" type="org.gbif.ipt.action.AboutAction" -->
<#if hostingOrganisation?? && hostingOrganisation.name??>
    <p>${getText("about.installation")} ${hostingOrganisation.name}</p>

    <p>${getText("about.variables")}:</p>

    <div class="row pb-2 text-smaller">
        <div class="col-lg-6">
            <strong>description</strong>
        </div>
        <div class="col-lg-6">
            ${hostingOrganisation.description!"--"}
        </div>
    </div>

    <div class="row pb-2 text-smaller">
        <div class="col-lg-6">
            <strong>name</strong>
        </div>
        <div class="col-lg-6">${hostingOrganisation.name!"--"}</div>
    </div>

    <div class="row pb-2 text-smaller">
        <div class="col-lg-6">
            <strong>alias</strong>
        </div>
        <div class="col-lg-6">${hostingOrganisation.alias!"--"}</div>
    </div>

    <div class="row pb-2 text-smaller">
        <div class="col-lg-6">
            <strong>homepageURL</strong>
        </div>
        <div class="col-lg-6">${hostingOrganisation.homepageURL!"--"}</div>
    </div>

    <div class="row pb-2 text-smaller">
        <div class="col-lg-6">
            <strong>primaryContactType</strong>
        </div>
        <div class="col-lg-6">${hostingOrganisation.primaryContactType!"--"}</div>
    </div>

    <div class="row pb-2 text-smaller">
        <div class="col-lg-6">
            <strong>primaryContactName</strong>
        </div>
        <div class="col-lg-6">${hostingOrganisation.primaryContactName!"--"}</div>
    </div>

    <div class="row pb-2 text-smaller">
        <div class="col-lg-6">
            <strong>primaryContactDescription</strong>
        </div>
        <div class="col-lg-6">${hostingOrganisation.primaryContactDescription!"--"}</div>
    </div>

    <div class="row pb-2 text-smaller">
        <div class="col-lg-6">
            <strong>primaryContactAddress</strong>
        </div>
        <div class="col-lg-6">${hostingOrganisation.primaryContactAddress!"--"}</div>
    </div>

    <div class="row pb-2 text-smaller">
        <div class="col-lg-6">
            <strong>primaryContactEmail</strong>
        </div>
        <div class="col-lg-6">${hostingOrganisation.primaryContactEmail!"--"}</div>
    </div>

    <div class="row pb-2 text-smaller">
        <div class="col-lg-6">
            <strong>primaryContactPhone</strong>
        </div>
        <div class="col-lg-6">${hostingOrganisation.primaryContactPhone!"--"}</div>
    </div>

    <div class="row pb-2 text-smaller">
        <div class="col-lg-6">
            <strong>nodeKey</strong>
        </div>
        <div class="col-lg-6">${hostingOrganisation.nodeKey!"--"}</div>
    </div>

    <div class="row pb-2 text-smaller">
        <div class="col-lg-6">
            <strong>nodeName</strong>
        </div>
        <div class="col-lg-6">${hostingOrganisation.nodeName!"--"}</div>
    </div>

    <div class="row pb-2 text-smaller">
        <div class="col-lg-6">
            <strong>nodeContactEmail</strong>
        </div>
        <div class="col-lg-6">${hostingOrganisation.nodeContactEmail!"--"}</div>
    </div>
<#else>
    <p class="text-center">${getText("about.notRegistered")}</p>
</#if>
