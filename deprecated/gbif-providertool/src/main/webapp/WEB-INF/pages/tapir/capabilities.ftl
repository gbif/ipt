<?xml version='1.0' encoding='utf-8'?>
<#include "/WEB-INF/pages/inc/globalVars.ftl">  
<#if envelope>
 <#include "/WEB-INF/pages/tapir/header.ftl">  
</#if>
<#escape x as x?xml>
<capabilities>
 <operations>
   <ping/>
   <metadata/>
   <capabilities/>
   <inventory>
     <anyConcepts/>
   </inventory>
   <search>
     <outputModels>
       <knownOutputModels>
         <outputModel alias="${modelAlias}" location="${modelLocation}"/>
       </knownOutputModels>
     </outputModels>
   </search>
 </operations>
 <requests>
   <encoding><kvp/></encoding>
   <globalParameters><logOnly>denied</logOnly></globalParameters>
   <filter>
     <encoding>
       <expression>
         <concept/>
         <literal/>
         <parameter/>
         <variable/>
         <arithmetic/>
       </expression>
       <booleanOperators>
         <logical>
           <not/>
           <and/>
           <or/>
         </logical>
         <comparative>
           <equals caseSensitive="true"/>
           <greaterThan/>
           <greaterThanOrEquals/>
           <lessThan/>
           <lessThanOrEquals/>
           <in/>
           <isNull/>
           <like caseSensitive="true"/>
         </comparative>
       </booleanOperators>
     </encoding>
   </filter>
 </requests>
 <concepts>
 <#list conceptSchemas?keys as ns>
  <schema namespace="${ns}" location="http://darwincore.googlecode.com/svn/trunk/xsd/tdwg_dwcterms.xsd">
  <#list conceptSchemas[ns] as c>
   <mappedConcept id="${c.qualName}" alias="${c.name}" searchable="true" datatype="http://www.w3.org/2001/XMLSchema#string"/>
  </#list>
  </schema>
 </#list>
 </concepts>
 <variables/>
 <settings>
   <maxElementRepetitions>1000</maxElementRepetitions>
 </settings>
</capabilities>
</#escape>
<#if envelope>
 <#include "/WEB-INF/pages/tapir/footer.ftl">  
</#if>