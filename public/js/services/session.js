fbServices.service('Session', ['$rootScope', '$location', '$log','$http', '$q', '$cookies', 'Model', function($rootScope, $location, $log, $http, $q, $cookies, Model) {

	$rootScope.$on('unauthenticated', function() {
		$log.error("[Session.$on.unauthenticated] UNAUTHENTICATED RESPONSE EVENT FROM SERVER. Destroying Session...");
		callLogout();
	});

	var callLogout = function() {
		$log.debug("[Session.logout] Ending session...");
		$cookies.remove("FEEDBACKER_SESSION");
		$location.path("#/landing");
		return $http.get("/api/session/logout");
	};

	var sessionService = {
		login: function (username, password) {
			var deferred = $q.defer();

			$http({
				method: "PUT",
				url: "/api/session/login",
				data: {
					apiVersion: "1.0",
					body: {
						username: username,
						password: password
					}
				}
			}).then(function(result){
				deferred.resolve(result)
			}, function(result){
				deferred.reject(result);
			});
			return deferred.promise
		},

		logout: callLogout,

		validSession: function() {
			return $cookies.get("FEEDBACKER_SESSION") != undefined;
		}

	}
	return sessionService;
}]);