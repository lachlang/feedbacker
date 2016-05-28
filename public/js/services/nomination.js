fbServices.service('Nomination', ['$log', '$http', function($log, $http) {

	return {

		getNomineeCandidates: function() {
			return $http.get("/api/nominations/candidates");
		},

		getCurrentNominations: function() {
			return $http.get("/api/nominations");
		},

		addNomination: function(nomination) {
			return $http({
				method: "POST",
				url: "/api/nominations",
				data:{
					apiVersion: "1.0",
					body: nomination
				}
			})
		},

		cancelNomination: function(id) {
			// LG it would be better to use DELETE here but I'm not sure
			// if it blocked by the firewall
			return $http.get("/api/nominations/cancel/" + id);
		}
	}
}]);