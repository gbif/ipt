<#--
  Rich text editor for EML text fields stored as DocBook (description, purpose, introduction, getting started,
  acknowledgements). See js/docbook/docbook-editor.js.

  Call <@docBookEditorAssets/> once in the page head, then <@docBookEditor .../> for every field.
-->
<#macro docBookEditorAssets>
    <link rel="stylesheet" href="${baseURL}/styles/prosemirror/prosemirror.css">
    <script src="${baseURL}/js/prosemirror/prosemirror-bundle.min.js"></script>
    <script src="${baseURL}/js/docbook/docbook-editor.js"></script>
    <script>
        IptDocBookEditor.setLabels({
            bold: "${action.getText('manage.metadata.textEditor.bold')?js_string}",
            subscript: "${action.getText('manage.metadata.textEditor.subscript')?js_string}",
            superscript: "${action.getText('manage.metadata.textEditor.superscript')?js_string}",
            link: "${action.getText('manage.metadata.textEditor.link')?js_string}",
            heading: "${action.getText('manage.metadata.textEditor.heading')?js_string}",
            bulletList: "${action.getText('manage.metadata.textEditor.bulletList')?js_string}",
            orderedList: "${action.getText('manage.metadata.textEditor.orderedList')?js_string}",
            codeBlock: "${action.getText('manage.metadata.textEditor.codeBlock')?js_string}",
            undo: "${action.getText('manage.metadata.textEditor.undo')?js_string}",
            redo: "${action.getText('manage.metadata.textEditor.redo')?js_string}",
            linkApply: "${action.getText('manage.metadata.textEditor.link.apply')?js_string}",
            linkRemove: "${action.getText('manage.metadata.textEditor.link.remove')?js_string}",
            linkCancel: "${action.getText('button.cancel')?js_string}",
            linkInvalid: "${action.getText('manage.metadata.textEditor.link.invalid')?js_string}"
        });
    </script>
</#macro>

<#--
  name:  request parameter receiving the DocBook value, e.g. eml.description
  id:    id of the hidden input; the editor is rendered into "${id}-editor"
  value: current DocBook value
  label: accessible name of the editor
-->
<#macro docBookEditor name id value="" label="">
    <div id="${id}-editor"></div>
    <#-- prefilled, so the value is kept if the editor fails to load -->
    <input id="${id}" type="hidden" name="${name}" value="${value?html}">
    <script>
        IptDocBookEditor.create({
            mount: document.getElementById("${id?js_string}-editor"),
            input: document.getElementById("${id?js_string}"),
            value: "${value?js_string}",
            ariaLabel: "${label?js_string}"
        });
    </script>
</#macro>
