<#ftl output_format="HTML">
<#--
  Lists of small cards on the EML pages (Citations and links, Collection Data); js/eml-cards.js renames the fields and
  numbers the cards. Card fields of a stored item are named base[i].field (base[i] for a plain value); the template's are
  named TPL.field (TPL).

  <@cardList base="eml.collections" titleKey=... addKey=... emptyKey=...; part>
      <#if part == "cards"> stored cards </#if>
      <#if part == "template"> one card with TPL names </#if>
  </@cardList>
-->
<#macro ecIcon path>
    <svg viewBox="0 0 24 24" aria-hidden="true"><path d="${path}"/></svg>
</#macro>

<#macro cardList base titleKey addKey emptyKey>
    <div class="ec-list" data-cards="${base}" data-title="${action.getText(titleKey)}"
         data-removed="${action.getText('eml.party.removed')}" data-moved="${action.getText('eml.party.moved')}">
        <div class="ms-steps" data-items><#nested "cards"></div>
        <p class="geo-hint mt-0" data-empty hidden><@s.text name=emptyKey/></p>
        <#-- errors for the whole list, e.g. a list that must not be empty -->
        <#local listErrors = (action.fieldErrors[base])![]/>
        <#if listErrors?has_content>
            <ul class="list-unstyled text-gbif-danger small mb-0 mt-1"><#list listErrors as message><li>${message}</li></#list></ul>
        </#if>
        <div class="add-area">
            <button type="button" data-act="add"><@ecIcon "M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/><span><@s.text name=addKey/></span></button>
        </div>
        <template data-template><#nested "template"></template>
    </div>
</#macro>

<#--
  The header of a card: grip, number ("Collection 1"; none for a one-field card, whose field goes in the header
  instead, as the nested content), up/down and remove.
-->
<#macro cardHead titleKey index=-1 inline=false>
    <div class="ms-step-head">
        <span class="party-grip" title="${action.getText('eml.party.dragToReorder')}" aria-hidden="true">
            <@ecIcon "M9 4a2 2 0 1 1 0 4 2 2 0 0 1 0-4zm6 0a2 2 0 1 1 0 4 2 2 0 0 1 0-4zM9 10a2 2 0 1 1 0 4 2 2 0 0 1 0-4zm6 0a2 2 0 1 1 0 4 2 2 0 0 1 0-4zm-6 6a2 2 0 1 1 0 4 2 2 0 0 1 0-4zm6 0a2 2 0 1 1 0 4 2 2 0 0 1 0-4z"/>
        </span>
        <#if inline>
            <div class="ec-inline-field"><#nested></div>
        <#else>
            <span class="ms-step-title" data-card-title><#if (index >= 0)>${action.getText(titleKey)?replace('{0}', (index + 1)?c)}</#if></span>
        </#if>
        <span class="party-order">
            <button type="button" class="party-icon-btn" data-act="up" aria-label="${action.getText('eml.party.moveUp')}" title="${action.getText('eml.party.moveUp')}"><@ecIcon "M7.41 15.41 12 10.83l4.59 4.58L18 14l-6-6-6 6z"/></button>
            <button type="button" class="party-icon-btn" data-act="down" aria-label="${action.getText('eml.party.moveDown')}" title="${action.getText('eml.party.moveDown')}"><@ecIcon "M7.41 8.59 12 13.17l4.59-4.58L18 10l-6 6-6-6z"/></button>
        </span>
        <button type="button" class="party-icon-btn party-icon-danger" data-act="remove" aria-label="${action.getText('eml.party.remove')}" title="${action.getText('eml.party.remove')}"><@ecIcon "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/></button>
    </div>
</#macro>
