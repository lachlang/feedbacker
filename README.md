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
npm install karma --save-dev
npm install karma-jasmine jasmine-core karma-chrome-launcher --save-dev
npm install -g karma-cli
```

The test may be executed via ```karma start conf/karma.conf.js``` from the base
project directory.

#### Server config

The server is implemented in Scala and by default uses a PostgreSQL database.

### Future Scope

There are are number of features which have been considered but not implemented at this point.  These include:

* pre-configure an internal hierarchy model within Feedbacker
* admin controls to set up additional review cycles and/or update questions
* additional reporting for viewing and/or sharing feedback
* export of feedback results to csv or excel for appropriate users
* nomination for ad-hoc/immediate feedback for individuals without a nomination
* renaming Feedbacker to remove needless vowels