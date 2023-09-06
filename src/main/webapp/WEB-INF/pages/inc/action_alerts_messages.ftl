[#ftl output_format="HTML"]

[#if actionMessages?size>0]
    [#list actionMessages as message]
        <div class="alert alert-success mt-2 alert-dismissible fade show d-flex" role="alert">
            <div class="me-3">
                <i class="bi bi-check2-circle alert-green-2 fs-bigger-2 me-2"></i>
            </div>
            <div class="overflow-x-hidden pt-1">
                [#if message?has_content]
                <span>[@message?interpret /]</span>
                [/#if]
            </div>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    [/#list]
[/#if]
