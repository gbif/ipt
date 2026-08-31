/**
 * Generic scroll-spy: highlights a table-of-contents link (adds an "active"
 * class) for whichever content section currently sits just below a sticky
 * navbar, using IntersectionObserver — no cached offsets, so it stays
 * correct even when content above a section changes height after load
 * (tables populating, async content rendering, images/fonts loading, etc).
 *
 * Convention: each TOC link's href points to a small anchor element
 * (e.g. <span id="anchor-foo">); the actual section that gets activated
 * and scrolled to is the element with id="foo" (the "anchor-" prefix
 * stripped). This lets the anchor sit anywhere convenient while the
 * "section" used for activation/scrolling can be a different element.
 *
 * Usage:
 *   var spy = IptScrollSpy.init({
 *     tocSelector: '.bd-toc nav a',       // required: TOC links
 *     navbarHeightVar: '--navbar-height', // optional: CSS custom property (px), default shown
 *     activeClass: 'active',              // optional
 *     onActivate: function ($link) {}     // optional: called whenever the active link changes
 *   });
 *
 *   // later, if content is swapped in/out dynamically:
 *   spy.refresh();
 *   // when the page/section is torn down:
 *   spy.destroy();
 *
 * Returns null if tocSelector matches no links, or none of them resolve
 * to a real section element.
 */
(function (window, $) {
    'use strict';

    function createScrollSpy(options) {
        var settings = $.extend({
            tocSelector: '.bd-toc nav a',
            navbarHeightVar: '--navbar-height',
            activeClass: 'active',
            onActivate: null
        }, options);

        var $tocLinks = $(settings.tocSelector);
        if (!$tocLinks.length) return null;

        function getNavbarOffset() {
            var value = getComputedStyle(document.documentElement)
                .getPropertyValue(settings.navbarHeightVar);
            return parseInt(value, 10) || 0;
        }

        function resolveSection($link) {
            var anchor = $($link.attr('href'));
            if (!anchor.length) return null;
            var sectionId = anchor[0].id.replace('anchor-', '');
            var $section = $('#' + sectionId);
            return $section.length ? $section[0] : null;
        }

        var sectionToLink = new Map();
        $tocLinks.each(function () {
            var $link = $(this);
            var el = resolveSection($link);
            if (el) sectionToLink.set(el, $link);
        });

        if (!sectionToLink.size) return null;

        var visibleSections = new Set();
        var observer = null;

        function setActiveLink() {
            // Prefer the section that appears latest in document order among
            // those currently crossing the activation line — that's the one
            // the user has most recently scrolled into.
            var current = null;
            sectionToLink.forEach(function (_link, el) {
                if (!visibleSections.has(el)) return;
                if (!current || (current.compareDocumentPosition(el) & Node.DOCUMENT_POSITION_FOLLOWING)) {
                    current = el;
                }
            });

            $tocLinks.removeClass(settings.activeClass);
            if (current) {
                sectionToLink.get(current).addClass(settings.activeClass);
            }

            if (typeof settings.onActivate === 'function') {
                settings.onActivate(current ? sectionToLink.get(current) : null);
            }
        }

        function initObserver() {
            if (observer) observer.disconnect();
            visibleSections.clear();

            var navbarHeight = getNavbarOffset();
            // Collapse the observation area to a thin horizontal line just
            // below the sticky navbar. Whichever section crosses that line
            // is "current" — reflects what's actually on screen right now.
            var bottomMargin = Math.max(window.innerHeight - navbarHeight - 1, 0);

            observer = new IntersectionObserver(function (entries) {
                entries.forEach(function (entry) {
                    if (entry.isIntersecting) {
                        visibleSections.add(entry.target);
                    } else {
                        visibleSections.delete(entry.target);
                    }
                });
                setActiveLink();
            }, {
                root: null,
                rootMargin: '-' + navbarHeight + 'px 0px -' + bottomMargin + 'px 0px',
                threshold: 0
            });

            sectionToLink.forEach(function (_link, el) {
                observer.observe(el);
            });
        }

        function scrollToTarget(e) {
            var $link = $(this);
            var el = resolveSection($link);
            if (!el) return;

            e.preventDefault();
            el.scrollIntoView({ behavior: 'smooth' });
            history.pushState(null, '', $link.attr('href'));
        }

        initObserver();

        var resizeTimer = null;
        function onResize() {
            clearTimeout(resizeTimer);
            resizeTimer = setTimeout(initObserver, 150);
        }

        $(window).on('resize', onResize);
        $tocLinks.on('click', scrollToTarget);

        return {
            refresh: initObserver,
            destroy: function () {
                if (observer) observer.disconnect();
                $(window).off('resize', onResize);
                $tocLinks.off('click', scrollToTarget);
            }
        };
    }

    window.IptScrollSpy = {
        init: createScrollSpy
    };
})(window, jQuery);