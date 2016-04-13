var fbServices = angular.module('feedbacker.services', []);

var fbControllers = angular.module('feedbacker.controllers', []);

var feedbackerApp = angular.module('feedbacker', ['feedbacker.services','feedbacker.controllers','ngRoute']);