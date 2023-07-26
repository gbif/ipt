<#ftl output_format="HTML">
<footer class="footer bg-light border-top mt-auto text-center text-smaller py-3">
    <div class="container">
        <div class="mb-1">
            <img title="IPT ${(cfg.version)!}" class="footer-gbif-logo" alt="GBIF" src="${baseURL}/images/GBIF-2015-standard-ipt.png"/>
            Integrated Publishing Toolkit (IPT) <@s.text name="footer.version"/> ${(cfg.shortVersion)!}
        </div>
        <ul class="list-inline mb-0">
            <li class="list-inline-item ipt-footer-item">
                <a class="footer-link" href="https://www.gbif.org/ipt"><small><@s.text name="footer.projectHome"/></small></a>
            </li>
            <li class="list-inline-item ipt-footer-item">
                <a class="footer-link" href="https://ipt.gbif.org/manual/" target="_blank"><small><@s.text name="footer.useManual"/></small></a>
            </li>
            <li class="list-inline-item ipt-footer-item">
                <a class="footer-link" href="https://github.com/gbif/ipt/issues/new" target="_blank"><small><@s.text name="footer.bugReport"/></small></a>
            </li>
            <li class="list-inline-item">
                <a class="footer-link" href="https://github.com/gbif/ipt/issues/new" target="_blank"><small><@s.text name="footer.featureRequest"/></small></a>
            </li>
        </ul>
    </div>
</footer>

<div class="dataTables_processing" style="display: none;">
    <div><div></div><div></div><div></div><div></div></div>
</div>

<div id="modalbox">
    <div id="modalback"></div>
    <div id="modalcontainer">
        <div id="modalcontent" class="container-fluid"></div>
    </div>
</div>

<script src="${baseURL}/js/bootstrap/bootstrap.bundle.min.js"></script>
<script src="${baseURL}/js/form-validation.js"></script>
<script src='${baseURL}/js/sortable/html5sortable-0.13.3.js'></script>
<script>
    var popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'))
    var popoverList = popoverTriggerList.map(function (popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl)
    })
    try {
        var popover = new bootstrap.Popover(document.querySelector('.popover-dismiss'), {
            trigger: 'focus'
        })
    } catch (TypeError) {}
</script>

</body>
</html>
