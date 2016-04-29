fbServices.service('Session', ['$log','$http', '$q', function($log, $http, $q) {

	var validSession = false;

	return {
		// login: function (username, password) {
		// 	var deferred = $q.defer();

		// 	$http({
		// 		method: "PUT",
		// 		url: "/api/session/login",
		// 		data: {
		// 			apiVersion: "1.0",
		// 			body: {
		// 				username: username,
		// 				password: password
		// 			}
		// 		}
		// 	}).then(function(result) {
		// 		//todo: do something with the token
		// 		validSession = true;
		// 		deferred.resolve(result)
		// 	}, function() {
		// 		validSession = false;
		// 		deferred.reject();
		// 	});
		// 	return deferred.promise;
		// },

		// this is a bodgy hack to support demo functionality - this one is first against the wall
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