<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="manage.resource.create.title"/></title>
 <#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>
<#include "/WEB-INF/pages/macros/popover.ftl">
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
<script src="${baseURL}/js/select2/select2-4.0.13.full.min.js"></script>
<script src="${baseURL}/js/jszip/jszip-3.10.1.min.js"></script>

<script>
    $(document).ready(function(){
        $("#resourceType").select2({
            placeholder: '${action.getText("manage.resource.create.coreType.selection")?js_string}',
            language: {
                noResults: function () {
                    return '${selectNoResultsFound}';
                }
            },
            width: "100%",
            minimumResultsForSearch: 15,
            theme: 'bootstrap4'
        });

        const datasetTypeMap = {
            "Occurrence": "occurrence",
            "Checklist": "checklist",
            "Samplingevent": "samplingevent",
            "Material entity": "materialentity",
            "Metadata": "metadata",
            "Other": "other",
            "camtrap-dp": "camtrap-dp",
            "coldp": "coldp"
        };

        $('#file').on('change', function (event) {
            const file = event.target.files[0];
            if (!file) return;

            var filename = file.name.toLowerCase();
            var shortnameMatch = filename.match(/^(dwca|datapackage|eml)-([a-z0-9_-]+)-[^\/\\]+$/i);

            if (shortnameMatch && shortnameMatch[2]) {
                $('#shortname').val(shortnameMatch[2]);
            }

            if (file.name.endsWith('.zip')) {
                JSZip.loadAsync(file).then(function (zip) {
                    const jsonFile = zip.file('datapackage.json');
                    const emlFile = zip.file('eml.xml');

                    if (jsonFile) {
                        jsonFile.async('string').then(processDatapackageJson);
                    } else if (emlFile) {
                        emlFile.async('string').then(processEmlXml);
                    } else {
                        console.log('Neither datapackage.json nor eml.xml found in archive.');
                    }
                }).catch(function (err) {
                    console.log('Error reading ZIP: ' + err);
                });
            } else if (file.name === 'datapackage.json') {
                const reader = new FileReader();
                reader.onload = function (e) {
                    processDatapackageJson(e.target.result);
                };
                reader.readAsText(file);
            } else if (file.name.endsWith('.xml')) {
                const reader = new FileReader();
                reader.onload = function (e) {
                    processEmlXml(e.target.result);
                };
                reader.readAsText(file);
            } else {
                console.log('Unsupported file type. Please upload datapackage.json, eml.xml, or a ZIP archive.');
            }
        });

        function processDatapackageJson(jsonText) {
            try {
                const json = JSON.parse(jsonText);
                const profile = (json.profile || '').toLowerCase();
                const resources = (json.resources || []);

                let datasetType = '';
                if (profile.includes('camtrap-dp')) {
                    datasetType = datasetTypeMap['camtrap-dp'];
                } else if (profile.includes('coldp')) {
                    datasetType = datasetTypeMap['coldp'];
                } else if (profile.includes('data-package')) {
                    resources.forEach(function(item) {
                        if (item.schema.includes('coldp')) {
                            datasetType = datasetTypeMap['coldp'];
                        }
                    });
                }

                applyDatasetType(datasetType);
            } catch (e) {
                console.log('Invalid datapackage.json: ' + e.message);
            }
        }

        function processEmlXml(xmlText) {
            const parser = new DOMParser();
            const xmlDoc = parser.parseFromString(xmlText, 'application/xml');
            const keywords = xmlDoc.getElementsByTagName('keyword');
            let datasetType = '';

            for (let i = 0; i < keywords.length; i++) {
                const keyword = keywords[i].textContent.trim();
                if (datasetTypeMap[keyword]) {
                    datasetType = datasetTypeMap[keyword];
                    break;
                }
            }

            applyDatasetType(datasetType);
        }

        function applyDatasetType(datasetType) {
            if (datasetType && $('#resourceType').find('option[value="' + datasetType + '"]').length) {
                $('#resourceType').val(datasetType).trigger('change');
            } else {
                console.log('No matching dataset type found.');
                $('#resourceType').val('other').trigger('change');
            }
        }
    });
</script>

<div class="container px-0">
    <#include "/WEB-INF/pages/inc/action_alerts.ftl">
</div>

<form id="create-form" class="needs-validation" action="create.do" method="post" enctype="multipart/form-data" novalidate>
    <div class="container-fluid bg-body border-bottom">
        <div class="container bg-body border rounded-2 mb-4">
            <div class="container my-3 p-3">
                <div class="text-center fs-smaller">
                    <@s.text name="menu.manage.short"/>
                </div>

                <div class="text-center">
                    <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                        <@s.text name="manage.resource.create.title"/>
                    </h1>

                    <#if (organisations?size==0)>
                        <div class="text-smaller text-gbif-danger">
                            <@s.text name="manage.resource.create.forbidden"/>
                        </div>
                    </#if>

                    <div class="mt-2">
                        <#if (organisations?size>0) >
                            <@s.submit form="create-form" cssClass="btn btn-sm btn-outline-gbif-primary top-button" name="create" key="button.create"/>
                        </#if>
                        <a href="${baseURL}/manage/" class="btn btn-sm btn-outline-secondary top-button">
                            <@s.text name="button.cancel"/>
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <main class="container main-content-container">
        <div class="my-3 p-3">
            <p class="pt-2"><@s.text name="manage.resource.create.intro"/></p>

            <script>
                $(document).ready(function() {
                    /** This function updates the map each time the global coverage checkbox is checked or unchecked  */
                    $(":checkbox").click(function() {
                        if($("#importDwca").is(":checked")) {
                            $("#import-dwca-section").slideDown('slow');
                        } else {
                            $("#file").attr("value", '');
                            $("#import-dwca-section").slideUp('slow');
                        }
                    });
                    $("#import-dwca-section").slideUp('fast');
                    $("#create").on("click", displayProcessing);
                });
            </script>

            <div class="row g-3 mt-0 mb-2">
                <div class="col-md-6">
                    <@input name="shortname" i18nkey="resource.shortname" help="i18n" errorfield="resource.shortname" requiredField=true size=40/>
                </div>

                <div class="col-md-6">
                    <div>
                        <div class="d-flex text-smaller">
                            <@popoverPropertyInfo "manage.resource.create.coreType.help" />
                            <label for="resourceType" class="form-label px-1">
                                <@s.text name='resource.coreType'/> <span class="text-gbif-danger">*</span>
                            </label>
                        </div>
                        <select name="resourceType" id="resourceType" size="1" class="form-select" required>
                            <option value="" selected="selected"><@s.text name="manage.resource.create.coreType.selection"/></option>
                            <optgroup label="<@s.text name='manage.resource.create.coreType.dwca'/>">
                                <#list types! as typeValue, typeDisplayValue>
                                    <option value="${typeValue}">${typeDisplayValue}</option>
                                </#list>
                            </optgroup>
                            <#if dataPackageTypes?has_content>
                                <optgroup label="<@s.text name='manage.resource.create.coreType.dp'/>">
                                    <#list dataPackageTypes! as typeValue, typeDisplayValue>
                                        <option value="${typeValue}">${typeDisplayValue}</option>
                                    </#list>
                                </optgroup>
                            </#if>
                        </select>
                        <@s.fielderror id="field-error-resourceType" cssClass="invalid-feedback list-unstyled field-error my-1" fieldName="resourceType"/>
                    </div>
                </div>

                <div class="col-12">
                    <@checkbox name="importDwca" help="i18n" i18nkey="manage.resource.create.archive"/>
                </div>

                <div id="import-dwca-section" class="col-md-6">
                    <@s.fielderror cssClass="fielderror" fieldName="file"/>
                    <label for="file" class="form-label"><@s.text name="manage.resource.create.file"/>: </label>
                    <@s.file name="file" cssClass="form-control" key="manage.resource.create.file" />
                </div>
            </div>
        </div>
    </main>
</form>

<#include "/WEB-INF/pages/inc/footer.ftl">
