var fbServices = angular.module('feedbacker.services', ['ngCookies']);

var fbFilters = angular.module('feedbacker.filters', []);

var fbControllers = angular.module('feedbacker.controllers', []);

var feedbackerApp = angular.module('feedbacker', ['feedbacker.services','feedbacker.filters', 'feedbacker.controllers', 'ngRoute', 'ngCookies', 'ui.bootstrap'])// 'ui.bootstrap.alert', 'ui.bootstrap.buttons', 'ui.bootstrap.typeahead'])

.config(function($httpProvider) {
    $httpProvider.interceptors.push(function($q, $location, $rootScope, $cookies, $log) {
        return {
            responseError: function(rejection) {

            	if(rejection.status == 403) {
                    $rootScope.$broadcast("unauthenticated", {});
                	$cookies.remove("FEEDBACKER_SESSION");
                }
                return $q.reject(rejection);
            }
        };
    });
});