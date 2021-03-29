<#ftl output_format="HTML">
<footer class="footer mt-auto text-muted text-center py-3" style='font-size: 0.875rem !important;'>
    <div class="container">
        <ul class="list-inline mb-0">
            <li class="list-inline-item ipt-footer-item">
                <small>IPT <@s.text name="footer.version"/> ${cfg.version!"???"}</small>
            </li>
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

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta3/dist/js/bootstrap.bundle.min.js" integrity="sha384-JEW9xMcG8R+pH31jmWH6WWP0WintQrMb4s7ZOdauHnUtxwoG2vI5DkLtS3qm9Ekf" crossorigin="anonymous"></script>
<script type="text/javascript" src="${baseURL}/js/form-validation.js"></script>
<script>
    var popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'))
    var popoverList = popoverTriggerList.map(function (popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl)
    })
    var popover = new bootstrap.Popover(document.querySelector('.popover-dismiss'), {
        trigger: 'focus'
    })
</script>

</body>
</html>
