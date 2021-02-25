<#ftl output_format="HTML">
<footer class="footer mt-auto text-muted text-center py-3">
    <div class="container">
        <ul class="list-inline mb-0">
            <li class="list-inline-item">
                <small>IPT Version 2.4.3-SNAPSHOT-reb1201e</small>
            </li>
            <li class="list-inline-item">
                <a href="#"><small>About the IPT</small></a>
            </li>
            <li class="list-inline-item">
                <a href="#"><small>User Manual</small></a>
            </li>
            <li class="list-inline-item">
                <a href="#"><small>Report a bug</small></a>
            </li>
            <li class="list-inline-item">
                <a href="#"><small>Request new feature</small></a>
            </li>
        </ul>
        <small>© 2009–2021 Global Biodiversity Information Facility. Licensed under the Apache license, version 2.0.</small>
    </div>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta2/dist/js/bootstrap.bundle.min.js" integrity="sha384-b5kHyXgcpbZJO/tY9Ul7kGkf1S0CWuKcCD38l8YkeH8z8QjE0GmW1gYU5S9FOnJ0" crossorigin="anonymous"></script>
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
