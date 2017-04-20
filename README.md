# FixrService-Backend

## Contributing

Please see [CONTRIBUTING](CONTRIBUTING.md) for information on our contribution processes and setting up a development environment.

## Deploy

ubuntu@ec2-13-58-20-201.us-east-2.compute.amazonaws.com


## Run

cd cuplv
cd FixrService-Backend/
sbt run


# Groums API

`POST /compute/method/groums`


```
Input: { user: <TEXT - user name of GitHub site>
          , repo: <TEXT - repo name of GitHub site>
          , class: <TEXT - Class name>, method: <TEXT - Method name>  
          , (Optional  hash: <TEXT - hash of GitHub site (optional)>) }

output: { cluster_id : <INT - unique id of cluster> 
            , patterns : LIST { weight : INT , pattern : <GROUMPAT> }  }
            where GROUMPAT = { groum : <JSON REP of DOT>
                                               , provenance : LIST <GROUMKEY>] }
                       GROUMKEY = { user:<TEXT>, repo:<TEXT>, class:<TEXT>
                                                , method:<TEXT>, hash:<TEXT> }


```

`GET /query/provenance/groums`


```
Params: 
user=<TEXT> & repo=<TEXT> & class=<TEXT> & method=<TEXT> & hash=<TEXT>

Output: { groum : <JSON REP of DOT>
             , githubLink : <TEXT - Link to GitHub page> }


```


