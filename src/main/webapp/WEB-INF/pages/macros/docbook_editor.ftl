<#--
  Rich text editor for EML text fields stored as DocBook (description, purpose, introduction, getting started,
  acknowledgements). See js/docbook/docbook-editor.js.

  Call <@docBookEditorAssets/> once in the page head, then <@docBookEditor .../> for every field.

  Translated fields (only the description so far): also call <@translationAssets/> once in the page head (after
  <@docBookEditorAssets/>, on a MetadataAction page), then <@docBookEditor ... translatable=true/>.
  See js/docbook/multilingual-fields.js.

  To show DocBook read-only (e.g. on the public resource page), call <@docBookViewerAssets/>, put the value in a
  data-docbook attribute (data-docbook="${eml.description}", escaped by the HTML output format) and call
  IptDocBookEditor.renderAll() once the page is loaded.
-->
<#macro docBookViewerAssets>
    <script src="${baseURL}/js/prosemirror/prosemirror-bundle.min.js"></script>
    <script src="${baseURL}/js/docbook/docbook-editor.js"></script>
    <script src="${baseURL}/js/docbook/multilingual-fields.js"></script>
</#macro>

<#macro docBookEditorAssets>
    <link rel="stylesheet" href="${baseURL}/styles/prosemirror/prosemirror.css">
    <script src="${baseURL}/js/prosemirror/prosemirror-bundle.min.js"></script>
    <script src="${baseURL}/js/docbook/docbook-editor.js"></script>
    <script>
        IptDocBookEditor.setLabels({
            bold: "${action.getText('manage.metadata.textEditor.bold')?js_string?no_esc}",
            subscript: "${action.getText('manage.metadata.textEditor.subscript')?js_string?no_esc}",
            superscript: "${action.getText('manage.metadata.textEditor.superscript')?js_string?no_esc}",
            link: "${action.getText('manage.metadata.textEditor.link')?js_string?no_esc}",
            heading: "${action.getText('manage.metadata.textEditor.heading')?js_string?no_esc}",
            bulletList: "${action.getText('manage.metadata.textEditor.bulletList')?js_string?no_esc}",
            orderedList: "${action.getText('manage.metadata.textEditor.orderedList')?js_string?no_esc}",
            codeBlock: "${action.getText('manage.metadata.textEditor.codeBlock')?js_string?no_esc}",
            undo: "${action.getText('manage.metadata.textEditor.undo')?js_string?no_esc}",
            redo: "${action.getText('manage.metadata.textEditor.redo')?js_string?no_esc}",
            linkApply: "${action.getText('manage.metadata.textEditor.link.apply')?js_string?no_esc}",
            linkRemove: "${action.getText('manage.metadata.textEditor.link.remove')?js_string?no_esc}",
            linkCancel: "${action.getText('button.cancel')?js_string?no_esc}",
            linkInvalid: "${action.getText('manage.metadata.textEditor.link.invalid')?js_string?no_esc}"
        });
    </script>
</#macro>

<#--
  loadSelect2: load select2 (searchable "add translation" list); false on pages that load it already
-->
<#macro translationAssets loadSelect2=true>
    <#if loadSelect2>
        <link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
        <link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
        <script src="${baseURL}/js/select2/select2-4.0.13.min.js"></script>
    </#if>
    <script src="${baseURL}/js/docbook/multilingual-fields.js"></script>
    <script>
        IptMultilingualFields.setLabels({
            addTranslation: "${action.getText('manage.metadata.translations.add')?js_string?no_esc}",
            removeTranslation: "${action.getText('manage.metadata.translations.remove')?js_string?no_esc}",
            metadataLanguage: "${action.getText('eml.metadataLanguage')?js_string?no_esc}",
            confirmRemove: "${action.getText('manage.metadata.translations.remove.confirm')?js_string?no_esc}",
            confirmRemoveText: "${action.getText('manage.metadata.translations.remove.confirm.text')?js_string?no_esc}",
            confirmRemoveButton: "${action.getText('button.remove')?js_string?no_esc}",
            cancel: "${action.getText('button.cancel')?js_string?no_esc}",
            baseUrl: "${baseURL?js_string?no_esc}",
            translationOf: "${action.getText('manage.metadata.translations.fieldLabel')?js_string?no_esc}"
        });
        <#-- JSON from Gson, which escapes <, > and & -->
        window.iptTranslations = IptMultilingualFields.create(${action.translationSettingsJson?no_esc});
    </script>
</#macro>

<#--
  name:  request parameter receiving the DocBook value, e.g. eml.description
  id:    id of the hidden input; the editor is rendered into "${id}-editor"
  value: current DocBook value
  label: accessible name of the editor
  translatable: one editor per language, see <@translationAssets/>
  translationHint: shown under the language tabs while there are translations, {0} = metadata language name
-->
<#macro docBookEditor name id value="" label="" translatable=false translationHint="">
    <div id="${id}-editor"></div>
    <#-- prefilled, so the value is kept if the editor fails to load -->
    <input id="${id}" type="hidden" name="${name}" value="${value}">
    <#-- e.g. markup removed by SanitizeHtmlFilter; d-block, as there is no form control to mark is-invalid -->
    <@s.fielderror cssClass="invalid-feedback d-block list-unstyled my-1" fieldName="${name}"/>
    <script>
        <#if translatable>
        window.iptTranslations.docBookField({
            mount: document.getElementById("${id?js_string?no_esc}-editor"),
            input: document.getElementById("${id?js_string?no_esc}"),
            value: "${value?js_string?no_esc}",
            label: "${label?js_string?no_esc}",
            hint: "${translationHint?js_string?no_esc}"
        });
        <#else>
        IptDocBookEditor.create({
            mount: document.getElementById("${id?js_string?no_esc}-editor"),
            input: document.getElementById("${id?js_string?no_esc}"),
            value: "${value?js_string?no_esc}",
            ariaLabel: "${label?js_string?no_esc}"
        });
        </#if>
    </script>
</#macro>
