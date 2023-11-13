[#ftl output_format="HTML"]

[#if actionErrors?size>0]
    [#list actionErrors as error]
        <div class="alert alert-danger alert-dismissible fade show d-flex" role="alert">
            <div class="me-3">
                <i class="bi bi-exclamation-circle alert-red-2 fs-bigger-2 me-2"></i>
            </div>
            <div class="overflow-x-hidden pt-1">
                [#if error?has_content]
                    <span>[@error?interpret /]</span>
                [/#if]
            </div>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    [/#list]
[/#if]
