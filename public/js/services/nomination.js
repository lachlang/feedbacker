fbServices.service('Nomination', ['$log', '$http', function($log, $http) {

	return {
		getCurrentNominations: function() {
			return $http.get("/api/nominations");
		},

		addNomination: function() {
			// return $http({

			// })
		},

		cancelNomination: function(id) {
			return $http.get("/api/nominations/cancel/" + id);
		}
	}
}]);