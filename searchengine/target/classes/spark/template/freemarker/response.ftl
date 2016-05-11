<!DOCTYPE html>
<html>
<head>
    <#include "header.ftl">
</head>

<body>

<#include "searchbar.ftl">

<div class="container">
    <#list videos as v>
    <p class="text-center">
        <a target="_blank" href="https://www.youtube.com/watch?v=${v.name}#t=${v.time}"><img src="http://img.youtube.com/vi/${v.name}/hqdefault.jpg"></a> 
    </p>
    </#list>
</div>

</body>
</html>
