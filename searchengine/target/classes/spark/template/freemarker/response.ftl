<!DOCTYPE html>
<html>
<head>
    <#include "header.ftl">
</head>

<body>

<div class="container">
    <#list links as video>
        <#list v as video>
            <a href="${l}">Here</a>
        </#list>
        <br>
    </#list>
</div>

</body>
</html>
