<#-- Shown while the metadata of a big dataset is inferred in the background: the page refreshes when it is done -->
<#if inferring!false>
    <div id="inferring-notice" class="callout callout-info text-smaller mt-2" role="status">
        <@s.text name="eml.inferring"/>
        <div class="inline-spinner ms-1" aria-hidden="true">
            <div class="dot"></div>
            <div class="dot"></div>
            <div class="dot"></div>
            <div class="dot"></div>
        </div>
    </div>
    <script>
        (function () {
            var statusUrl = "${baseURL}/manager-api/inferring-status?r=${resource.shortname?url}";
            var poll = setInterval(function () {
                $.getJSON(statusUrl).done(function (status) {
                    if (status && status.inferring === false) {
                        clearInterval(poll);
                        // reload without requesting to re-infer again
                        var url = new URL(window.location.href);
                        url.searchParams.delete("reinferMetadata");
                        window.location.replace(url.toString());
                    }
                });
            }, 3000);
        })();
    </script>
</#if>
