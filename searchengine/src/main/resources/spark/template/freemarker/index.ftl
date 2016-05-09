<!DOCTYPE html>
<html>
<head>
<#include "header.ftl">
</head>

<body>

<div class="search-bar-container text-center">
    <div class="container search-bar">
        <form class="form-inline" method="get" action="./">
            <div class="form-group">
                <label class="sr-only" for="query">Your query</label>
                <div class="input-group">
                    <input type="text" class="form-control" name="q" id="query" placeholder="Enter a query">
                </div>
            </div>
            <button type="submit" class="btn btn-primary">Search</button>
        </form>
    </div>
</div>

</body>
</html>
