$mongo = Start-Job { &"D:\programs\mongodb-3.6.3\bin\mongod.exe" --dbpath d:\cvut-checkouted\andruian\thirdparty\mongo\ }
&"d:\cvut-checkouted\andruian\thirdparty\solr-7.2.1\bin\solr.cmd" start  # Runs as daemon
$fuseki = Start-Job { cd "d:\cvut-checkouted\andruian\wip\apache-jena-fuseki-3.6.0\"; .\fuseki-server.bat }

Write-Host "Waiting for servers init..."
sleep 10

while ($True) {
    Write-Host "====================="
    Write-Host "MongoDB"
    Write-Host "====================="
    Receive-Job $mongo.Id
        
    Write-Host "====================="
    Write-Host "Fuseki"
    Write-Host "====================="
    Receive-Job $fuseki.Id
    
    sleep 5
}

# Stop solr:  &"d:\cvut-checkouted\andruian\thirdparty\solr-7.2.1\bin\solr.cmd" stop -all