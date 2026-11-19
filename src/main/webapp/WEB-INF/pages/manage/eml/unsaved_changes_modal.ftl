<#-- asked when leaving a metadata page with changes; inc/footer.ftl opens it and follows the link on Leave -->
<#include "/WEB-INF/pages/macros/dialog.ftl"/>
<#assign dlgTitle><@s.text name="dialog.unsaved.title"/></#assign>
<@dialog id="unsavedChangesModal" title=dlgTitle tone="warn" alert=true>
    <div class="modal-body">
        <@s.text name="dialog.unsaved.text"/>
    </div>
    <div class="modal-footer">
        <button id="leaveButton" type="button" class="btn btn-sm btn-outline-gbif-danger me-auto" data-bs-dismiss="modal"><@s.text name="dialog.unsaved.leave"/></button>
        <button id="stayButton" type="button" autofocus class="btn btn-sm btn-gbif-solid" data-bs-dismiss="modal"><@s.text name="manage.metadata.unsavedChanges.button.stay"/></button>
    </div>
</@dialog>
