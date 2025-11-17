<#ftl output_format="HTML">
<footer class="footer bg-light border-top mt-auto text-center text-smaller py-3">
    <div class="container">
        <div class="mb-1 footer-text">
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
    document.addEventListener('DOMContentLoaded', () => {
        const modalEl = document.getElementById('unsavedChangesModal');
        const stayButton = document.getElementById('stayButton');
        const leaveButton = document.getElementById('leaveButton');

        if (!modalEl || !leaveButton) {
            console.warn("Unsaved changes modal not found.");
            return;
        }

        const bsModal = new bootstrap.Modal(modalEl);
        let hasUnsavedChanges = false;
        let redirectUrl = null;
        let isIntentionalUnload = false;

        // Track form changes
        const form = document.querySelector('.track-unsaved');
        if (form) {
            // inputs
            form.addEventListener('input', () => {
                hasUnsavedChanges = true;
            });

            // dropdowns (select2)
            $(document).on('select2:select select2:unselect change', 'select', function () {
                if ($(this).data('select2')) {
                    hasUnsavedChanges = true;
                }
            });

            // copy agent button
            document.getElementById('copy-agent-button')?.addEventListener('click', () => {
                hasUnsavedChanges = true;
            });

            // Disable warning before submitting the form
            form.addEventListener('submit', () => {
                isIntentionalUnload = true;
                hasUnsavedChanges = false;
            });
        }

        // Warn before browser unload (refresh, close tab)
        window.addEventListener('beforeunload', (e) => {
            if (hasUnsavedChanges && !isIntentionalUnload) {
                e.preventDefault();
                e.returnValue = '';
            }
        });

        // Intercept internal link clicks
        document.querySelectorAll('a[href]').forEach(link => {
            link.addEventListener('click', (e) => {
                // Skip links that should not trigger the unsaved-changes modal
                if (link.id === 're-infer-link') return;

                const href = link.getAttribute('href');

                // Ignore non-links and JS links
                if (!href ||
                    href === '#' ||
                    href.startsWith('#') ||
                    href.startsWith('javascript:') ||
                    link.target === '_blank') {
                    return;
                }

                // Compute resolved URL
                const targetUrl = new URL(link.href);
                const currentUrl = new URL(window.location.href);

                // Only warn if the target is a DIFFERENT page
                const isRealNavigation =
                    targetUrl.pathname !== currentUrl.pathname ||
                    targetUrl.search !== currentUrl.search ||
                    targetUrl.hash === '' && currentUrl.hash !== '';

                if (!isRealNavigation) {
                    return;
                }

                if (hasUnsavedChanges) {
                    e.preventDefault();
                    redirectUrl = link.href;
                    bsModal.show();
                }
            });
        });

        // Stay -> close modal only
        if (stayButton) {
            stayButton.addEventListener('click', () => {
                redirectUrl = null;
            });
        }

        // Leave -> proceed to stored URL
        leaveButton.addEventListener('click', () => {
            if (redirectUrl) {
                isIntentionalUnload = true;
                hasUnsavedChanges = false;
                window.location.href = redirectUrl;
            }
        });
    });
</script>
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
