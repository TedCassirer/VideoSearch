<!DOCTYPE html>
<html>
<head>
    <#include "header.ftl">
</head>

<body>

<#include "searchbar.ftl">

<div class="container">
    <#list videos as v>
    <p>
        <a target="_blank" href="https://www.youtube.com/watch?v=${v.name}"><img src="http://img.youtube.com/vi/${v.name}/default.jpg"></a> 
        <#list v.times as t>
            - <a target="_blank" href="https://www.youtube.com/watch?v=${v.name}#t=${t}s">At ${t}s</a> 
        </#list>
    </p>
    </#list>
</div>

</body>
</html>
