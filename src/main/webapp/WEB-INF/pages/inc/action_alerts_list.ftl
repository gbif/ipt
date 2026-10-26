[#ftl output_format="HTML"]

[#--
  The action's alerts of one kind in one alert: the first shown, the others (if any) listed collapsed under it, so a
  page isn't filled with alerts and none of them is left out.
  items: the alerts' texts
  alertClass: the alert's class, e.g. alert-warning
  iconClass: the icon's classes, e.g. bi-exclamation-triangle alert-orange-2
  moreKey: the text of the toggle, e.g. "{0} more warning(s)"
--]
[#macro alertList items alertClass iconClass moreKey]
    [#if items?size > 0]
        <div class="alert ${alertClass} alert-dismissible fade show d-flex" role="alert">
            <div class="me-3">
                <i class="bi ${iconClass} fs-bigger-2 me-2"></i>
            </div>
            <div class="overflow-x-hidden pt-1">
                [#list items as item]
                    [#if item?is_first]
                        [#if item?has_content]
                            <span>[@item?interpret /]</span>
                        [/#if]
                        [#if !item?is_last]
                            <details class="alert-more mt-1">
                                <summary class="fs-smaller">[@s.text name=moreKey][@s.param]${items?size - 1}[/@s.param][/@s.text]</summary>
                                <ul class="mt-1 mb-0 ps-3">
                        [/#if]
                    [#else]
                                    <li>[#if item?has_content][@item?interpret /][/#if]</li>
                        [#if item?is_last]
                                </ul>
                            </details>
                        [/#if]
                    [/#if]
                [/#list]
            </div>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    [/#if]
[/#macro]
