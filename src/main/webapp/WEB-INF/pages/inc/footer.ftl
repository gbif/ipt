<#ftl output_format="HTML">
<footer class="footer mt-auto text-muted text-center text-smaller py-3">
    <div class="container">
        <div class="mb-1">
            <img class="footer-gbif-logo" alt="GBIF" src="${baseURL}/images/GBIF-2015-standard-ipt.png"/>
            Integrated Publishing Toolkit (IPT) <@s.text name="footer.version"/> ${cfg.version!"???"}
        </div>
        <ul class="list-inline mb-0">
            <li class="list-inline-item ipt-footer-item">
                <a href="https://www.gbif.org/ipt"><small><@s.text name="footer.projectHome"/></small></a>
            </li>
            <li class="list-inline-item ipt-footer-item">
                <a href="https://github.com/gbif/ipt/wiki/IPT2ManualNotes.wiki" target="_blank"><small><@s.text name="footer.useManual"/></small></a>
            </li>
            <li class="list-inline-item ipt-footer-item">
                <a href="https://github.com/gbif/ipt/issues/new" target="_blank"><small><@s.text name="footer.bugReport"/></small></a>
            </li>
            <li class="list-inline-item">
                <a href="https://github.com/gbif/ipt/issues/new" target="_blank"><small><@s.text name="footer.featureRequest"/></small></a>
            </li>
        </ul>
    </div>
</footer>

<div id="modalbox">
    <div id="modalback"></div>
    <div id="modalcontainer">
        <div id="modalcontent" class="container-fluid"></div>
    </div>
</div>

<script src="${baseURL}/js/bootstrap/bootstrap.bundle-5.0.0-beta3.min.js"></script>
<script src="${baseURL}/js/form-validation.js"></script>
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
