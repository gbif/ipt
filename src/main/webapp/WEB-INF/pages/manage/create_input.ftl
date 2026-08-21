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
<script src="${baseURL}/js/datapackage-precheck.js"></script>
<script src="${baseURL}/js/datapackage-precheck-render.js"></script>

<style>
    :root {
        --radius: 8px;
        --border-color: #ced4da;
    }

    /* path selector */
    .path-select {
        margin: 18px 0 6px;
        border: 1px solid var(--border-color);
        border-radius: 4px;
        overflow: hidden;
    }

    .path-option {
        display: flex;
        gap: 12px;
        padding: 14px 16px;
        border-bottom: 1px solid var(--border-color);
        cursor: pointer;
        align-items: flex-start;
        transition: background .1s ease;
    }

    .path-option:last-child {
        border-bottom: none;
    }

    .path-option:hover {
        background: #fafaf7;
    }

    .path-option.selected {
        background: rgba(var(--color-gbif-primary), 0.1);
    }

    .path-option .path-label {
        font-weight: 600;
        color: #575757;
        margin-bottom: 2px;
    }

    .path-option .path-desc {
        font-size: 13px;
    }

    .import-block {
        margin-top: 22px;
        padding: 16px;
        background: #fafafa;
        border: 1px solid var(--border-color);
        border-radius: 4px;
        display: none;
    }

    .import-block.show {
        display: block;
    }
</style>

<script>
    $(document).ready(function () {
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

        $("#create").on('click', displayProcessing);

        var dataPackages = ${schemasJson?no_esc};

        const datasetTypeMap = {
            "Occurrence": "occurrence",
            "Checklist": "checklist",
            "Samplingevent": "samplingevent",
            "Material entity": "materialentity",
            "Metadata": "metadata",
            "Other": "other",
            "camtrap-dp": "camtrap-dp",
            "coldp": "coldp",
            "dwc-dp": "dwc-dp"
        };

        const $file = $('#file');
        const $shortname = $('#shortname');
        const $typeSelect = $('#resourceType');
        const $importNote = $('#importNote');
        const $precheckPanel = $('#precheckPanel');
        const $pathOptions = $('.path-option');
        const $importBlock = $('#importBlock');
        const $shortnameField = $('#shortnameField');
        const $typeField = $('#typeField');
        const $createBtn = $('#create');

        let currentPath = 'blank';
        let fileChosen = false;

        $file.on('change', async function (e) {
            const file = e.target.files[0];
            fileChosen = !!file;

            if (!file) {
                validate();
                return;
            }

            $importNote.hide();

            deriveShortname(file.name);
            await detectDatasetType(file);

            validate();
        });

        function deriveShortname(filename) {
            const lower = filename.toLowerCase();
            const shortnameMatch = lower.match(/^(dwca|datapackage|eml)-([a-z0-9_-]+)-[^\/\\]+$/i);

            if (shortnameMatch && shortnameMatch[2]) {
                $shortname.val(shortnameMatch[2]);
            } else {
                $shortname.val(lower.replace(/\.[^/.]+$/, ""));
            }
        }

        async function detectDatasetType(file) {
            // Let DataPackagePrecheck have a look first (drives the precheck panel)
            const result = await DataPackagePrecheck.run(file, dataPackages);

            if (result && result.type === 'data-package') {
                renderPrecheckPanel(result, file.name, $precheckPanel.get(0));
            } else {
                $precheckPanel.empty();
            }

            // TODO: precheck should do this eventually
            // Then inspect the file contents to guess the resource type
            if (file.name.endsWith('.zip')) {
                try {
                    const zip = await JSZip.loadAsync(file);
                    const jsonPath = Object.keys(zip.files).find(path => path.endsWith('/datapackage.json') || path === 'datapackage.json');
                    const emlPath = Object.keys(zip.files).find(path => path.endsWith('/eml.xml') || path === 'eml.xml');

                    if (jsonPath) {
                        processDatapackageJson(await zip.file(jsonPath).async('string'));
                    } else if (emlPath) {
                        processEmlXml(await zip.file(emlPath).async('string'));
                    } else {
                        console.log('Neither datapackage.json nor eml.xml found in archive.');
                    }
                } catch (err) {
                    console.log('Error reading ZIP: ' + err);
                }
            } else if (file.name === 'datapackage.json') {
                processDatapackageJson(await file.text());
            } else if (file.name.endsWith('.xml')) {
                processEmlXml(await file.text());
            } else {
                console.log('Unsupported file type. Please upload datapackage.json, eml.xml, or a ZIP archive.');
            }
        }

        function processDatapackageJson(jsonText) {
            try {
                const json = JSON.parse(jsonText);
                const profile = (json.profile || '').toLowerCase();
                const resources = json.resources || [];

                let datasetType = '';
                if (profile.includes('dwc-dp')) {
                    datasetType = datasetTypeMap['dwc-dp'];
                } else if (profile.includes('camtrap-dp')) {
                    datasetType = datasetTypeMap['camtrap-dp'];
                } else if (profile.includes('coldp')) {
                    datasetType = datasetTypeMap['coldp'];
                } else if (profile.includes('data-package')) {
                    const identifier = resources[0]?.schema?.identifier?.toLowerCase() || '';

                    if (identifier.includes('dwc-dp')) {
                        datasetType = datasetTypeMap['dwc-dp'];
                    } else if (identifier.includes('coldp')) {
                        datasetType = datasetTypeMap['coldp'];
                    }
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
            const hasOption = datasetType && $typeSelect.find('option[value="' + datasetType + '"]').length;

            $typeSelect.val(hasOption ? datasetType : 'other').trigger('change');
            if (!hasOption) console.log('No matching dataset type found.');

            validate();
        }

        function updatePathUI() {
            $pathOptions.each(function () {
                $(this).toggleClass('selected', $(this).data('path') === currentPath);
            });

            if (currentPath === 'blank') {
                $importBlock.removeClass('show');
                $importNote.hide();
                $file.prop('disabled', true);
            } else {
                $importBlock.addClass('show');
                fileChosen ? $importNote.hide() : $importNote.show();
                $file.prop('disabled', false);
            }

            $shortnameField.show();
            $typeField.show();

            validate();
        }

        $pathOptions.on('click', function () {
            currentPath = $(this).data('path');
            $(this).find('input[type=radio]').prop('checked', true);
            updatePathUI();
        });

        $typeSelect.on('change', validate);
        $shortname.on('input', validate);

        function validate() {
            let valid = true;

            if (!$shortname.val().trim()) valid = false;
            if (!$typeSelect.val()) valid = false;
            if (currentPath !== 'blank' && !fileChosen) valid = false;

            $createBtn.prop('disabled', !valid);
        }

        updatePathUI();
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

            <div class="body-pad">
                <div class="path-select" role="radiogroup" aria-label="Resource start method">
                    <label class="path-option selected" data-path="blank">
                        <input type="radio" class="form-check-input flex-shrink-0" name="startMethod" value="blank" checked>
                        <div>
                            <div class="path-label"><@s.text name="manage.resource.create.option.blank"/></div>
                            <div class="path-desc text-muted"><@s.text name="manage.resource.create.option.blank.help"/></div>
                        </div>
                    </label>

                    <label class="path-option" data-path="archive">
                        <input type="radio" class="form-check-input flex-shrink-0" name="startMethod" value="archive">
                        <div>
                            <div class="path-label"><@s.text name="manage.resource.create.option.archive"/></div>
                            <div class="path-desc text-muted"><@s.text name="manage.resource.create.option.archive.help"/></div>
                        </div>
                    </label>
                </div>

                <div class="import-block" id="importBlock">
                    <div class="file-row">
                        <@s.fielderror cssClass="fielderror" fieldName="file"/>
                        <label for="file" class="form-label"><@s.text name="manage.resource.create.file"/> <span class="text-gbif-danger">*</span> </label>
                        <@s.file name="file" cssClass="form-control" key="manage.resource.create.file" />
                    </div>

                    <div id="precheckPanel"></div>
                </div>

                <div id="importNote" class="border rounded px-3 py-1 mt-3" style="display: none;">
                    <div class="simpleCallout">
                        <div class="simpleCallout-inner">
                            <div class="simpleCalloutInfo simpleCalloutInfo-neutral">
                                <div class="simpleCalloutIcon">
                                    <i class="bi bi-info-circle text-gbif-primary"></i>
                                </div>
                                <div class="simpleCalloutMeta">
                                    <div class="simpleCalloutMessage">
                                        <@s.text name="manage.resource.create.import.note"/>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

            </div>

            <div class="row g-3 mt-0 mb-2">
                <div class="col-md-6" id="shortnameField">
                    <@input name="shortname" i18nkey="resource.shortname" help="i18n" errorfield="resource.shortname" requiredField=true size=40/>
                </div>

                <div class="col-md-6" id="typeField">
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
            </div>
        </div>
    </main>
</form>

<#include "/WEB-INF/pages/inc/footer.ftl">
