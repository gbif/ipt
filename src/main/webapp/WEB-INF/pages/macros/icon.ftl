<#-- A Material Icon from the sprite built by npm run vendor (scripts/vendor/README.md): 1em square, in the text colour.
     name: icon name as listed in ICONS in scripts/vendor/vendor.mjs, e.g. "edit"
     class, id, style, title: optional; without a title the icon is hidden from screen readers -->
<#macro icon name class="" id="" style="" title="">
<svg class="material-icon<#if class?has_content> ${class}</#if>"<#if id?has_content> id="${id}"</#if><#if style?has_content> style="${style}"</#if><#if title?has_content> role="img" aria-label="${title}"<#else> aria-hidden="true"</#if> focusable="false"><#if title?has_content><title>${title}</title></#if><use href="${baseURL}/images/icons/material-icons.svg#${name}"/></svg><#t>
</#macro>
