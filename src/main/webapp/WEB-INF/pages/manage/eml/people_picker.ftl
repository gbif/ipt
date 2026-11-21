<#ftl output_format="HTML">
<#-- "Copy from another resource" dialog of the contacts and project pages, run by js/people.js -->
    <div id="party-picker" class="modal fade" tabindex="-1" aria-labelledby="party-picker-title" aria-hidden="true">
        <div class="modal-dialog modal-dialog-scrollable">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title fs-6" id="party-picker-title"><@s.text name="eml.party.picker.title"/></h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="${action.getText('button.cancel')}"></button>
                </div>
                <div class="modal-body">
                    <input type="search" class="form-control mb-2" id="party-picker-search" placeholder="${action.getText('eml.party.picker.search')}" aria-label="${action.getText('eml.party.picker.search')}"/>
                    <div class="party-picker-results" id="party-picker-results" role="listbox" aria-labelledby="party-picker-title"></div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal"><@s.text name="button.cancel"/></button>
                    <button type="button" class="btn btn-sm btn-gbif-solid" id="party-picker-ok" disabled><@s.text name="button.copy"/></button>
                </div>
            </div>
        </div>
    </div>
