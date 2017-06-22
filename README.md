# feedbacker

*Because Feedback is a Gift!*
-----------------------------

This is web app designed to facilitate the collection of feedback for and
individual.  There is a specific workflow around the nomination of people to
provide the feedback, submission and review, and distribution.

The nature of the feedback is in the form of a configurable set of questions 
with pick list answers and a free text comments section.  All questions are 
optional by design since any feedback is better than none.

A useful future extension would be to facilitate ad-hoc and unsolicited feedback
however that is (quite a long way) out of scope of this exercise.

### Getting started

#### UI Demo

To start having a look at the simple demo implemented here you will need a 
basic webserver. The author used [npm http-server](https://www.npmjs.com/package/http-server)
executed from the ```/base/feedbacker``` directory.

To run the demo you will also need to copy the sample service responses from
the ```/feedbacker/test/api/samples``` directory to the 
```/feedbacker/public/api``` directory.


#### UI Tests

User interface tests are implemented using [Karma](https://karma-runner.github.io/0.13/intro/configuration.html).

Running these requires the following npm installations:

```
> npm install karma --save-dev
> npm install karma-jasmine jasmine-core karma-chrome-launcher --save-dev
> npm install -g karma-cli
```

The test may be executed via ```karma start conf/karma.conf.js``` from the base
project directory.

#### Server Test and Startup

The server is implemented in Scala and by default uses a PostgreSQL database.

Some useful commands are:

```
> sbt
sbt> test
sbt> run
sbt> docker:publishLocal
```

### Tech Stack

Feedbacker is implemented using scala and Play Framework on the server side, with Angular JS (version 1) and Bootstrap
in the browser.

To develop you will need:

* [sbt (Scala Build Tool)](http://www.scala-sbt.org/)
* [NodeJS (for testing only)](https://nodejs.org/en/download/)
* [PostgreSQL](https://www.postgresql.org/)

Tools and documentation include:

* [Play Framework (v2.5.x)](https://www.playframework.com/documentation/2.5.x/Home)
* [Angular JS (v1.5.3)](https://docs.angularjs.org/guide)
* [Angular Bootstrap (v2.5.0)](https://angular-ui.github.io/bootstrap/)
* [Bootstrap CSS (v3.3.5)](http://getbootstrap.com/getting-started/)


#### Database Development Configuration

To initialise the database for development follow the below steps:

1. Install [PostgreSQL](https://www.postgresql.org/)
2. Create a system user with the following credentials
Username: `feedback-service`
Password: `password`
3. Create a new schema called **feedbacks** using the new user
4. Apply all the *up* evolutions in the /conf/evolutions directory

NOTE: change the system password when deploying on a server.

### Future Scope

There are are number of features which have been considered but not implemented at this point.  These include:

* pre-configure an internal hierarchy model within Feedbacker
* additional reporting for viewing and/or sharing feedback
* renaming Feedbacker to remove needless vowels