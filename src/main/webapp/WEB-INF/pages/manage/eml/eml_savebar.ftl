<#ftl output_format="HTML">
<#-- shown at the bottom of an EML metadata page once something has changed (inc/footer.ftl); goes inside the form.
     Discard reloads the page without the changes (data-discard: footer.ftl doesn't ask) -->
<div class="eml-savebar" id="eml-savebar" aria-hidden="true">
    <div class="container">
        <span class="eml-savebar-msg"><@s.text name="manage.metadata.savebar.unsaved"/></span>
        <a class="btn btn-sm btn-outline-secondary" href="metadata-${currentMetadataPage}.do?r=${resource.shortname}" data-discard><@s.text name="manage.metadata.savebar.discard"/></a>
        <button type="submit" id="savebar-save" class="btn btn-sm btn-gbif-solid" name="save" value="save"><@icon name="save"/> <@s.text name="button.save"/></button>
    </div>
</div>
