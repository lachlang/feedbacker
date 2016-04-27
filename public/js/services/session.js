fbServices.service('Session', ['$log','$http', '$q', function($log, $http, $q) {

	var validSession = false;

	return {
	// 	login: function (username, password) {
	// 		return $http({
	// 			method: "PUT",
	// 			url: "/api/session/login",
	// 			data: {
	// 				apiVersion: "1.0",
	// 				body: {
	// 					username: username,
	// 					password: password
	// 				}
	// 			}
	// 		})
	// 	},

		login: function() {
			var deferred = $q.defer();

			$http.get("/api/session/login").then(function(result) {
				validSession = true;
				deferred.resolve(result)
			}, function() {
				validSession = false;
				deferred.reject();
			});
			return deferred.promise;
		},

		logout: function() {
			validSession = false;
			return $http.get("/api/session/logout");
		},

		isLoggedIn: function() {
			return validSession;
		}
	}
}]);