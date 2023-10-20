<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.DataPackageMetadataAction" -->
<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name='manage.datapackagemetadata.geographic.title'/></title>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>
    <script>
        $(document).ready(function(){
            // scroll to the error if present
            var invalidElements = $(".is-invalid");

            if (invalidElements !== undefined && invalidElements.length > 0) {
                var invalidElement = invalidElements.first();
                var pos = invalidElement.offset().top - 100;
                // scroll to the element
                $('body, html').animate({scrollTop: pos});
            }

            var $customScopeCheckbox = $("#resource\\.customGeocoverage");
            var isCustomScopeEnabled = $customScopeCheckbox.is(":checked");

            if (isCustomScopeEnabled) {
                $("#actual-metadata-block").show();
                $("#inferred-metadata-block").hide();
                $("#preview-links").hide();
                $("#custom-data").show();
            } else {
                $("#custom-data").hide();
            }

            $customScopeCheckbox.click(function() {
                if ($(this).is(":checked")) {
                    $("#inferred-metadata-block").hide();
                    $("#preview-links").hide();
                    $("#custom-data").show();
                } else {
                    $("#inferred-metadata-block").show();
                    $("#preview-links").show();
                    $("#custom-data").hide();
                }
            });

            var customGeoJson;

            $('#buttonUpload').on('click', function () {
                const fileInput = $('#custom-data-input')[0];
                const file = fileInput.files[0];
                if (file) {
                    const reader = new FileReader();

                    reader.onload = function (e) {
                        try {
                            const json = JSON.parse(e.target.result);
                            const jsonString = JSON.stringify(json, null, 0);
                            customGeoJson = jsonString;

                            $("#custom-data-textarea").text(jsonString);
                        } catch (error) {
                            $('#jsonContent').text("Invalid JSON file.");
                        }
                    };

                    reader.readAsText(file);
                } else {
                    $('#jsonContent').text("Please select a JSON file first.");
                }
            });

            $("#geographic-scope-form").submit(function (e) {
                e.preventDefault();
                var formData = $(this).serializeArray();

                // Check if the form submits custom scope data
                var isCustomScope = formData.some(function(input) {
                    return input.name === "resource.customGeocoverage" && input.value === 'true';
                });

                // Create a new array to store the parameters
                var filteredData = [];
                filteredData.push({ name: "Save", value: "save" });

                if (isCustomScope) {
                    filteredData.push({ name: "customGeoJson", value: customGeoJson });
                } else {
                    filteredData.push({ name: "metadata.spatial.type", value: "Polygon" });
                    filteredData.push({ name: "metadata.spatial.coordinates", value: "Polygon" });
                }

                // Remove parameters if custom scope is true
                formData.forEach(function(input) {
                    if (!input.name.startsWith("metadata.spatial.bbox") || !isCustomScope) {
                        filteredData.push(input);
                    }
                });

                // Serialize the modified data back into a query string
                var modifiedData = $.param(filteredData);

                // Re-submit form
                $.ajax({
                    type: 'POST',
                    url: 'camtrap-metadata-${section}.do',
                    data: modifiedData,
                    success: function() {
                        window.location.href = "${baseURL}/manage/camtrap-metadata-taxonomic.do?r=${resource.shortname}";
                    }
                });
            });


            $("#file-selector").click(function() {
                $("#custom-data-input").click();
            });

            $("#custom-data-input").change(function() {
                const fileNameDisplay = $("#file-name");
                const fileInput = $("#custom-data-input")[0];

                if (fileInput.files.length > 0) {
                    fileNameDisplay.text("Selected file: " + fileInput.files[0].name);
                } else {
                    fileNameDisplay.text("");
                }
            });
        });
    </script>
    <#assign currentMenu="manage"/>
    <#assign currentMetadataPage = "geographic"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <form id="geographic-scope-form" class="needs-validation" action="camtrap-metadata-${section}.do" method="post" novalidate>
        <div class="container-fluid bg-body border-bottom">
            <div class="container bg-body border rounded-2 mb-4">
                <div class="container my-3 p-3">
                    <div class="text-center fs-smaller">
                        <div class="text-center fs-smaller">
                            <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                                <ol class="breadcrumb justify-content-center mb-0">
                                    <li class="breadcrumb-item"><a href="/manage/"><@s.text name="breadcrumb.manage"/></a></li>
                                    <li class="breadcrumb-item"><a href="resource?r=${resource.shortname}"><@s.text name="breadcrumb.manage.overview"/></a></li>
                                    <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.manage.overview.metadata"/></li>
                                </ol>
                            </nav>
                        </div>
                    </div>

                    <div class="text-center">
                        <h1 class="py-2 mb-0 text-gbif-header fs-2 fw-normal">
                            <@s.text name='manage.datapackagemetadata.geographic.title'/>
                        </h1>
                    </div>

                    <div class="text-center fs-smaller">
                        <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">
                            <#if resource.title?has_content>
                                ${resource.title}
                            <#else>
                                ${resource.shortname}
                            </#if>
                        </a>
                    </div>

                    <div class="text-center mt-2">
                        <@s.submit cssClass="button btn btn-sm btn-outline-gbif-primary top-button" name="save" key="button.save"/>
                        <@s.submit cssClass="button btn btn-sm btn-outline-secondary top-button" name="cancel" key="button.back"/>
                    </div>
                </div>
            </div>
        </div>

        <#--        <#include "metadata_section_select.ftl"/>-->

        <div class="container-fluid bg-body">
            <div class="container bd-layout main-content-container">
                <main class="bd-main bd-main">
                    <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                        <#include "metadata_sidebar.ftl"/>
                    </div>

                    <div class="bd-content">
                        <div class="my-md-3 ps-3 py-3">
                            <p class="mb-2"><@s.text name="datapackagemetadata.geographic.intro"/></p>

                            <div id="actual-metadata-block" class="mt-3">
                                <div>Current saved metadata</div>
                                <div class="table-responsive border rounded p-3">
                                    <table class="text-smaller table table-sm table-borderless mb-0">
                                        <tr>
                                            <th class="col-4"><@s.text name='datapackagemetadata.geographic.type'/></th>
                                            <td>
                                                ${(metadata.spatial.type)!"-"}
                                            </td>
                                        </tr>
                                        <tr>
                                            <th class="col-4"><@s.text name='datapackagemetadata.geographic.boundingCoordinates'/></th>
                                            <td>
                                                <#if (metadata.spatial.coordinates)??>
                                                    [${metadata.spatial.coordinates}]
                                                <#else>
                                                    -
                                                </#if>
                                            </td>
                                        </tr>
                                    </table>
                                </div>
                            </div>

                            <div class="mt-4">
                                <@checkbox name="resource.customGeocoverage" i18nkey="datapackagemetadata.geographic.custom" value="${resource.customGeocoverage?c}" />
                            </div>

                            <div id="custom-data" class="mt-4">
                                <div class="col-md-6">
                                    <@s.file id="custom-data-input" cssClass="form-control form-control-sm my-1 d-none" />
                                </div>

                                <textarea id="custom-data-textarea" name="text" placeholder="Input custom Geo JSON object" class="form-control" style="border-bottom: 1px dashed #ced4da;border-bottom-right-radius: 0;border-bottom-left-radius: 0;"></textarea>
                                <div id="file-selector" class="border-bottom border-start border-end rounded py-2 px-3 fs-smaller text-muted" style="cursor: pointer;border-color: #ced4da !important;border-top-right-radius: 0 !important;border-top-left-radius: 0 !important;">
                                    Or upload file
                                    <span id="file-name"></span>
                                    <a href="#" class="action-link-button action-link-button-primary fs-smaller" id="buttonUpload">
                                        <svg class="action-link-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                            <path d="M9 16h6v-6h4l-7-7-7 7h4zm-4 2h14v2H5z"></path>
                                        </svg>
                                        <@s.text name="button.upload"/>
                                    </a>
                                </div>

                                <pre id="jsonContent" class="border rounded p-3 mt-3 fs-smaller-2" style="background: #f6f8fa; display: none;"></pre>
                            </div>


                            <#assign geographicScopeMetadataIsInferred = (inferredMetadata.inferredGeographicScope)?? && inferredMetadata.inferredGeographicScope.inferred && !inferredMetadata.inferredGeographicScope.errors?has_content/>

                            <div id="inferred-metadata-block" class="mt-4">
                                <div class="row">
                                    <div class="col-md-6">Inferred metadata (not saved)</div>
                                    <div id="preview-links" class="col-md-6">
                                        <div id="dateInferred" class="text-smaller mt-0 d-flex justify-content-end">
                                            <span class="fs-smaller-2" style="padding: 4px;">${(inferredMetadata.lastModified?datetime?string.medium)!}&nbsp;</span>
                                            <a href="camtrap-metadata-geographic.do?r=${resource.shortname}&amp;reinferMetadata=true" class="metadata-action-link">
                                        <span>
                                            <svg class="link-icon" viewBox="0 0 24 24">
                                                <path d="m19 8-4 4h3c0 3.31-2.69 6-6 6-1.01 0-1.97-.25-2.8-.7l-1.46 1.46C8.97 19.54 10.43 20 12 20c4.42 0 8-3.58 8-8h3l-4-4zM6 12c0-3.31 2.69-6 6-6 1.01 0 1.97.25 2.8.7l1.46-1.46C15.03 4.46 13.57 4 12 4c-4.42 0-8 3.58-8 8H1l4 4 4-4H6z"></path>
                                            </svg>
                                        </span>
                                                <span><@s.text name="datapackagemetadata.reinfer"/></span>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                                <div class="table-responsive border rounded p-3">
                                    <table class="text-smaller table table-sm table-borderless mb-0">
                                        <tr>
                                            <th class="col-4"><@s.text name='datapackagemetadata.geographic.type'/></th>
                                            <td>
                                                <#if geographicScopeMetadataIsInferred>
                                                    Polygon
                                                <#else>
                                                    -
                                                </#if>
                                            </td>
                                        </tr>
                                        <tr>
                                            <th class="col-4"><@s.text name='datapackagemetadata.geographic.boundingCoordinates'/></th>
                                            <td>
                                                <#if geographicScopeMetadataIsInferred>
                                                    [[${inferredMetadata.inferredGeographicScope.minLongitude!}, ${inferredMetadata.inferredGeographicScope.minLatitude!}, ${inferredMetadata.inferredGeographicScope.maxLongitude!}, ${inferredMetadata.inferredGeographicScope.maxLatitude!}]]
                                                <#else>
                                                    -
                                                </#if>
                                            </td>
                                        </tr>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                </main>
            </div>
        </div>
    </form>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
