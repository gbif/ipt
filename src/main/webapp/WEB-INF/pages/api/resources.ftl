{"ipt":{
"name":"${ipt.name!}",
"link":"${baseURL!}",
"root":"${baseURL!}/${REQ_PATH_API!"api"}/resources"
},
"resources": [
    <#list resources as resource>
    {"data":${resource.toJSONSimple()}
    ,"links":{
         "self":"${cfg.getResourceApiUrl(resource.shortname)}"
         ,"dwca":"${cfg.getResourceArchiveUrl(resource.shortname)}"
         ,"eml":"${cfg.getResourceEmlUrl(resource.shortname)}"
         ,"resource":"${cfg.getResourceUrl(resource.shortname)}"
    }}<#sep>,</#sep>
    </#list>
    ]
}
