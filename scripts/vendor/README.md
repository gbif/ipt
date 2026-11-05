# Front-end libraries

The JavaScript and CSS libraries used by the IPT UI are committed to `src/main/webapp/js` and `src/main/webapp/styles`.
Their versions are tracked in the root `package.json`. npm is not part of the Maven build: building the IPT doesn't need Node.

## Upgrading a library

```sh
npm ci                        # install the versions in package-lock.json
npm install <package>@<version> --save-exact
npm run vendor                # copy the files into src/main/webapp
```

`npm run vendor` copies the files listed in `vendor.mjs` and rebuilds the ProseMirror bundle (`js/prosemirror/prosemirror-bundle.min.js`, exposed as `window.PM`, used by `js/docbook/docbook-editor.js`).
For file names that contain the version, such as `jquery-3.7.1.min.js`, it removes the old file and updates the references in the FTL pages.
Commit `package.json`, `package-lock.json` and the changed files together.

Renovate opens upgrade PRs for `package.json`, but doesn't merge them automatically.
Check out the PR branch, run `npm ci && npm run vendor`, test the affected pages, and push the result.

The `UI libraries` GitHub workflow runs `npm audit` and `npm run vendor -- --check`. The check fails if the committed files don't match `package.json`.

## Maintained by hand

These aren't on npm in the form used, or have IPT changes:

- `js/leaflet/locationfilter-0.1-leaflet1.js`, `styles/leaflet/locationfilter.css`: Leaflet.locationfilter 0.1 (2012, unmaintained), updated for Leaflet 1.x
- `images/leaflet/`: IPT marker and filter images
- `js/ajaxfileupload.js`: old jQuery plugin, not on npm
- `js/jconfirmation.jquery.js`: jQuery plugin, extended for the IPT
- `styles/fonts/gbificons.*`: GBIF icon font
- `styles/bootstrap/bootstrap-borders.css`: IPT additions to Bootstrap

A file with a version in its name, such as `locationfilter-0.1-leaflet1.js`, is cached by browsers for a year. Rename it when you change it, and update the FTL pages.
