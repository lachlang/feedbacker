fbServices.service('Session', ['$log','$http', function($log, $http) {

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
			return $http.get("/api/session/login");
		},

		logout: function() {
			return $http.get("/api/session/logout");
		}
	}
}]);