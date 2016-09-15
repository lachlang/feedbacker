# feedbacker

*Because Feedback is a Gift!*
-----------------------------

This is web app designed to facilitate the collection of feedback for and
individual.  There is a specific workflow around the nomination of people to
provide the feedback, submission and review, and distribution.

The nature of the feedback is in the form of a configurable set of questions 
with pick list answers and a free text comments section.  All questions are 
optional by design since any feedback is better than none.

A useful future extension would be to faciliate ad-hoc and unsolicited feedback
however that is (quite a long way) out of scope of this exercise.

### Scope and Objectives

The current implemention is constrained to the UI components of a web 
application.  

In particular there is *no* server code implemented at this point.

Future implementation concerns include:
* authentication (both server and client side)
* a staff hierarchy model on the server side to drive visibility of published
feedback
	* this could either be hard coded as a stand alone solution or
	* integrated with a centralised directory
* a consolidated feedback view for a given individual

### Getting started

To start having a look at the simple demo implemented here you will need a 
basic webserver. The author used [npm http-server](https://www.npmjs.com/package/http-server)
executed from the ```/base/feedbacker``` directory.

To run the demo you will also need to copy the sample service responses from
the ```/feedbacker/test/api/samples``` directory to the 
```/feedbacker/public/api``` directory.

Finally unit tests are implemented using [Karma](https://karma-runner.github.io/0.13/intro/configuration.html)
and can be exected via ```karma start conf/karma.conf.js``` from the base
project directory.
