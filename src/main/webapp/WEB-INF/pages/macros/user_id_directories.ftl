<#ftl output_format="HTML">

<!-- temp fix to protocol differences -->
<#assign userIdDirecotriesExtended =
{"https://orcid.org/": "https://orcid.org/",
"http://orcid.org/": "https://orcid.org/",
"http://www.researcherid.com/rid/": "http://www.researcherid.com/rid/",
"https://www.researcherid.com/rid/": "http://www.researcherid.com/rid/",
"http://scholar.google.com/citations?user=": "http://scholar.google.com/citations?user=",
"https://scholar.google.com/citations?user=": "http://scholar.google.com/citations?user=",
"https://www.linkedin.com/profile/view?id=": "https://www.linkedin.com/profile/view?id=",
"http://www.linkedin.com/profile/view?id=": "https://www.linkedin.com/profile/view?id=",
"http://www.linkedin.com/in/": "https://www.linkedin.com/profile/view?id=",
"https://www.linkedin.com/in/": "https://www.linkedin.com/profile/view?id=",
"http://www.wikidata.org/entity/": "http://www.wikidata.org/entity/",
"https://www.wikidata.org/entity/": "http://www.wikidata.org/entity/"}>
