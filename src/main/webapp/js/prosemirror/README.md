# ProseMirror bundle

`prosemirror-bundle.min.js` packs the ProseMirror modules used by the EML text editor
(`js/docbook/docbook-editor.js`) into one file, exposed as `window.PM`
(`PM.model`, `PM.state`, `PM.view`, `PM.transform`, `PM.commands`, `PM.keymap`, `PM.history`,
`PM.schemaList`, `PM.inputrules`). The matching stylesheet is `styles/prosemirror/prosemirror.css`.

Versions: prosemirror-model 1.25.12, prosemirror-state 1.4.4, prosemirror-view 1.42.9,
prosemirror-transform 1.12.4, prosemirror-commands 1.7.2, prosemirror-keymap 1.2.3,
prosemirror-history 1.5.1, prosemirror-schema-list 1.6.0, prosemirror-inputrules 1.5.1.
License: MIT, https://github.com/ProseMirror/prosemirror/blob/master/LICENSE

## Rebuilding

In an empty directory outside the project:

```sh
npm init -y
npm install prosemirror-model prosemirror-state prosemirror-view prosemirror-transform \
  prosemirror-commands prosemirror-keymap prosemirror-history prosemirror-schema-list \
  prosemirror-inputrules esbuild

cat > entry.js <<'JS'
export * as model from "prosemirror-model";
export * as state from "prosemirror-state";
export * as view from "prosemirror-view";
export * as transform from "prosemirror-transform";
export * as commands from "prosemirror-commands";
export * as keymap from "prosemirror-keymap";
export * as history from "prosemirror-history";
export * as schemaList from "prosemirror-schema-list";
export * as inputrules from "prosemirror-inputrules";
JS

npx esbuild entry.js --bundle --minify --format=iife --global-name=PM --target=es2018 \
  --legal-comments=none --outfile=prosemirror-bundle.min.js \
  --banner:js="/*! ProseMirror bundle for IPT (window.PM). <package@version list>. MIT License, Copyright (C) 2015-2017 by Marijn Haverbeke <marijn@haverbeke.berlin> and others. See README.md */"
```

Copy `prosemirror-bundle.min.js` here and `node_modules/prosemirror-view/style/prosemirror.css` to
`styles/prosemirror/` with a version comment on top as in the current file, then update the versions above.
