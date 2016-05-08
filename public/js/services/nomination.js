fbServices.service('Nomination', ['$log', '$http', function($log, $http) {

	return {
		getCurrentNominations: function() {
			return $http.get("/api/nominations");
		},

		addNomination: function(nomination) {
			$http({
				method: "POST",
				url: "/api/nominations",
				data:{
					apiVersion: "1.0",
					body: nomination
				}
			})
		},

		cancelNomination: function(id) {
			return $http.get("/api/nominations/cancel/" + id);
		}
	}
}]);