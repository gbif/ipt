{"ipt":{
    "name":"${ipt.name!}",
    "link":"${baseURL!}",
    "root":"${baseURL!}/${REQ_PATH_API!"api"}/resources"
},
"resource": {"data":${resource.toJSON()}
            ,"links":{
                "self":"${cfg.getResourceApiUrl(resource.shortname)}"
                ,"dwca":"${cfg.getResourceArchiveUrl(resource.shortname)}"
                ,"eml":"${cfg.getResourceEmlUrl(resource.shortname)}"
                ,"resource":"${cfg.getResourceUrl(resource.shortname)}"
            }}
}
