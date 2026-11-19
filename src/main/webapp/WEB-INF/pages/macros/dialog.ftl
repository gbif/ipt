<#-- Dialogs (styles/dialogs.css): the parts every modal shares, so the templates only write the body and the buttons.

     <#assign dlgTitle><@s.text name="..."/></#assign>
     <@dialog id="x" title=dlgTitle tone="danger" alert=true>
         <div class="modal-body">...</div>
         <div class="modal-footer"><@dialogCancel/> ...</div>
     </@dialog>

     alert     true: an alert dialog (role="alertdialog", 440 px) that asks one question; false: a task dialog for a form
               or content
     tone      danger, warn, info or ok: shows an icon tile in front of the title, coloured by tone
     iconName  the tile's Material icon (default: the tone's, e.g. "delete" for danger)
     sub       a subtitle under the title, usually the resource title
     size      "wide" for previews (96vw x 94vh)

     Name the title variable dlgTitle, not t: some templates (contacts.ftl) have a function t(). -->
<#include "/WEB-INF/pages/macros/icon.ftl"/>
<#macro dialog id title tone="" alert=false sub="" size="" iconName="">
    <div id="${id}" class="modal fade" tabindex="-1" role="${alert?then('alertdialog', 'dialog')}" aria-labelledby="${id}-title" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered<#if alert> dlg-alert</#if><#if size == "wide"> dlg-wide</#if><#if !alert && size != "wide"> modal-dialog-scrollable</#if>">
            <div class="modal-content">
                <div class="modal-header">
                    <#if tone?has_content>
                        <span class="dlg-tone ${tone}" aria-hidden="true"><@icon name=iconName?has_content?then(iconName, {"danger": "delete", "warn": "warning", "info": "info", "ok": "check_circle"}[tone]!"info")/></span>
                    </#if>
                    <div class="dlg-titles">
                        <h2 class="modal-title" id="${id}-title">${title}</h2>
                        <#if sub?has_content><div class="dlg-sub">${sub}</div></#if>
                    </div>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="<@s.text name='button.close'/>"></button>
                </div>
                <#nested>
            </div>
        </div>
    </div>
</#macro>

<#-- the Cancel (or Close) button of a dialog footer -->
<#macro dialogCancel key="button.cancel">
    <button type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal"><@s.text name=key/></button>
</#macro>
