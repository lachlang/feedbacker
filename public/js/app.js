var fbServices = angular.module('feedbacker.services', ['ngCookies']);

var fbControllers = angular.module('feedbacker.controllers', []);

var feedbackerApp = angular.module('feedbacker', ['feedbacker.services','feedbacker.controllers','ngRoute', 'ngCookies', 'ui.bootstrap.alert', 'ui.bootstrap.buttons'])

.config(function($httpProvider) {
    // set up an anonymous interceptor on $http - sets auth header
    //note that I can only inject a provider during config stage
    //and in config I cannot inject a service or instance (inc. rootScope)
    //so I have to have a function to retrieve the cookie val
    $httpProvider.interceptors.push(function($q, $location, $rootScope, $cookies, $log) {
        return {
//            // on request
//            request : function(config) {
//
//              // get access token cookie val
////                //can I put this somewhere else?
////                var accessToken = function() {
////                        var nameEQ = "FEEDBACKER_SESSION=";
////                        var ca = document.cookie.split(';');
////                        for (var i = 0; i < ca.length; i++) {
////                                var c = ca[i];
////                                while (c.charAt(0) == ' ')
////                                        c = c.substring(1, c.length);
////                                if (c.indexOf(nameEQ) == 0)
////                                        return c.substring(nameEQ.length, c.length);
////                        }
////                        return null;
////                }();
////                if (accessToken) {
////                        // add auth bearer access token header to config
////                        config.headers["Authorization"] = "Bearer " + accessToken;
////                        // $log.debug("set auth header");
////                }
//                return config || $q.when(config);
//            },
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