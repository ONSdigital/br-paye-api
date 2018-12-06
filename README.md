# Business Register PAYE API
[![license](https://img.shields.io/github/license/mashape/apistatus.svg)](./LICENSE)

Supports retrieval of Paye As You Earn (PAYE) admin data.

See the [Open API Specification](./api.yaml) for details of the API.


### Development Tasks

Run the service in development mode (default Play port of 9000):

    sbt run

Run the service in development mode (custom port of 9123):

    sbt "run 9123"

Run unit tests with coverage:

    sbt clean coverage test coverageReport

Run acceptance tests:

    sbt it:test

Run all tests:

    sbt clean test it:test

Generate static analysis report:

    sbt scapegoat


#### Testing Against a Local HBase Database

1.  Start HBase

        bin/start-hbase.sh

2.  Create Database Schema

        bin/hbase shell

    In the resulting HBase Shell, create the namespace:

        create_namespace 'br_paye_db'

    followed by the table:

        create 'br_paye_db:paye', 'd'

3.  Create Sample Data

    Create one record with only mandatory fields:

        put 'br_paye_db:paye', '054G6Y20621', 'd:payeref', '054G6Y20621-payeref'
        put 'br_paye_db:paye', '054G6Y20621', 'd:nameline1', '054G6Y20621-nameline1'
        put 'br_paye_db:paye', '054G6Y20621', 'd:legalstatus', '054G6Y20621-legalstatus'
        put 'br_paye_db:paye', '054G6Y20621', 'd:address1', '054G6Y20621-address1'
        put 'br_paye_db:paye', '054G6Y20621', 'd:postcode', '054G6Y20621-postcode'

    And another with all possible fields:

        put 'br_paye_db:paye', '076I8A42843', 'd:payeref', '076I8A42843-payeref'
        put 'br_paye_db:paye', '076I8A42843', 'd:nameline1', '076I8A42843-nameline1'
        put 'br_paye_db:paye', '076I8A42843', 'd:nameline2', '076I8A42843-nameline2'
        put 'br_paye_db:paye', '076I8A42843', 'd:nameline3', '076I8A42843-nameline3'
        put 'br_paye_db:paye', '076I8A42843', 'd:tradstyle1', '076I8A42843-tradstyle1'
        put 'br_paye_db:paye', '076I8A42843', 'd:tradstyle2', '076I8A42843-tradstyle2'
        put 'br_paye_db:paye', '076I8A42843', 'd:tradstyle3', '076I8A42843-tradstyle3'
        put 'br_paye_db:paye', '076I8A42843', 'd:legalstatus', '076I8A42843-legalstatus'
        put 'br_paye_db:paye', '076I8A42843', 'd:prevpaye', '076I8A42843-prevpaye'
        put 'br_paye_db:paye', '076I8A42843', 'd:employer_cat', '076I8A42843-employer_cat'
        put 'br_paye_db:paye', '076I8A42843', 'd:stc', '076I8A42843-stc'
        put 'br_paye_db:paye', '076I8A42843', 'd:actiondate', '076I8A42843-actiondate'
        put 'br_paye_db:paye', '076I8A42843', 'd:birthdate', '076I8A42843-birthdate'
        put 'br_paye_db:paye', '076I8A42843', 'd:deathdate', '076I8A42843-deathdate'
        put 'br_paye_db:paye', '076I8A42843', 'd:deathcode', '076I8A42843-deathcode'
        put 'br_paye_db:paye', '076I8A42843', 'd:mar_jobs', '103'
        put 'br_paye_db:paye', '076I8A42843', 'd:june_jobs', '106'
        put 'br_paye_db:paye', '076I8A42843', 'd:sept_jobs', '109'
        put 'br_paye_db:paye', '076I8A42843', 'd:dec_jobs', '112'
        put 'br_paye_db:paye', '076I8A42843', 'd:jobs_lastupd', '076I8A42843-jobs_lastupd'
        put 'br_paye_db:paye', '076I8A42843', 'd:mfullemp', '211'
        put 'br_paye_db:paye', '076I8A42843', 'd:msubemp', '212'
        put 'br_paye_db:paye', '076I8A42843', 'd:ffullemp', '221'
        put 'br_paye_db:paye', '076I8A42843', 'd:fsubemp', '222'
        put 'br_paye_db:paye', '076I8A42843', 'd:unclemp', '231'
        put 'br_paye_db:paye', '076I8A42843', 'd:unclsubemp', '232'
        put 'br_paye_db:paye', '076I8A42843', 'd:address1', '076I8A42843-address1'
        put 'br_paye_db:paye', '076I8A42843', 'd:address2', '076I8A42843-address2'
        put 'br_paye_db:paye', '076I8A42843', 'd:address3', '076I8A42843-address3'
        put 'br_paye_db:paye', '076I8A42843', 'd:address4', '076I8A42843-address4'
        put 'br_paye_db:paye', '076I8A42843', 'd:address5', '076I8A42843-address5'
        put 'br_paye_db:paye', '076I8A42843', 'd:postcode', '076I8A42843-postcode'
        put 'br_paye_db:paye', '076I8A42843', 'd:ubrn', '076I8A42843-ubrn'

4.  Start HBase Rest Service

        bin/hbase rest start

5.  Start This Service

        export BR_PAYE_QUERY_DB_HBASE_NAMESPACE=br_paye_db
        export BR_PAYE_QUERY_DB_HBASE_TABLE_NAME=paye

        sbt clean run

    This runs the service on the default Play port (9000).

6.  Query This API

        curl -i http://localhost:9000/v1/paye/054G6Y20621
        curl -i http://localhost:9000/v1/paye/076I8A42843

7.  Shutdown This Service

    Terminate the running command (typically Ctrl-C).

8.  TearDown Database

    Delete the table:

        disable 'br_paye_db:paye'
        drop 'br_paye_db:paye'

    followed by the namespace:

        drop_namespace 'br_paye_db'

9.  Shutdown HBase REST Service

        bin/hbase rest stop

10.  Shutdown HBase

         bin/hbase stop-hbase.sh


### Tracing
[kamon](http://kamon.io) is used to automatically instrument the application and report trace spans to
[zipkin](https://zipkin.io/).  The AspectJ Weaver is required to make this happen, see [adding-the-aspectj-weaver](http://kamon.io/documentation/1.x/recipes/adding-the-aspectj-weaver/)
for further details.

Kamon takes care of propagating the traceId across threads, and making the relevant traceId available to
logback's Mapped Diagnostic Context (MDC).

Tracing is not enabled during the execution of tests, resulting in log statements that contain a traceId
with the value "undefined".

To undertake manual trace testing, run a local Zipkin 2 server.  One simple way to do this is via Docker:

    docker run --rm -d -p 9411:9411 openzipkin/zipkin:2.11

Then run this service via `sbt run`, and exercise an endpoint.

The resulting trace information should be available in the Zipkin UI at
[http://localhost:9411/zipkin/](http://localhost:9411/zipkin/).


### Service Configuration
As is standard for Play, the runtime configuration file can be found at `src/main/resources/application.conf`.

This file adopts a pattern where each variable has a sensible default for running the application locally,
which may then be overridden by an environment variable (if defined).  For example:

    host = "localhost"
    host = ${?BR_PAYE_QUERY_DB_HBASE_HOST}

The actual settings used for our formal deployment environments are held outside of Github, and rely on the
the ability to override settings via environment variables in accordance with the '12-factor app' approach.

Note that acceptance tests (and the entire IntegrationTest phase generally) use a dedicated configuration
that is defined at `src/it/resources/it_application.conf`.  This imports the standard configuration, and then
overrides the environment to that expected by locally executing acceptance tests.  This allows us to specify
non-standard ports for example, to avoid conflicts with locally running services.  For this to work, the
build file overrides the `-Dconfig.resource` setting when executing the IntegrationTest phase.
