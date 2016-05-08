fbServices.service('Account', ['$log', '$http', '$q', function($log, $http, $q) {

	var invalidRequestError = function () {
		var deferred = $q.defer();
		deferred.reject("Invalid request");
		return deferred.promise
	};

	return {
		register: function(name, email, managerEmail, password) {
			if (!name || !email || !managerEmail || !password) {
				return invalidRequestError();
			}
			return $http({
				method: "POST",
				url: "/api/register",
				data: {
					body: {
						name: name,
						email: email,
						managerEmail: managerEmail,
						password: password
					}
				}
			});
		},

		activate: function(email, token) {
			if (!email || !token) {
				return invalidRequestError();
			}
			return $http({
				method: "POST",
				url: "/api/register/activate/" + feedbackId,
				data:{
					apiVersion: "1.0",
					body: {
						email: name,
						token: token
					}
				}
			})
		},

		requestActivationEmail: function(email) {
			return $http({
				method:"PUT",
				url:"/api/register/activate/email",
				data: {
					body:{ email: email}
				}
			});
		},

		resetPassword: function(oldPassword, newPassword, token) {
			return $http({
				method:"PUT",
				url:"/api/password/reset",
				data: {
					body: { email: email}
				}
			});
		},

		requestPasswordResetEmail: function(email) {
			return $http({
				method:"PUT",
				url:"/api/password/reset/email",
				data: {
					body:{ email: email}
				}
			});
		}
	}
}]);