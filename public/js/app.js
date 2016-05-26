var fbServices = angular.module('feedbacker.services', ['ngCookies']);

var fbControllers = angular.module('feedbacker.controllers', []);

var feedbackerApp = angular.module('feedbacker', ['feedbacker.services','feedbacker.controllers','ngRoute', 'ngCookies', 'ui.bootstrap.alert', 'ui.bootstrap.buttons'])

.config(function($httpProvider) {
    $httpProvider.interceptors.push(function($q, $location, $rootScope, $cookies, $log) {
        return {
            responseError: function(rejection) {

            	if(rejection.status === 401 || rejection.status == 403) {
                    $rootScope.$broadcast("unauthenticated", {});
                	$cookies.remove("FEEDBACKER_SESSION");
                }
                return $q.reject(rejection);
            }
        };
    });
});