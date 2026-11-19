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

<#-- Loading spinner shown by displayProcessing() in global.js. dataTables_processing is the class global.js looks for,
     dt-processing applies the DataTables spinner styles -->
<div class="dataTables_processing dt-processing" style="display: none;">
    <div><div></div><div></div><div></div><div></div></div>
</div>


<script src="${baseURL}/js/bootstrap/bootstrap-5.3.8.bundle.min.js"></script>
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
        // the link to follow on Leave is marked data-post (e.g. logout): it must be sent as a POST, see csrf.js
        let redirectPost = false;
        let isIntentionalUnload = false;
        // the EML metadata pages' save bar (manage/eml/eml_savebar.ftl) comes up with the first change
        const saveBar = document.getElementById('eml-savebar');
        function markUnsaved() {
            hasUnsavedChanges = true;
            if (saveBar) {
                saveBar.classList.add('show');
                saveBar.setAttribute('aria-hidden', 'false');
            }
        }

        // Track form changes
        const form = document.querySelector('.track-unsaved');

        form.querySelectorAll('button[type="submit"], input[type="submit"]').forEach(btn => {
            btn.addEventListener('click', () => {
                isIntentionalUnload = true;
                hasUnsavedChanges = false;
            });
        });

        if (form) {
            // inputs
            form.addEventListener('input', () => {
                markUnsaved();
            });

            // adding or removing an item of a list (macros/metadata.ftl links: ids plus*, classes remove*)
            $(form).on('click', 'a[id^="plus"], a[class*="remove"]', function () {
                markUnsaved();
            });

            // dropdowns (select2)
            $(document).on('select2:select select2:unselect change', 'select', function () {
                if ($(this).data('select2')) {
                    markUnsaved();
                }
            });

            // copy agent button
            document.getElementById('copy-agent-button')?.addEventListener('click', () => {
                markUnsaved();
            });

            // Disable warning before submitting the form
            form.addEventListener('submit', () => {
                isIntentionalUnload = true;
                hasUnsavedChanges = false;
            });
        }

        // Warn before browser unload (refresh, close tab)
        window.addEventListener('beforeunload', (e) => {
            if (!hasUnsavedChanges || isIntentionalUnload) {
                return;
            }

            e.preventDefault();
            e.returnValue = '';
        });

        // Intercept internal link clicks
        document.querySelectorAll('a[href]').forEach(link => {
            link.addEventListener('click', (e) => {
                // Discard in the save bar: reloads the page, dropping the changes on purpose
                if (link.hasAttribute('data-discard')) {
                    isIntentionalUnload = true;
                    hasUnsavedChanges = false;
                    return;
                }
                // Skip links that should not trigger the unsaved-changes modal
                if (link.id === 're-infer-link') {
                    console.log("link.id=" + link.id);
                    console.log("skipping!")
                    isIntentionalUnload = true;
                    hasUnsavedChanges = false;
                    return;
                }

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
                    redirectPost = link.hasAttribute('data-post');
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
                if (redirectPost && window.IPT_CSRF) {
                    window.IPT_CSRF.postTo(redirectUrl);
                } else {
                    window.location.href = redirectUrl;
                }
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
